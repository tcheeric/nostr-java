package nostr.mcp.integration;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.social.McpDirectMessageService;
import nostr.mcp.tool.ReadDirectMessagesTool;
import nostr.mcp.tool.SendDirectMessageTool;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sends real gift-wrapped messages between real identities through a real relay.
 *
 * <p>The two behaviours this ticket cares about only appear against a live relay: a recipient
 * with no relay list genuinely cannot be sent to, and the sender's own archival copy is a second
 * outcome that must not be counted as a failed delivery. Both were previously observed here
 * rather than reasoned about.
 */
@Testcontainers
class DirectMessageToolsIT {

  private static final int DM_RELAY_LIST_KIND = 10050;
  private static final int GIFT_WRAP_KIND = 1059;
  private static final String ALIAS = "sender";

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13"))
          .withExposedPorts(8080)
          .withStartupAttempts(5)
          .waitingFor(new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  // Verifies a message reaches a recipient who has published a relay list, which is the whole
  // delivery path: sealing, wrapping, relay-list lookup and publication.
  @Test
  void aMessageReachesARecipientWithARelayList() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();
    publishRelayList(sender);
    publishRelayList(recipient);

    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf(sender)) {
      CallToolResult sent = send(pool, vault, recipient, "hello " + System.nanoTime());

      assertFalse(Boolean.TRUE.equals(sent.isError()), textOf(sent));
      assertEquals("Delivered to 1 recipient.", textOf(sent));
      assertEquals(1L, structuredOf(sent).get("delivered"));
    }
  }

  // Verifies a recipient with no relay list is reported unreachable and named, since NIP-17
  // forbids sending to them and the user must know who missed the message.
  @Test
  void aRecipientWithNoRelayListIsReportedUnreachable() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Identity unreachable = Identity.generateRandomIdentity();
    publishRelayList(sender);

    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf(sender)) {
      CallToolResult sent = send(pool, vault, unreachable, "into the void");

      assertTrue(textOf(sent).contains("Delivered to 0 of 1"), textOf(sent));
      assertTrue(textOf(sent).contains("no relay list"), textOf(sent));
      assertTrue(textOf(sent).contains(unreachable.getPublicKey().toHexString()), textOf(sent));
    }
  }

  // Verifies a sender without their own relay list is told about their missing archival copy as
  // advice, not as a delivery failure: the recipient did receive the message.
  @Test
  void theSendersOwnCopyIsNotCountedAsAFailedDelivery() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();
    publishRelayList(recipient);

    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf(sender)) {
      CallToolResult sent = send(pool, vault, recipient, "one way " + System.nanoTime());

      assertTrue(textOf(sent).startsWith("Delivered to 1 recipient."), textOf(sent));
      assertTrue(textOf(sent).contains("archival copy"), textOf(sent));
      assertEquals(1L, structuredOf(sent).get("delivered"));
    }
  }

  // Verifies a sent message can be read back and decrypted by its recipient, proving the
  // envelope is genuinely readable rather than merely well-formed.
  @Test
  void aSentMessageIsReadableByItsRecipient() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();
    publishRelayList(sender);
    publishRelayList(recipient);
    String content = "readable " + System.nanoTime();

    try (RelayPool pool = pool();
        IdentityVault senderVault = vaultOf(sender)) {
      send(pool, senderVault, recipient, content);
    }

    try (RelayPool pool = pool();
        IdentityVault recipientVault = vaultOf(recipient)) {
      CallToolResult read =
          new ReadDirectMessagesTool(
                  new McpDirectMessageService(recipientVault, pool, Set.of(ALIAS)),
                  recipientVault,
                  new EventQuery(pool),
                  QueryLimits.defaults(),
                  Clock.systemUTC())
              .call(new CallToolRequest("nostr_read_direct_messages", Map.of()));

      assertFalse(Boolean.TRUE.equals(read.isError()), textOf(read));
      assertTrue(read.structuredContent().toString().contains(content), textOf(read));
    }
  }

  // Verifies reading is refused unless the identity's owner has enabled it, since decrypting
  // correspondence puts it into the conversation and the host's logs.
  @Test
  void readingIsRefusedUnlessExplicitlyEnabled() throws Exception {
    Identity owner = Identity.generateRandomIdentity();

    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf(owner)) {
      CallToolResult read =
          new ReadDirectMessagesTool(
                  new McpDirectMessageService(vault, pool, Set.of()),
                  vault,
                  new EventQuery(pool),
                  QueryLimits.defaults(),
                  Clock.systemUTC())
              .call(new CallToolRequest("nostr_read_direct_messages", Map.of()));

      assertTrue(Boolean.TRUE.equals(read.isError()), textOf(read));
      assertTrue(textOf(read).contains("not enabled"), textOf(read));
    }
  }

  // Verifies the relay never sees who is talking to whom, which is the guarantee NIP-17 exists
  // to provide and the reason NIP-04 is not offered at all.
  @Test
  void theRelayCannotSeeTheCorrespondents() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();
    publishRelayList(sender);
    publishRelayList(recipient);

    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf(sender)) {
      send(pool, vault, recipient, "private " + System.nanoTime());
    }

    try (RelayPool pool = pool()) {
      List<GenericEvent> wraps =
          new EventQuery(pool)
              .run(
                  nostr.event.filter.EventFilter.builder().kind(GIFT_WRAP_KIND).limit(50).build(),
                  50,
                  Duration.ofSeconds(15))
              .events();

      assertFalse(wraps.isEmpty(), "no gift wraps were stored");
      assertTrue(
          wraps.stream()
              .noneMatch(
                  wrap ->
                      wrap.getPubKey().toHexString().equals(sender.getPublicKey().toHexString())),
          "a gift wrap was signed by the real sender, exposing the correspondents");
    }
  }

  private CallToolResult send(
      RelayPool pool, IdentityVault vault, Identity recipient, String content) {
    return new SendDirectMessageTool(
            new McpDirectMessageService(vault, pool, Set.of()), vault)
        .call(
            new CallToolRequest(
                "nostr_send_direct_message",
                Map.of(
                    "recipients", List.of(recipient.getPublicKey().toHexString()),
                    "content", content)));
  }

  /** Publishes the kind-10050 list that says where this key receives private messages. */
  private void publishRelayList(Identity owner) throws Exception {
    GenericEvent relayList =
        GenericEvent.builder()
            .pubKey(owner.getPublicKey())
            .kind(DM_RELAY_LIST_KIND)
            .content("")
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    relayList.addTag(new nostr.event.tag.GenericTag("relay", List.of(relayUri())));
    relayList.update();
    owner.sign(relayList);
    try (RelayPool pool = pool()) {
      pool.publish(relayList);
    }
  }

  private IdentityVault vaultOf(Identity identity) {
    byte[] key = HexFormat.of().parseHex(identity.getPrivateKey().toHexString());
    return new IdentityVault(
        new KeySource() {
          @Override
          public Map<String, byte[]> loadKeys(IdentityBinding binding) {
            return Map.of(ALIAS, key);
          }

          @Override
          public String type() {
            return "test";
          }
        },
        null);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> structuredOf(CallToolResult result) {
    return (Map<String, Object>) result.structuredContent();
  }

  private RelayPool pool() {
    return new RelayPool(List.of(relayUri()), DirectMessageToolsIT::connect);
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
