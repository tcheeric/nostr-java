package nostr.mcp.integration;

import com.sun.net.httpserver.HttpServer;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import nostr.id.Identity;
import nostr.mcp.blossom.BlobSource;
import nostr.mcp.blossom.BlossomAuth;
import nostr.mcp.blossom.BlossomClient;
import nostr.mcp.blossom.BlossomServers;
import nostr.mcp.blossom.BlossomVerb;
import nostr.mcp.blossom.PublicHttpUrl;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.identity.SigningAlias;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.tool.BlossomDeleteTool;
import nostr.mcp.tool.BlossomGetBlobTool;
import nostr.mcp.tool.BlossomListTool;
import nostr.mcp.tool.BlossomServerListTool;
import nostr.mcp.tool.BlossomSetServersTool;
import nostr.mcp.tool.BlossomUploadTool;
import nostr.mcp.tool.ToolException;
import nostr.mcp.tool.ToolFailure;
import nostr.mcp.write.RateLimit;
import nostr.mcp.write.WriteGuard;
import nostr.mcp.write.WritePolicy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the Blossom tools against a real Blossom server and a real relay.
 *
 * <p>The claim worth testing is interoperability, and nothing below the wire can make it. The
 * server is configured to require authorization on both upload and list, so every assertion here
 * also asserts that the kind-24242 token this module builds — its tags, its expiry, and in
 * particular its base64url encoding — is one an independent implementation accepts. A stub we
 * wrote would accept whatever we sent and prove none of that.
 *
 * <p>Private addresses are allowed because Testcontainers publishes on loopback. That is the
 * configuration a self-hosted deployment uses; the refusals the default configuration makes are
 * covered by {@code PublicHttpUrlTest}.
 */
@Testcontainers
class BlossomToolsIT {

  private static final String BLOB = "a small blob of media, uploaded by a test";
  private static final String BLOB_TYPE = "text/plain";

  @Container
  private static final GenericContainer<?> BLOSSOM =
      new GenericContainer<>(DockerImageName.parse("ghcr.io/hzrd149/blossom-server:4.4.1"))
          .withCopyFileToContainer(
              MountableFile.forClasspathResource("blossom-server-config.yml"), "/app/config.yml")
          .withExposedPorts(3000)
          .withStartupAttempts(3)
          .waitingFor(Wait.forHttp("/").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(60)));

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13"))
          .withExposedPorts(8080)
          .withStartupAttempts(5)
          .waitingFor(new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  private static HttpServer mediaHost;

  /**
   * Serves the blob the upload tool is asked to fetch, since it takes a URL rather than a file.
   *
   * <p>The body is derived from the path so that two tests asking for different paths get
   * different blobs. Blossom addresses by hash and the container is shared across this class: if
   * every test uploaded identical bytes they would all own one blob, and a test deleting "its"
   * blob would find it still there, owned by another test. That failure is order-dependent and
   * looks exactly like a bug in the delete path.
   */
  @BeforeAll
  static void startMediaHost() throws IOException {
    mediaHost = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    mediaHost.createContext(
        "/media",
        exchange -> {
          byte[] bytes = blobFor(exchange.getRequestURI().getPath());
          exchange.getResponseHeaders().add("Content-Type", BLOB_TYPE);
          exchange.sendResponseHeaders(200, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });
    mediaHost.start();
  }

  /** The bytes served at a path: distinct per path, and known to the test so it can hash them. */
  private static byte[] blobFor(String path) {
    return (BLOB + " [" + path + "]").getBytes(StandardCharsets.UTF_8);
  }

  @AfterAll
  static void stopMediaHost() {
    mediaHost.stop(0);
  }

  // Verifies the whole round trip against a server that demands a valid token at every step:
  // upload from a URL, find the blob, see it listed, delete it, and find it gone. Run as one
  // test because the steps share a blob, and asserting the delete means having uploaded it.
  @Test
  void mediaCanBeUploadedFoundListedAndDeleted() {
    try (IdentityVault vault = vaultOf("personal");
        RelayPool pool = pool()) {
      WriteGuard writeGuard = guard(pool, vault, WritePolicy.ALLOW);
      BlossomServers servers = servers();
      BlossomClient client = new BlossomClient();
      BlossomAuth auth = new BlossomAuth(Clock.systemUTC());

      CallToolResult uploaded =
          new BlossomUploadTool(servers, client, blobSource(), auth, writeGuard)
              .call(
                  new CallToolRequest(
                      "nostr_blossom_upload", Map.of("sourceUrl", mediaUrl("round-trip"))));

      assertFalse(Boolean.TRUE.equals(uploaded.isError()), textOf(uploaded));
      String sha256 = String.valueOf(structuredOf(uploaded).get("sha256"));
      assertTrue(
          Boolean.TRUE.equals(structuredOf(uploaded).get("hashMatches")),
          "the server stored something other than what was sent: " + textOf(uploaded));
      assertTrue(String.valueOf(structuredOf(uploaded).get("url")).contains(sha256), textOf(uploaded));

      // The upload reports the size we sent, because blossom-server answers every upload with
      // "size": 0 and an agent told its own file is empty would draw the wrong conclusion.
      assertEquals(
          (long) blobFor("/media/round-trip").length,
          Long.parseLong(String.valueOf(structuredOf(uploaded).get("size"))),
          textOf(uploaded));

      CallToolResult found =
          new BlossomGetBlobTool(servers, client)
              .call(new CallToolRequest("nostr_blossom_get", Map.of("sha256", sha256)));

      // Deliberately not asserting a size here: this server sends no Content-Length on a HEAD,
      // so the honest answer is that the blob is there and this is its URL.
      assertFalse(Boolean.TRUE.equals(found.isError()), textOf(found));
      assertTrue(String.valueOf(structuredOf(found).get("url")).contains(sha256), textOf(found));

      CallToolResult listed =
          new BlossomListTool(servers, client, auth, vault)
              .call(new CallToolRequest("nostr_blossom_list", Map.of()));

      assertFalse(Boolean.TRUE.equals(listed.isError()), textOf(listed));
      assertTrue(listed.structuredContent().toString().contains(sha256), textOf(listed));

      CallToolResult deleted =
          new BlossomDeleteTool(servers, client, auth, writeGuard)
              .call(new CallToolRequest("nostr_blossom_delete", Map.of("sha256", sha256)));

      assertFalse(Boolean.TRUE.equals(deleted.isError()), textOf(deleted));

      CallToolResult gone =
          new BlossomGetBlobTool(servers, client)
              .call(new CallToolRequest("nostr_blossom_get", Map.of("sha256", sha256)));

      assertTrue(Boolean.TRUE.equals(gone.isError()), "the blob survived the delete: " + textOf(gone));
      assertTrue(textOf(gone).startsWith("BLOB_NOT_FOUND"), textOf(gone));
    }
  }

  // Verifies a delete under a confirming policy does nothing until the token comes back, which
  // is the whole point of the two-step: a hallucinated deletion is a no-op.
  @Test
  void anUnconfirmedDeleteRemovesNothing() {
    try (IdentityVault vault = vaultOf("personal");
        RelayPool pool = pool()) {
      BlossomServers servers = servers();
      BlossomClient client = new BlossomClient();
      BlossomAuth auth = new BlossomAuth(Clock.systemUTC());

      String sha256 =
          String.valueOf(
              structuredOf(
                      new BlossomUploadTool(
                              servers,
                              client,
                              blobSource(),
                              auth,
                              guard(pool, vault, WritePolicy.ALLOW))
                          .call(
                              new CallToolRequest(
                                  "nostr_blossom_upload", Map.of("sourceUrl", mediaUrl("unconfirmed-delete")))))
                  .get("sha256"));

      CallToolResult preview =
          new BlossomDeleteTool(servers, client, auth, guard(pool, vault, WritePolicy.CONFIRM))
              .call(new CallToolRequest("nostr_blossom_delete", Map.of("sha256", sha256)));

      assertTrue(textOf(preview).contains("Nothing has been deleted yet"), textOf(preview));
      assertTrue(
          new BlossomGetBlobTool(servers, client)
              .call(new CallToolRequest("nostr_blossom_get", Map.of("sha256", sha256)))
              .structuredContent()
              .toString()
              .contains(sha256),
          "the preview deleted the blob");
    }
  }

  // Verifies a server list makes the round trip through a real relay: published as kind 10063
  // and read back in the order it was written, which BUD-03 makes meaningful.
  @Test
  void aServerListCanBePublishedAndReadBack() {
    try (IdentityVault vault = vaultOf("personal");
        RelayPool pool = pool()) {
      List<String> published = List.of("https://cdn.example.com", "https://backup.example.com");

      CallToolResult set =
          new BlossomSetServersTool(guard(pool, vault, WritePolicy.ALLOW), servers())
              .call(new CallToolRequest("nostr_blossom_set_servers", Map.of("servers", published)));

      assertFalse(Boolean.TRUE.equals(set.isError()), textOf(set));

      CallToolResult read =
          new BlossomServerListTool(new EventQuery(pool), vault, QueryLimits.defaults())
              .call(new CallToolRequest("nostr_blossom_get_servers", Map.of()));

      assertFalse(Boolean.TRUE.equals(read.isError()), textOf(read));
      assertEquals(published, structuredOf(read).get("servers"), textOf(read));
    }
  }

  // Verifies an upload is refused where writing is denied. The tool is not registered at all on
  // such a server, but the guard is what makes that true rather than a registration oversight.
  @Test
  void aReadOnlyServerWillNotUpload() {
    try (IdentityVault vault = vaultOf("personal");
        RelayPool pool = pool()) {
      CallToolResult refused =
          new BlossomUploadTool(
                  servers(),
                  new BlossomClient(),
                  blobSource(),
                  new BlossomAuth(Clock.systemUTC()),
                  guard(pool, vault, WritePolicy.DENY))
              .call(new CallToolRequest("nostr_blossom_upload", Map.of("sourceUrl", mediaUrl("read-only"))));

      assertTrue(Boolean.TRUE.equals(refused.isError()), textOf(refused));
      assertTrue(textOf(refused).startsWith("WRITE_FORBIDDEN"), textOf(refused));
    }
  }

  // --- Negative controls -------------------------------------------------------------------
  //
  // Everything above proves a correct token is accepted. On its own that is weak evidence: a
  // server that ignored authorization entirely would pass every one of those tests. These four
  // deliberately break one field each and require the server to notice, which is what makes the
  // passing cases mean something.

  // Verifies the server enforces the x tag. The token commits to one hash and the body is a
  // different blob, which is exactly the substitution an attacker who captured a token would
  // attempt.
  @Test
  void aTokenCommittingToADifferentBlobIsRefused() {
    try (IdentityVault vault = vaultOf("personal")) {
      byte[] body = "the blob actually sent".getBytes(StandardCharsets.UTF_8);
      String otherHash = sha256Of("a completely different blob");

      ToolException refused =
          assertThrows(
              ToolException.class,
              () ->
                  new BlossomClient()
                      .upload(
                          blossomUri(),
                          body,
                          "text/plain",
                          sha256Of(new String(body, StandardCharsets.UTF_8)),
                          headerFor(vault, BlossomVerb.UPLOAD, Optional.of(otherHash), Clock.systemUTC())));

      assertEquals(ToolFailure.BLOB_SERVER_REJECTED, refused.getFailure(), refused.getMessage());
    }
  }

  // Verifies the server enforces the expiration tag, and therefore that ours is a NIP-40 tag it
  // can read. A token that never expired would be a password that leaked permanently.
  @Test
  void anExpiredTokenIsRefused() {
    try (IdentityVault vault = vaultOf("personal")) {
      byte[] body = "blob behind an expired token".getBytes(StandardCharsets.UTF_8);
      Clock longAgo = Clock.fixed(Instant.now().minus(Duration.ofDays(2)), ZoneOffset.UTC);

      ToolException refused =
          assertThrows(
              ToolException.class,
              () ->
                  new BlossomClient()
                      .upload(
                          blossomUri(),
                          body,
                          "text/plain",
                          sha256Of(new String(body, StandardCharsets.UTF_8)),
                          headerFor(
                              vault,
                              BlossomVerb.UPLOAD,
                              Optional.of(sha256Of(new String(body, StandardCharsets.UTF_8))),
                              longAgo)));

      assertEquals(ToolFailure.BLOB_SERVER_REJECTED, refused.getFailure(), refused.getMessage());
    }
  }

  // Verifies the server enforces the t tag, and so that a token minted to read cannot be
  // replayed to write. Without this check a list token would be an upload token.
  @Test
  void aListTokenCannotBeUsedToUpload() {
    try (IdentityVault vault = vaultOf("personal")) {
      byte[] body = "blob behind a list token".getBytes(StandardCharsets.UTF_8);
      String sha256 = sha256Of(new String(body, StandardCharsets.UTF_8));

      ToolException refused =
          assertThrows(
              ToolException.class,
              () ->
                  new BlossomClient()
                      .upload(
                          blossomUri(),
                          body,
                          "text/plain",
                          sha256,
                          headerFor(vault, BlossomVerb.LIST, Optional.of(sha256), Clock.systemUTC())));

      assertEquals(ToolFailure.BLOB_SERVER_REJECTED, refused.getFailure(), refused.getMessage());
    }
  }

  // Verifies a request carrying no usable token is refused. Asserts only that it fails, not
  // which code: blossom-server answers an unparseable Authorization header with 500 rather than
  // the 401 BUD-02 lists, and this module's own code cannot produce such a header anyway, so
  // pinning the mapping here would pin someone else's bug.
  @Test
  void aRequestWithoutAUsableTokenIsRefused() {
    byte[] body = "blob with no token".getBytes(StandardCharsets.UTF_8);

    ToolException refused =
        assertThrows(
            ToolException.class,
            () ->
                new BlossomClient()
                    .upload(
                        blossomUri(),
                        body,
                        "text/plain",
                        sha256Of(new String(body, StandardCharsets.UTF_8)),
                        "Nostr bm90LWEtdG9rZW4"));

    assertTrue(
        refused.getFailure() == ToolFailure.BLOB_SERVER_REJECTED
            || refused.getFailure() == ToolFailure.BLOB_SERVER_UNREACHABLE,
        "an unusable token was not refused: " + refused.getMessage());
  }

  // --- Behaviour that only a real server shows ---------------------------------------------

  // Verifies re-uploading a blob the server already holds succeeds rather than erroring. BUD-02
  // allows 200 or 201 here, and treating the second one as a failure would make an agent's retry
  // of a request that already worked look like a problem.
  @Test
  void uploadingTheSameMediaTwiceSucceedsBothTimes() {
    try (IdentityVault vault = vaultOf("personal");
        RelayPool pool = pool()) {
      BlossomUploadTool tool =
          new BlossomUploadTool(
              servers(),
              new BlossomClient(),
              blobSource(),
              new BlossomAuth(Clock.systemUTC()),
              guard(pool, vault, WritePolicy.ALLOW));
      CallToolRequest request =
          new CallToolRequest("nostr_blossom_upload", Map.of("sourceUrl", mediaUrl("twice")));

      CallToolResult first = tool.call(request);
      CallToolResult second = tool.call(request);

      assertFalse(Boolean.TRUE.equals(first.isError()), textOf(first));
      assertFalse(Boolean.TRUE.equals(second.isError()), textOf(second));
      assertEquals(
          structuredOf(first).get("sha256"),
          structuredOf(second).get("sha256"),
          "the same bytes produced two different hashes");
    }
  }

  // Verifies the size cap stops a blob before it is ever offered to the server, since the bytes
  // are buffered in this process to be hashed and the cap is what bounds that.
  @Test
  void aBlobOverTheCapNeverReachesTheServer() {
    try (IdentityVault vault = vaultOf("personal");
        RelayPool pool = pool()) {
      CallToolResult refused =
          new BlossomUploadTool(
                  servers(),
                  new BlossomClient(),
                  new BlobSource(new PublicHttpUrl(true), 8),
                  new BlossomAuth(Clock.systemUTC()),
                  guard(pool, vault, WritePolicy.ALLOW))
              .call(new CallToolRequest("nostr_blossom_upload", Map.of("sourceUrl", mediaUrl("oversize"))));

      assertTrue(Boolean.TRUE.equals(refused.isError()), textOf(refused));
      assertTrue(textOf(refused).startsWith("INVALID_ARGUMENT"), textOf(refused));
      assertTrue(textOf(refused).contains("larger than"), textOf(refused));
    }
  }

  // Verifies the address guard is what actually stops the upload, at its default setting,
  // against a live server. Every other test here runs with it off because Testcontainers
  // publishes on loopback, so without this case the default configuration is never exercised.
  @Test
  void theDefaultGuardRefusesMediaOnThisMachine() {
    try (IdentityVault vault = vaultOf("personal");
        RelayPool pool = pool()) {
      CallToolResult refused =
          new BlossomUploadTool(
                  servers(),
                  new BlossomClient(),
                  new BlobSource(new PublicHttpUrl(false), 1024 * 1024),
                  new BlossomAuth(Clock.systemUTC()),
                  guard(pool, vault, WritePolicy.ALLOW))
              .call(new CallToolRequest("nostr_blossom_upload", Map.of("sourceUrl", mediaUrl("guarded"))));

      assertTrue(Boolean.TRUE.equals(refused.isError()), textOf(refused));
      assertTrue(textOf(refused).contains("not on the public internet"), textOf(refused));
    }
  }

  /**
   * Builds an authorization header directly, so a test can make one that is deliberately wrong.
   *
   * <p>The tools always build a correct token, which is what makes them unusable for a negative
   * control: proving the server rejects a bad token means being able to make one.
   */
  private String headerFor(
      IdentityVault vault, BlossomVerb verb, Optional<String> blobHash, Clock clock) {
    BlossomAuth auth = new BlossomAuth(clock);
    return auth.headerValue(
        SigningAlias.sign(vault, "personal", auth.tokenFor(verb, blobHash)));
  }

  private String sha256Of(String text) {
    try {
      return nostr.util.NostrUtil.bytesToHex(
          nostr.util.NostrUtil.sha256(text.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  private BlossomServers servers() {
    return new BlossomServers(List.of(blossomUri()), new PublicHttpUrl(true));
  }

  private BlobSource blobSource() {
    return new BlobSource(new PublicHttpUrl(true), 1024 * 1024);
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
    return new RelayPool(List.of(relayUri()), BlossomToolsIT::connect);
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

  /**
   * A URL serving media unique to one test, so no two tests share a blob hash.
   *
   * @param name what to make unique, normally the test's own name
   */
  private static String mediaUrl(String name) {
    return "http://127.0.0.1:" + mediaHost.getAddress().getPort() + "/media/" + name;
  }

  private static String blossomUri() {
    return "http://" + BLOSSOM.getHost() + ":" + BLOSSOM.getMappedPort(3000);
  }

  private static String relayUri() {
    return "ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(8080);
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
}
