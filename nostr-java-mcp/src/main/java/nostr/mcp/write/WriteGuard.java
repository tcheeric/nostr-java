package nostr.mcp.write;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.client.relay.NoRelayAcceptedException;
import nostr.client.relay.PublishResult;
import nostr.client.relay.RelayPool;
import nostr.event.impl.GenericEvent;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.SigningAlias;
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
    String alias = signWriteToken(event, requestedAlias);
    PendingWrite pending =
        new PendingWrite(newToken(), event, alias);
    if (policy.requiresConfirmation()) {
      pendingByToken.put(pending.token(), pending);
    }
    return pending;
  }

  /**
   * Sign something that acts on the world without going to a relay.
   *
   * <p>A Blossom upload is a write: it puts bytes on a public server under the user's key, and
   * once they are there they are addressed by their hash and may be copied anywhere. So it
   * passes the same three gates a published event does, and the only difference is that what
   * comes back is a credential rather than a queued publication.
   *
   * <p>Existing here rather than in the Blossom package is the point. A tool that signed through
   * the vault directly would silently skip the policy and the rate limit, and nothing would
   * catch it; routing through the guard means a new kind of write inherits all three by
   * construction, exactly as a new publishing tool does.
   *
   * @param event the unsigned event, which is mutated in place
   * @param requestedAlias the identity to sign as, or empty to use the default
   * @return the alias it was signed as
   * @throws nostr.mcp.tool.ToolException when writing is denied, the identity is unknown or
   *     ambiguous, or the rate limit is exhausted
   */
  public String signWriteToken(@NonNull GenericEvent event, Optional<String> requestedAlias) {
    String alias = authorizeWrite(requestedAlias);
    signAs(alias, event);
    return alias;
  }

  /**
   * Settle whether a write may happen at all, before doing the work it needs.
   *
   * <p>Split from signing because some writes have to gather something expensive first. A
   * Blossom upload cannot build its token until it has fetched the blob and hashed it, so
   * signing last would put an outbound fetch of up to the configured cap, buffered in memory,
   * on the wrong side of the rate limit: a caller past its quota would still make this server
   * pull the bytes before being refused. Taking the quota first makes the refusal free.
   *
   * @param requestedAlias the identity to write as, or empty to use the default
   * @return the alias the write is authorized for
   * @throws nostr.mcp.tool.ToolException when writing is denied, the identity is unknown or
   *     ambiguous, or the rate limit is exhausted
   */
  public String authorizeWrite(Optional<String> requestedAlias) {
    refuseIfDenied();
    String alias = resolveIdentity(requestedAlias);
    enforceRateLimit(alias);
    return alias;
  }

  /**
   * Sign an event as an already-authorized identity.
   *
   * <p>Takes no further quota: the caller paid for this write in {@link #authorizeWrite}. It is
   * also what lets a held write be re-signed later, which a token carrying an expiry needs.
   *
   * @param alias the identity to sign as, from {@link #authorizeWrite}
   * @param event the unsigned event, which is mutated in place
   */
  public void signAs(@NonNull String alias, @NonNull GenericEvent event) {
    sign(event, alias);
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
   * Whether this process operates exactly one identity.
   *
   * <p>Signing tools ask so they can leave the {@code identity} argument out of their schema
   * entirely. Offering an argument with one legal value invites a model to pass the wrong thing
   * and turns an impossible mistake back into a possible one.
   *
   * @return true when the server is bound to a single identity
   */
  public boolean bindsOneIdentity() {
    return identityVault.binding().isBound();
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

  private String resolveIdentity(Optional<String> requestedAlias) {
    return SigningAlias.resolve(identityVault, requestedAlias);
  }

  private void enforceRateLimit(String alias) {
    if (!rateLimit.tryAcquire(alias)) {
      throw ToolFailure.WRITE_FORBIDDEN.raise(
          "Identity '" + alias + "' has reached its write rate limit of " + rateLimit.describe()
              + ". Wait before publishing again.");
    }
  }

  private GenericEvent sign(GenericEvent event, String alias) {
    return SigningAlias.sign(identityVault, alias, event);
  }

  private String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    tokens.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }
}
