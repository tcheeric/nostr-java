package nostr.mcp.integration;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.mcp.directory.Nip05Resolver;
import nostr.mcp.directory.WellKnownJson;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.tool.GetProfileTool;
import nostr.mcp.tool.QueryEventsTool;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the read tools against a real relay.
 *
 * <p>The read path is almost entirely about behaviour a fake cannot reproduce: a query terminates
 * on an end-of-stored-events signal the relay sends when it chooses, events arrive on transport
 * threads, and the relay decides what matches a filter. A stubbed pool would prove only that the
 * tools call the methods this test's author expected them to.
 */
@Testcontainers
class ReadToolsIT {

  private static final DockerImageName RELAY_IMAGE =
      DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13");
  private static final int RELAY_PORT = 8080;
  private static final int PROFILE_KIND = 0;
  private static final int TEXT_NOTE_KIND = 1;

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(RELAY_IMAGE)
          .withExposedPorts(RELAY_PORT)
          .withStartupAttempts(5)
          .waitingFor(new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  // Verifies a published note is found by a query filtered on its author, which is the read
  // path working end to end: filter encoding, REQ, collection, and termination on EOSE.
  @Test
  void aPublishedNoteIsFoundByAuthor() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    String content = "read tools " + System.nanoTime();
    publish(note(author, content));

    try (RelayPool pool = pool()) {
      CallToolResult result =
          queryTool(pool)
              .call(
                  new CallToolRequest(
                      "nostr_query_events",
                      Map.of("authors", List.of(author.getPublicKey().toHexString()))));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).contains("Found 1 event"), textOf(result));
    }
  }

  // Verifies an npub is accepted where hex is, since that is the form a user pastes.
  @Test
  void anNpubAuthorWorksAsWellAsHex() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    publish(note(author, "npub query " + System.nanoTime()));

    try (RelayPool pool = pool()) {
      CallToolResult result =
          queryTool(pool)
              .call(
                  new CallToolRequest(
                      "nostr_query_events",
                      Map.of("authors", List.of(author.getPublicKey().toBech32String()))));

      assertTrue(textOf(result).contains("Found 1 event"), textOf(result));
    }
  }

  // Verifies a query that matches nothing says so plainly, rather than hanging until the
  // timeout, which is what a wrong termination condition would look like.
  @Test
  void aQueryMatchingNothingReturnsPromptly() throws Exception {
    Identity stranger = Identity.generateRandomIdentity();

    try (RelayPool pool = pool()) {
      long startedAt = System.currentTimeMillis();
      CallToolResult result =
          queryTool(pool)
              .call(
                  new CallToolRequest(
                      "nostr_query_events",
                      Map.of("authors", List.of(stranger.getPublicKey().toHexString()))));
      long elapsed = System.currentTimeMillis() - startedAt;

      assertEquals("No events matched.", textOf(result));
      assertTrue(elapsed < 10_000, "the query took " + elapsed + "ms, so it waited for a timeout");
    }
  }

  // Verifies the configured limit actually bounds the answer, and that the agent is told the
  // result was cut short rather than being left to assume it was complete.
  @Test
  void theEventLimitBoundsTheAnswerAndSaysSo() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    for (int index = 0; index < 5; index++) {
      publish(note(author, "limit " + index + " " + System.nanoTime()));
    }

    try (RelayPool pool = pool()) {
      CallToolResult result =
          new QueryEventsTool(new EventQuery(pool), new QueryLimits(2, Duration.ofSeconds(15)), Clock.systemUTC())
              .call(
                  new CallToolRequest(
                      "nostr_query_events",
                      Map.of("authors", List.of(author.getPublicKey().toHexString()))));

      assertTrue(textOf(result).contains("Found 2 events"), textOf(result));
      assertTrue(textOf(result).contains("limit was reached"), textOf(result));
    }
  }

  // Verifies a profile is fetched and its JSON content decoded, which is the part of the read
  // path a model most often gets wrong when doing it itself.
  @Test
  void aProfileIsFetchedAndDecoded() throws Exception {
    Identity subject = Identity.generateRandomIdentity();
    publish(profile(subject, "{\"name\":\"alice\",\"about\":\"testing\"}"));

    try (RelayPool pool = pool()) {
      CallToolResult result =
          profileTool(pool)
              .call(
                  new CallToolRequest(
                      "nostr_get_profile",
                      Map.of("pubkey", subject.getPublicKey().toHexString())));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertEquals("alice: testing", textOf(result));
    }
  }

  // Verifies a key with no profile is an ordinary answer rather than an error, since most keys
  // have never published one.
  @Test
  void aKeyWithNoProfileIsAnAnswerNotAnError() throws Exception {
    Identity stranger = Identity.generateRandomIdentity();

    try (RelayPool pool = pool()) {
      CallToolResult result =
          profileTool(pool)
              .call(
                  new CallToolRequest(
                      "nostr_get_profile",
                      Map.of("pubkey", stranger.getPublicKey().toHexString())));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).contains("No profile"), textOf(result));
    }
  }

  // Verifies a malformed profile is reported rather than failing the call, since anyone can
  // publish anything as kind 0 and that is not the caller's fault.
  @Test
  void aMalformedProfileDoesNotFailTheCall() throws Exception {
    Identity subject = Identity.generateRandomIdentity();
    publish(profile(subject, "this is not json"));

    try (RelayPool pool = pool()) {
      CallToolResult result =
          profileTool(pool)
              .call(
                  new CallToolRequest(
                      "nostr_get_profile",
                      Map.of("pubkey", subject.getPublicKey().toHexString())));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
    }
  }

  // Verifies a bad identifier is refused with a code the agent can act on, before any relay is
  // troubled with it.
  @Test
  void aBadIdentifierIsRefusedWithACode() throws Exception {
    try (RelayPool pool = pool()) {
      CallToolResult result =
          profileTool(pool).call(new CallToolRequest("nostr_get_profile", Map.of("pubkey", "wat")));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).startsWith("INVALID_ARGUMENT"), textOf(result));
    }
  }

  private QueryEventsTool queryTool(RelayPool pool) {
    return new QueryEventsTool(new EventQuery(pool), QueryLimits.defaults(), Clock.systemUTC());
  }

  private GetProfileTool profileTool(RelayPool pool) {
    return new GetProfileTool(
        new EventQuery(pool), new Nip05Resolver(new WellKnownJson()), QueryLimits.defaults());
  }

  private GenericEvent note(Identity author, String content) {
    return signed(author, TEXT_NOTE_KIND, content);
  }

  private GenericEvent profile(Identity author, String content) {
    return signed(author, PROFILE_KIND, content);
  }

  private GenericEvent signed(Identity author, int kind, String content) {
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(author.getPublicKey())
            .kind(kind)
            .content(content)
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    event.update();
    author.sign(event);
    return event;
  }

  private void publish(GenericEvent event) throws Exception {
    try (RelayPool pool = pool()) {
      pool.publish(event);
    }
  }

  private RelayPool pool() {
    return new RelayPool(List.of(relayUri()), ReadToolsIT::connect);
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
    return "ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(RELAY_PORT);
  }
}
