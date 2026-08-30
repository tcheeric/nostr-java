package nostr.mcp.integration;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.mcp.directory.WellKnownJson;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityLifecycle;
import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.identity.IdentityStore;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.identity.KeystoreException;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.relay.RelayDirectory;
import nostr.mcp.tool.CreateIdentityTool;
import nostr.mcp.tool.ExportIdentityBackupTool;
import nostr.mcp.tool.FetchThreadTool;
import nostr.mcp.tool.ImportIdentityTool;
import nostr.mcp.tool.PublishEventTool;
import nostr.mcp.tool.RelayInfoTool;
import nostr.mcp.tool.RenameIdentityTool;
import nostr.mcp.tool.SetDefaultIdentityTool;
import nostr.mcp.write.RateLimit;
import nostr.mcp.write.WriteGuard;
import nostr.mcp.write.WritePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the tools that only ever had their registration checked.
 *
 * <p>Eight of the twenty-two were never called by anything: a coverage measurement said so, and
 * "it appears in the golden tool list" is a guarantee that the class compiles, not that it works.
 * These are the calls that were missing.
 */
@Testcontainers
class UntestedToolsIT {

  private static final int RELAY_PORT = 8080;

  @TempDir Path directory;

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13"))
          .withExposedPorts(RELAY_PORT)
          .withStartupAttempts(5)
          .waitingFor(new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  // Verifies the NIP-11 tool reads a real relay's own description of itself, which is how an
  // agent learns a relay's rules without breaking one.
  @Test
  void relayInfoReadsTheRelaysOwnDocument() {
    try (RelayPool pool = pool()) {
      CallToolResult result =
          new RelayInfoTool(directoryOf(), new WellKnownJson())
              .call(new CallToolRequest("nostr_relay_info", Map.of("relay", relayUri())));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(structuredOf(result).containsKey("supported_nips"), structuredOf(result).toString());
    }
  }

  // Verifies an unknown relay name is refused by naming the ones that exist, rather than
  // failing with something the agent cannot act on.
  @Test
  void relayInfoRefusesAnUnknownRelayByName() {
    try (RelayPool pool = pool()) {
      CallToolResult result =
          new RelayInfoTool(directoryOf(), new WellKnownJson())
              .call(new CallToolRequest("nostr_relay_info", Map.of("relay", "not-a-relay")));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).startsWith("INVALID_ARGUMENT"), textOf(result));
    }
  }

  // Verifies a thread is assembled from a note and the replies pointing at it, which is two
  // queries an agent would otherwise have to know how to compose itself.
  @Test
  void fetchThreadFindsANoteAndItsReplies() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    GenericEvent root = note(author, "the opening note " + System.nanoTime());
    publish(root);
    publish(replyTo(author, root, "a reply"));
    publish(replyTo(author, root, "another reply"));

    try (RelayPool pool = pool()) {
      CallToolResult result =
          new FetchThreadTool(new EventQuery(pool), QueryLimits.defaults())
              .call(new CallToolRequest("nostr_fetch_thread", Map.of("eventId", root.getId())));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertEquals(2, structuredOf(result).get("replyCount"), textOf(result));
    }
  }

  // Verifies an unknown event id is an answer rather than an error, since asking about a note
  // no configured relay carries is an ordinary thing to do.
  @Test
  void fetchThreadReportsAnUnknownNoteAsAnAnswer() {
    try (RelayPool pool = pool()) {
      CallToolResult result =
          new FetchThreadTool(new EventQuery(pool), QueryLimits.defaults())
              .call(
                  new CallToolRequest(
                      "nostr_fetch_thread",
                      Map.of("eventId", "a".repeat(64))));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertEquals(false, structuredOf(result).get("found"), textOf(result));
    }
  }

  // Verifies the escape hatch publishes a kind nothing else wraps, with its tags intact, which
  // is what keeps the server useful for NIPs it has never heard of.
  @Test
  void publishEventSendsAnArbitraryKindWithItsTags() throws Exception {
    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf("personal")) {
      CallToolResult result =
          new PublishEventTool(guard(pool, vault))
              .call(
                  new CallToolRequest(
                      "nostr_publish_event",
                      Map.of(
                          "kind", 30023,
                          "content", "a long-form article",
                          "tags", List.of(List.of("d", "my-article"), List.of("title", "Hello")))));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).startsWith("Published "), textOf(result));

      CallToolResult found =
          new nostr.mcp.tool.QueryEventsTool(new EventQuery(pool), QueryLimits.defaults(), Clock.systemUTC())
              .call(
                  new CallToolRequest(
                      "nostr_query_events",
                      Map.of("authors", List.of(vault.publicKeyOf("personal").toHexString()), "kinds", List.of(30023))));

      assertEquals(1, structuredOf(found).get("count"), textOf(found));
    }
  }

  // Verifies a tag with no name is refused, since publishing a malformed tag is not something
  // the caller can undo.
  @Test
  void publishEventRefusesAMalformedTag() {
    try (RelayPool pool = pool();
        IdentityVault vault = vaultOf("personal")) {
      CallToolResult result =
          new PublishEventTool(guard(pool, vault))
              .call(
                  new CallToolRequest(
                      "nostr_publish_event",
                      Map.of("kind", 1, "content", "x", "tags", List.of(List.of()))));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
    }
  }

  // Verifies creating an identity through the tool yields one the server can then sign with,
  // which is the whole point of letting an agent make a throwaway account.
  @Test
  void createIdentityProducesAUsableSigningIdentity() throws Exception {
    try (RelayPool pool = pool();
        IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, new InMemoryStore());

      CallToolResult created =
          new CreateIdentityTool(lifecycle)
              .call(new CallToolRequest("nostr_create_identity", Map.of("alias", "throwaway")));

      assertFalse(Boolean.TRUE.equals(created.isError()), textOf(created));
      assertTrue(textOf(created).contains("npub1"), textOf(created));

      CallToolResult published =
          new nostr.mcp.tool.PublishNoteTool(guard(pool, vault))
              .call(new CallToolRequest("nostr_publish_note", Map.of("content", "from a new key")));

      assertTrue(textOf(published).startsWith("Published "), textOf(published));
    }
  }

  // Verifies an unusable alias is refused, since aliases appear in resource URIs.
  @Test
  void createIdentityRefusesAnAliasThatWouldBreakAUri() {
    try (IdentityVault vault = emptyVault()) {
      CallToolResult result =
          new CreateIdentityTool(new IdentityLifecycle(vault, new InMemoryStore()))
              .call(new CallToolRequest("nostr_create_identity", Map.of("alias", "has spaces")));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).startsWith("INVALID_ARGUMENT"), textOf(result));
    }
  }

  // Verifies importing reads a key the server can see and never takes one as an argument,
  // which is the property that keeps key material out of the model's context.
  @Test
  void importIdentityReadsAKeyFromAFileRatherThanAnArgument() throws Exception {
    Identity existing = Identity.generateRandomIdentity();
    Path keyFile = directory.resolve("imported.key");
    Files.writeString(keyFile, existing.getPrivateKey().toHexString());

    try (IdentityVault vault = emptyVault()) {
      CallToolResult result =
          new ImportIdentityTool(new IdentityLifecycle(vault, new InMemoryStore()))
              .call(
                  new CallToolRequest(
                      "nostr_import_identity",
                      Map.of("alias", "adopted", "source", "file:" + keyFile)));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertEquals(
          existing.getPublicKey().toHexString(),
          vault.publicKeyOf("adopted").toHexString(),
          "the imported key is not the one in the file");
    }
  }

  // Verifies a key pasted as the source is refused with advice, since by then it is already in
  // the conversation.
  @Test
  void importIdentityRefusesKeyMaterialPastedAsTheSource() {
    try (IdentityVault vault = emptyVault()) {
      CallToolResult result =
          new ImportIdentityTool(new IdentityLifecycle(vault, new InMemoryStore()))
              .call(
                  new CallToolRequest(
                      "nostr_import_identity",
                      Map.of(
                          "alias", "leaked",
                          "source", Identity.generateRandomIdentity().getPrivateKey().toBech32String())));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).contains("compromised"), textOf(result));
    }
  }

  // Verifies the shred option really removes the file, so an imported key does not sit on disk
  // in plaintext afterwards.
  @Test
  void importIdentityCanShredTheSourceFile() throws Exception {
    Path keyFile = directory.resolve("shred-me.key");
    Files.writeString(keyFile, Identity.generateRandomIdentity().getPrivateKey().toHexString());

    try (IdentityVault vault = emptyVault()) {
      new ImportIdentityTool(new IdentityLifecycle(vault, new InMemoryStore()))
          .call(
              new CallToolRequest(
                  "nostr_import_identity",
                  Map.of("alias", "adopted", "source", "file:" + keyFile, "shredSource", true)));

      assertFalse(Files.exists(keyFile), "the key file survived the import");
    }
  }

  // Verifies renaming keeps the key, so the account is untouched and only the local label moves.
  @Test
  void renameIdentityKeepsTheSameKey() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, new InMemoryStore());
      String publicKey = lifecycle.create("before").publicKey();

      CallToolResult result =
          new RenameIdentityTool(lifecycle)
              .call(
                  new CallToolRequest(
                      "nostr_rename_identity", Map.of("alias", "before", "newAlias", "after")));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertEquals(publicKey, vault.publicKeyOf("after").toHexString());
      assertTrue(vault.find("before").isEmpty(), "the old alias still resolves");
    }
  }

  // Verifies renaming something absent is reported rather than silently accepted.
  @Test
  void renameIdentityReportsAnUnknownAlias() {
    try (IdentityVault vault = emptyVault()) {
      CallToolResult result =
          new RenameIdentityTool(new IdentityLifecycle(vault, new InMemoryStore()))
              .call(
                  new CallToolRequest(
                      "nostr_rename_identity", Map.of("alias", "nobody", "newAlias", "somebody")));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
    }
  }

  // Verifies changing the default changes who signs. The first identity created becomes the
  // default on its own, so what this tool is really for is moving that choice afterwards, and
  // the check that matters is which key the next note is actually signed with.
  @Test
  void setDefaultIdentityChangesWhoSigns() throws Exception {
    try (RelayPool pool = pool();
        IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, new InMemoryStore());
      lifecycle.create("first");
      lifecycle.create("second");
      assertEquals("first", vault.defaultAlias().orElseThrow(), "the first identity should default");

      CallToolResult switched =
          new SetDefaultIdentityTool(vault)
              .call(new CallToolRequest("nostr_set_default_identity", Map.of("alias", "second")));
      assertFalse(Boolean.TRUE.equals(switched.isError()), textOf(switched));

      new nostr.mcp.tool.PublishNoteTool(guard(pool, vault))
          .call(new CallToolRequest("nostr_publish_note", Map.of("content", "signed by the new default")));

      CallToolResult bySecond =
          new nostr.mcp.tool.QueryEventsTool(new EventQuery(pool), QueryLimits.defaults(), Clock.systemUTC())
              .call(
                  new CallToolRequest(
                      "nostr_query_events",
                      Map.of("authors", List.of(vault.publicKeyOf("second").toHexString()), "kinds", List.of(1))));

      assertEquals(1, structuredOf(bySecond).get("count"), "the note was not signed by the new default");
    }
  }

  // Verifies choosing a default that does not exist is refused, rather than leaving the server
  // pointing at nothing.
  @Test
  void setDefaultIdentityRefusesAnUnknownAlias() {
    try (IdentityVault vault = emptyVault()) {
      CallToolResult result =
          new SetDefaultIdentityTool(vault)
              .call(new CallToolRequest("nostr_set_default_identity", Map.of("alias", "nobody")));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(textOf(result).startsWith("IDENTITY_UNKNOWN"), textOf(result));
    }
  }

  // Verifies a backup is written to the path given and the result names the path rather than the
  // key, which is what makes taking one safe from inside a conversation.
  @Test
  void exportBackupWritesAFileAndReturnsOnlyItsPath() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, new InMemoryStore());
      lifecycle.create("personal");
      Path backup = directory.resolve("personal.p12");

      CallToolResult result =
          new ExportIdentityBackupTool(lifecycle)
              .call(
                  new CallToolRequest(
                      "nostr_export_identity_backup",
                      Map.of("alias", "personal", "path", backup.toString(), "passphrase", "secret")));

      assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
      assertTrue(Files.exists(backup), "no backup file was written");
      assertTrue(textOf(result).contains(backup.toString()), textOf(result));
      assertFalse(textOf(result).contains("nsec"), "the backup tool disclosed key material");
    }
  }

  // Verifies backing up something absent is reported rather than writing an empty file.
  @Test
  void exportBackupReportsAnUnknownIdentity() {
    try (IdentityVault vault = emptyVault()) {
      CallToolResult result =
          new ExportIdentityBackupTool(new IdentityLifecycle(vault, new InMemoryStore()))
              .call(
                  new CallToolRequest(
                      "nostr_export_identity_backup",
                      Map.of(
                          "alias", "nobody",
                          "path", directory.resolve("nobody.p12").toString(),
                          "passphrase", "secret")));

      assertTrue(Boolean.TRUE.equals(result.isError()), textOf(result));
    }
  }

  private WriteGuard guard(RelayPool pool, IdentityVault vault) {
    return new WriteGuard(
        pool, vault, WritePolicy.ALLOW, new RateLimit(100, Duration.ofMinutes(1), Clock.systemUTC()));
  }

  private RelayDirectory directoryOf() {
    return new RelayDirectory(Map.of(RelayDirectory.READ, List.of(relayUri())));
  }

  private GenericEvent note(Identity author, String content) {
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(author.getPublicKey())
            .kind(1)
            .content(content)
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    event.update();
    author.sign(event);
    return event;
  }

  private GenericEvent replyTo(Identity author, GenericEvent root, String content) {
    GenericEvent reply =
        GenericEvent.builder()
            .pubKey(author.getPublicKey())
            .kind(1)
            .content(content)
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    reply.addTag(new nostr.event.tag.GenericTag("e", List.of(root.getId(), "", "reply")));
    reply.update();
    author.sign(reply);
    return reply;
  }

  private void publish(GenericEvent event) throws Exception {
    try (RelayPool pool = pool()) {
      pool.publish(event);
    }
  }

  private IdentityVault vaultOf(String alias) {
    byte[] key =
        HexFormat.of().parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString());
    return new IdentityVault(sourceOf(Map.of(alias, key)), null);
  }

  private IdentityVault emptyVault() {
    return new IdentityVault(sourceOf(Map.of()), null);
  }

  private KeySource sourceOf(Map<String, byte[]> keys) {
    return new KeySource() {
      @Override
      public Map<String, byte[]> loadKeys(IdentityBinding binding) {
        return keys;
      }

      @Override
      public String type() {
        return "test";
      }
    };
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> structuredOf(CallToolResult result) {
    return (Map<String, Object>) result.structuredContent();
  }

  private String textOf(CallToolResult result) {
    return result.content().stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .map(TextContent::text)
        .findFirst()
        .orElse("");
  }

  private RelayPool pool() {
    return new RelayPool(List.of(relayUri()), UntestedToolsIT::connect);
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

  private static String relayUri() {
    return "ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(RELAY_PORT);
  }

  /** A store standing in for a keystore file. */
  private static final class InMemoryStore implements IdentityStore {
    private final Map<String, byte[]> keys = new LinkedHashMap<>();

    @Override
    public void store(String alias, byte[] keyMaterial) {
      if (keys.containsKey(alias)) {
        throw new KeystoreException("already holds '" + alias + "'");
      }
      keys.put(alias, keyMaterial.clone());
    }

    @Override
    public List<String> aliases() {
      return new ArrayList<>(keys.keySet());
    }

    @Override
    public boolean remove(String alias) {
      return keys.remove(alias) != null;
    }
  }
}
