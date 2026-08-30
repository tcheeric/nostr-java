package nostr.mcp.write;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.client.relay.NoRelayAcceptedException;
import nostr.client.relay.PublishResult;
import nostr.client.relay.RelayPool;
import nostr.event.impl.GenericEvent;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.tool.ToolFailure;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The one place a write can happen, and the conditions it must satisfy first.
 *
 * <p>Every publishing tool goes through here rather than reaching the relay pool itself, so the
 * policy, the rate limit and the audit log are properties of the server rather than habits each
 * tool has to remember. A new write tool inherits all three by construction.
 *
 * <p>Under {@link WritePolicy#CONFIRM} a write is a two-step conversation: the first call signs
 * the event, holds it, and hands back a preview with a token; the second call presents the token
 * and the held event is sent. This turns a hallucinated post into a no-op, because an agent that
 * invented the intention will not follow through with the token it was given for it.
 */
@Slf4j
public final class WriteGuard {

  private static final int TOKEN_BYTES = 16;

  private final Map<String, PendingWrite> pendingByToken = new ConcurrentHashMap<>();
  private final SecureRandom tokens = new SecureRandom();
  private final RelayPool relayPool;
  private final IdentityVault identityVault;
  private final WritePolicy policy;
  private final RateLimit rateLimit;

  /**
   * @param relayPool where accepted writes go
   * @param identityVault signs the event, without releasing the key
   * @param policy how much freedom the agent has
   * @param rateLimit the cap on how often it may write
   */
  public WriteGuard(
      @NonNull RelayPool relayPool,
      @NonNull IdentityVault identityVault,
      @NonNull WritePolicy policy,
      @NonNull RateLimit rateLimit) {
    this.relayPool = relayPool;
    this.identityVault = identityVault;
    this.policy = policy;
    this.rateLimit = rateLimit;
  }

  /**
   * Prepare an event for publishing, signing it as the chosen identity.
   *
   * @param event the unsigned event
   * @param requestedAlias the identity to sign as, or empty to use the default
   * @return the signed event held under a token
   * @throws nostr.mcp.tool.ToolException when writing is denied, the identity is unknown or
   *     ambiguous, or the rate limit is exhausted
   */
  public PendingWrite prepare(@NonNull GenericEvent event, Optional<String> requestedAlias) {
    refuseIfDenied();
    String alias = resolveIdentity(requestedAlias);
    enforceRateLimit(alias);
    GenericEvent signed = sign(event, alias);
    PendingWrite pending =
        new PendingWrite(newToken(), signed, alias);
    if (policy.requiresConfirmation()) {
      pendingByToken.put(pending.token(), pending);
    }
    return pending;
  }

  /**
   * Publish an event the agent has confirmed.
   *
   * @param token the handle from the preview
   * @return the per-relay outcome
   * @throws nostr.mcp.tool.ToolException when the token is unknown or every relay refused
   */
  public PublishResult publishConfirmed(@NonNull String token) {
    PendingWrite pending = pendingByToken.remove(token);
    if (pending == null) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "That confirmation token is not valid. It may already have been used, or the server may"
              + " have restarted. Call the tool again without a token to get a fresh preview.");
    }
    return send(pending);
  }

  /**
   * Publish immediately, for a policy that does not require confirmation.
   *
   * @param pending the prepared write
   * @return the per-relay outcome
   * @throws nostr.mcp.tool.ToolException when every relay refused
   */
  public PublishResult publishDirectly(@NonNull PendingWrite pending) {
    return send(pending);
  }

  /**
   * Whether a write must be confirmed before it is sent.
   *
   * @return true under the confirming policy
   */
  public boolean requiresConfirmation() {
    return policy.requiresConfirmation();
  }

  /**
   * Sends the event and records what happened.
   *
   * <p>Partial success is success. The SDK reports each relay separately, and an event accepted
   * anywhere is on the network; calling that a failure would invite the agent to retry a write
   * that already landed, which on a permanent medium is worse than the original problem.
   */
  private PublishResult send(PendingWrite pending) {
    try {
      PublishResult result = relayPool.publish(pending.event());
      logWrite(pending, result);
      return result;
    } catch (java.io.IOException e) {
      if (!(e instanceof NoRelayAcceptedException rejection)) {
        throw ToolFailure.RELAY_UNREACHABLE.raise("Could not reach any relay: " + e.getMessage());
      }
      log.warn(
          "Write rejected: event {} kind {} as {} reached no relay",
          pending.event().getId(),
          pending.event().getKind(),
          identityVault.publicKeyOf(pending.identityAlias()).toHexString());
      throw ToolFailure.RELAY_REJECTED.raise(describeRejection(rejection));
    }
  }

  /**
   * Records every write with what was published, by whom, and where it went.
   *
   * <p>The audit trail is the only durable record of what an agent did on the user's behalf, and
   * it outlives the conversation that caused it.
   */
  private void logWrite(PendingWrite pending, PublishResult result) {
    log.info(
        "Write accepted: event {} kind {} as {} to {}",
        result.getEventId(),
        pending.event().getKind(),
        identityVault.publicKeyOf(pending.identityAlias()).toHexString(),
        result.getAcceptingRelays());
  }

  private String describeRejection(NoRelayAcceptedException rejection) {
    PublishResult result = rejection.getPublishResult();
    if (result == null) {
      return "No relay accepted the event: " + rejection.getMessage();
    }
    StringBuilder detail = new StringBuilder("No relay accepted the event.");
    result
        .getFailures()
        .forEach(
            failure ->
                detail
                    .append(' ')
                    .append(failure.relayUri())
                    .append(": ")
                    .append(failure.findReason().orElse(failure.status().name()))
                    .append('.'));
    return detail.toString();
  }

  private void refuseIfDenied() {
    if (!policy.allowsWriteTools()) {
      throw ToolFailure.WRITE_FORBIDDEN.raise(
          "This server is configured read-only (write-policy: deny), so it cannot publish.");
    }
  }

  /**
   * Chooses the identity to sign as, refusing to guess.
   *
   * <p>Posting as the wrong account is public and irreversible, so where several identities exist
   * and none is the default, the tool asks rather than picking one.
   */
  private String resolveIdentity(Optional<String> requestedAlias) {
    if (requestedAlias.isPresent()) {
      String alias = requestedAlias.get();
      if (identityVault.find(alias).isEmpty()) {
        throw ToolFailure.IDENTITY_UNKNOWN.raise(
            "No identity called '"
                + alias
                + "'. Available: "
                + identityVault.list().stream().map(summary -> summary.alias()).toList());
      }
      return alias;
    }
    return identityVault
        .defaultAlias()
        .orElseThrow(
            () ->
                ToolFailure.IDENTITY_AMBIGUOUS.raise(
                    identityVault.isEmpty()
                        ? "This server holds no identity to sign with. Create one with the"
                            + " command line: java -jar nostr-java-mcp.jar keygen <alias>"
                        : "This server holds several identities and none is the default, so"
                            + " signing would be a guess. Name one in the 'identity' argument:"
                            + identityVault.list().stream().map(summary -> summary.alias()).toList()));
  }

  private void enforceRateLimit(String alias) {
    if (!rateLimit.tryAcquire(alias)) {
      throw ToolFailure.WRITE_FORBIDDEN.raise(
          "Identity '" + alias + "' has reached its write rate limit of " + rateLimit.describe()
              + ". Wait before publishing again.");
    }
  }

  private GenericEvent sign(GenericEvent event, String alias) {
    event.setPubKey(identityVault.publicKeyOf(alias));
    event.update();
    identityVault.signAs(alias, event);
    return event;
  }

  private String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    tokens.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }
}
