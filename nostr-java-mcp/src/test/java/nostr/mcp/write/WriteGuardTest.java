package nostr.mcp.write;

import nostr.client.relay.RelayPool;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.tool.ToolException;
import nostr.mcp.tool.ToolFailure;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies every condition a write must satisfy before it can reach a relay. */
class WriteGuardTest {

  // Verifies a confirming server signs and holds the event rather than sending it, which is what
  // makes a hallucinated post a no-op.
  @Test
  void confirmingHoldsTheEventInsteadOfSendingIt() {
    try (IdentityVault vault = vaultOf("personal")) {
      RecordingPool pool = new RecordingPool();
      WriteGuard guard = guard(pool, vault, WritePolicy.CONFIRM);

      PendingWrite pending = guard.prepare(note("hello"), Optional.empty());

      assertNotNull(pending.event().getSignature(), "the event was not signed");
      assertTrue(pool.published.isEmpty(), "the event was published without confirmation");
    }
  }

  // Verifies the held event is what gets published, so the content cannot change between the
  // preview an agent showed the user and what actually goes out.
  @Test
  void theConfirmedEventIsTheOneThatWasPreviewed() {
    try (IdentityVault vault = vaultOf("personal")) {
      RecordingPool pool = new RecordingPool();
      WriteGuard guard = guard(pool, vault, WritePolicy.CONFIRM);

      PendingWrite pending = guard.prepare(note("exact text"), Optional.empty());
      guard.publishConfirmed(pending.token());

      assertEquals(1, pool.published.size());
      assertEquals("exact text", pool.published.getFirst().getContent());
      assertEquals(pending.event().getId(), pool.published.getFirst().getId());
    }
  }

  // Verifies a token works once, so a replayed confirmation cannot post the same note twice.
  @Test
  void aTokenCannotBeUsedTwice() {
    try (IdentityVault vault = vaultOf("personal")) {
      RecordingPool pool = new RecordingPool();
      WriteGuard guard = guard(pool, vault, WritePolicy.CONFIRM);
      PendingWrite pending = guard.prepare(note("once"), Optional.empty());
      guard.publishConfirmed(pending.token());

      ToolException replayed =
          assertThrows(ToolException.class, () -> guard.publishConfirmed(pending.token()));

      assertEquals(ToolFailure.INVALID_ARGUMENT, replayed.getFailure());
      assertEquals(1, pool.published.size());
    }
  }

  // Verifies an invented token is refused, since a model that hallucinates a confirmation must
  // not thereby publish anything.
  @Test
  void anInventedTokenIsRefused() {
    try (IdentityVault vault = vaultOf("personal")) {
      RecordingPool pool = new RecordingPool();
      WriteGuard guard = guard(pool, vault, WritePolicy.CONFIRM);

      assertThrows(ToolException.class, () -> guard.publishConfirmed("made-up-token"));
      assertTrue(pool.published.isEmpty());
    }
  }

  // Verifies tokens are unguessable rather than sequential, so one cannot be inferred from
  // another an agent has already seen.
  @Test
  void tokensAreUnguessable() {
    try (IdentityVault vault = vaultOf("personal")) {
      WriteGuard guard = guard(new RecordingPool(), vault, WritePolicy.CONFIRM);

      String first = guard.prepare(note("one"), Optional.empty()).token();
      String second = guard.prepare(note("two"), Optional.empty()).token();

      assertNotEquals(first, second);
      assertEquals(32, first.length());
    }
  }

  // Verifies the allowing policy publishes without a second call, for trusted automation.
  @Test
  void allowingPublishesImmediately() {
    try (IdentityVault vault = vaultOf("personal")) {
      RecordingPool pool = new RecordingPool();
      WriteGuard guard = guard(pool, vault, WritePolicy.ALLOW);

      guard.publishDirectly(guard.prepare(note("direct"), Optional.empty()));

      assertEquals(1, pool.published.size());
    }
  }

  // Verifies a denied server refuses even if a write tool somehow reaches the guard, so the
  // policy holds at the point of action and not only at registration.
  @Test
  void denyingRefusesAtThePointOfAction() {
    try (IdentityVault vault = vaultOf("personal")) {
      RecordingPool pool = new RecordingPool();
      WriteGuard guard = guard(pool, vault, WritePolicy.DENY);

      ToolException refused =
          assertThrows(ToolException.class, () -> guard.prepare(note("nope"), Optional.empty()));

      assertEquals(ToolFailure.WRITE_FORBIDDEN, refused.getFailure());
      assertTrue(pool.published.isEmpty());
    }
  }

  // Verifies signing refuses to guess when several identities exist and none is the default,
  // since posting as the wrong account is public and irreversible.
  @Test
  void severalIdentitiesWithNoDefaultRefuseToGuess() {
    try (IdentityVault vault = vaultOf("personal", "project-bot")) {
      WriteGuard guard = guard(new RecordingPool(), vault, WritePolicy.ALLOW);

      ToolException ambiguous =
          assertThrows(ToolException.class, () -> guard.prepare(note("who?"), Optional.empty()));

      assertEquals(ToolFailure.IDENTITY_AMBIGUOUS, ambiguous.getFailure());
      assertTrue(ambiguous.getMessage().contains("project-bot"), ambiguous.getMessage());
    }
  }

  // Verifies naming an unknown identity lists the real ones rather than failing opaquely.
  @Test
  void anUnknownIdentityListsTheRealOnes() {
    try (IdentityVault vault = vaultOf("personal")) {
      WriteGuard guard = guard(new RecordingPool(), vault, WritePolicy.ALLOW);

      ToolException unknown =
          assertThrows(
              ToolException.class, () -> guard.prepare(note("hi"), Optional.of("nobody")));

      assertEquals(ToolFailure.IDENTITY_UNKNOWN, unknown.getFailure());
      assertTrue(unknown.getMessage().contains("personal"), unknown.getMessage());
    }
  }

  // Verifies the rate limit stops a runaway agent, which is the ordinary failure rather than
  // the exceptional one.
  @Test
  void theRateLimitStopsARunawayAgent() {
    try (IdentityVault vault = vaultOf("personal")) {
      RecordingPool pool = new RecordingPool();
      WriteGuard guard =
          new WriteGuard(
              pool,
              vault,
              WritePolicy.ALLOW,
              new RateLimit(2, Duration.ofMinutes(1), Clock.systemUTC()));

      guard.prepare(note("one"), Optional.empty());
      guard.prepare(note("two"), Optional.empty());

      ToolException limited =
          assertThrows(ToolException.class, () -> guard.prepare(note("three"), Optional.empty()));

      assertEquals(ToolFailure.WRITE_FORBIDDEN, limited.getFailure());
      assertTrue(limited.getMessage().contains("rate limit"), limited.getMessage());
    }
  }

  // Verifies the event is signed by the identity that was named, not merely by whichever key
  // happened to be first.
  @Test
  void theEventIsSignedByTheNamedIdentity() {
    try (IdentityVault vault = vaultOf("personal", "project-bot")) {
      WriteGuard guard = guard(new RecordingPool(), vault, WritePolicy.ALLOW);

      PendingWrite pending = guard.prepare(note("as the bot"), Optional.of("project-bot"));

      assertEquals(
          vault.publicKeyOf("project-bot").toHexString(),
          pending.event().getPubKey().toHexString());
    }
  }

  private WriteGuard guard(RelayPool pool, IdentityVault vault, WritePolicy policy) {
    return new WriteGuard(
        pool, vault, policy, new RateLimit(100, Duration.ofMinutes(1), Clock.systemUTC()));
  }

  private GenericEvent note(String content) {
    return GenericEvent.builder()
        .kind(1)
        .content(content)
        .createdAt(System.currentTimeMillis() / 1000)
        .build();
  }

  private IdentityVault vaultOf(String... aliases) {
    Map<String, byte[]> keys = new LinkedHashMap<>();
    for (String alias : aliases) {
      keys.put(
          alias,
          HexFormat.of().parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString()));
    }
    return new IdentityVault(
        new KeySource() {
          @Override
          public Map<String, byte[]> loadKeys(IdentityBinding binding) {
            return keys;
          }

          @Override
          public String type() {
            return "test";
          }
        },
        null);
  }

  /** A pool that records what it was asked to publish instead of reaching a relay. */
  private static final class RecordingPool extends RelayPool {

    private final List<GenericEvent> published = new java.util.ArrayList<>();

    private RecordingPool() {
      super(List.of(), relayUri -> {
        throw new IOException("no relays in this test");
      });
    }

    @Override
    public nostr.client.relay.PublishResult publish(GenericEvent event) {
      published.add(event);
      return nostr.client.relay.PublishResult.of(
          event.getId(),
          List.of(nostr.client.relay.RelayPublishOutcome.accepted("wss://relay.example")));
    }
  }
}
