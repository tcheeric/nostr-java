package nostr.mcp.integration;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import nostr.id.Identity;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.tool.PublishNoteTool;
import nostr.mcp.tool.QueryEventsTool;
import nostr.mcp.tool.UpdateProfileTool;
import nostr.mcp.write.RateLimit;
import nostr.mcp.write.WriteGuard;
import nostr.mcp.write.WritePolicy;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Publishes to a real relay, through the guard, and reads the result back.
 *
 * <p>The claim worth testing is not that the code signs an event but that a relay accepts it and
 * serves it again. Signature encoding, event id derivation and the relay's own validation are all
 * outside a unit test, and all of them are places where a plausible-looking event is silently
 * rejected.
 */
@Testcontainers
class PublishToolsIT {

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13"))
          .withExposedPorts(8080)
          .withStartupAttempts(5)
          .waitingFor(new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  // Verifies the two-step confirmation actually publishes: the preview stores nothing, and the
  // confirmed call produces an event the relay serves back.
  @Test
  void aConfirmedNoteReachesTheRelayAndCanBeReadBack() throws Exception {
    String content = "confirmed note " + System.nanoTime();

    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf("personal")) {
      PublishNoteTool tool = new PublishNoteTool(guard(pool, vault, WritePolicy.CONFIRM));

      CallToolResult preview =
          tool.call(new CallToolRequest("nostr_publish_note", Map.of("content", content)));
      assertTrue(textOf(preview).contains("Nothing has been published yet"), textOf(preview));
      assertEquals(0, storedNotesBy(pool, vault), "the preview published something");

      String token = String.valueOf(structuredOf(preview).get("confirmationToken"));
      CallToolResult published =
          tool.call(
              new CallToolRequest(
                  "nostr_publish_note", Map.of("content", content, "confirmationToken", token)));

      assertFalse(Boolean.TRUE.equals(published.isError()), textOf(published));
      assertTrue(textOf(published).startsWith("Published "), textOf(published));
      assertEquals(1, storedNotesBy(pool, vault));
    }
  }

  // Verifies an unconfirmed note never reaches the relay, which is the entire point of the
  // confirming policy: a hallucinated post is a no-op.
  @Test
  void anUnconfirmedNoteNeverReachesTheRelay() throws Exception {
    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf("personal")) {
      PublishNoteTool tool = new PublishNoteTool(guard(pool, vault, WritePolicy.CONFIRM));

      tool.call(new CallToolRequest("nostr_publish_note", Map.of("content", "never sent")));
      tool.call(new CallToolRequest("nostr_publish_note", Map.of("content", "also never sent")));

      assertEquals(0, storedNotesBy(pool, vault));
    }
  }

  // Verifies the allowing policy publishes on the first call, for trusted automation.
  @Test
  void allowingPublishesOnTheFirstCall() throws Exception {
    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf("personal")) {
      PublishNoteTool tool = new PublishNoteTool(guard(pool, vault, WritePolicy.ALLOW));

      CallToolResult result =
          tool.call(
              new CallToolRequest(
                  "nostr_publish_note", Map.of("content", "direct " + System.nanoTime())));

      assertTrue(textOf(result).startsWith("Published "), textOf(result));
      assertEquals(1, storedNotesBy(pool, vault));
    }
  }

  // Verifies a published profile is readable as a profile, proving kind-0 encoding is right
  // rather than merely well-formed.
  @Test
  void aPublishedProfileIsReadableAsAProfile() throws Exception {
    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf("personal")) {
      new UpdateProfileTool(guard(pool, vault, WritePolicy.ALLOW))
          .call(
              new CallToolRequest(
                  "nostr_update_profile", Map.of("name", "alice", "about", "integration")));

      CallToolResult profile =
          new nostr.mcp.tool.GetProfileTool(
                  new EventQuery(pool),
                  new nostr.mcp.directory.Nip05Resolver(new nostr.mcp.directory.WellKnownJson()),
                  QueryLimits.defaults())
              .call(
                  new CallToolRequest(
                      "nostr_get_profile",
                      Map.of("pubkey", vault.publicKeyOf("personal").toHexString())));

      assertEquals("alice: integration", textOf(profile));
    }
  }

  // Verifies the rate limit holds against a real relay, so a runaway agent is stopped before the
  // events become permanent rather than after.
  @Test
  void theRateLimitStopsPublishingBeforeTheRelaySeesIt() throws Exception {
    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf("personal")) {
      PublishNoteTool tool =
          new PublishNoteTool(
              new WriteGuard(
                  pool,
                  vault,
                  WritePolicy.ALLOW,
                  new RateLimit(2, Duration.ofMinutes(1), Clock.systemUTC())));

      for (int index = 0; index < 2; index++) {
        tool.call(new CallToolRequest("nostr_publish_note", Map.of("content", "rate " + index)));
      }
      CallToolResult limited =
          tool.call(new CallToolRequest("nostr_publish_note", Map.of("content", "over the limit")));

      assertTrue(Boolean.TRUE.equals(limited.isError()), textOf(limited));
      assertTrue(textOf(limited).startsWith("WRITE_FORBIDDEN"), textOf(limited));
      assertEquals(2, storedNotesBy(pool, vault));
    }
  }

  private int storedNotesBy(RelayPool pool, IdentityVault vault) {
    CallToolResult found =
        new QueryEventsTool(new EventQuery(pool), QueryLimits.defaults(), Clock.systemUTC())
            .call(
                new CallToolRequest(
                    "nostr_query_events",
                    Map.of(
                        "authors", List.of(vault.publicKeyOf("personal").toHexString()),
                        "kinds", List.of(1))));
    return (int) structuredOf(found).get("count");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> structuredOf(CallToolResult result) {
    return (Map<String, Object>) result.structuredContent();
  }

  private WriteGuard guard(RelayPool pool, IdentityVault vault, WritePolicy policy) {
    return new WriteGuard(
        pool, vault, policy, new RateLimit(100, Duration.ofMinutes(1), Clock.systemUTC()));
  }

  private IdentityVault vaultOf(String alias) {
    byte[] key =
        HexFormat.of().parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString());
    return new IdentityVault(
        new KeySource() {
          @Override
          public Map<String, byte[]> loadKeys(IdentityBinding binding) {
            return Map.of(alias, key);
          }

          @Override
          public String type() {
            return "test";
          }
        },
        null);
  }

  private RelayPool pool() {
    return new RelayPool(List.of(relayUri()), PublishToolsIT::connect);
  }

  private static nostr.client.relay.RelayConnection connect(String relayUri) throws IOException {
    try {
      return new NostrRelayClient(relayUri, 30_000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(e);
    } catch (ExecutionException e) {
      throw new IOException(e.getCause());
    }
  }

  private String textOf(CallToolResult result) {
    return result.content().stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .map(TextContent::text)
        .findFirst()
        .orElse("");
  }

  private static String relayUri() {
    return "ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(8080);
  }
}
