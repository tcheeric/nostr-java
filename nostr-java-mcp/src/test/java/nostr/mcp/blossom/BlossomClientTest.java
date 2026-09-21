package nostr.mcp.blossom;

import nostr.mcp.tool.ToolException;
import nostr.mcp.tool.ToolFailure;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What this client sends, and what it makes of what comes back.
 *
 * <p>Run against a real server on loopback rather than a mocked client, because the claims worth
 * making here are about the wire: that the token reaches the {@code Authorization} header, that
 * a 404 is a different outcome from a 500, and that a server's {@code X-Reason} survives into
 * something the agent reads.
 */
class BlossomClientTest {

  private static final String DESCRIPTOR =
      """
      {"url":"https://cdn.example.com/b167.pdf","sha256":"b167","size":184292,\
      "type":"application/pdf","uploaded":1725105921}""";

  private final BlossomClient client = new BlossomClient();

  // Verifies the signed token actually reaches the server, and the blob's bytes with it. A
  // token built correctly but never attached fails exactly like a token built wrong.
  @Test
  void anUploadSendsTheTokenAndTheBytes() {
    try (StubHttpServer stub =
        StubHttpServer.started(exchange -> StubHttpServer.respond(exchange, 201, DESCRIPTOR))) {

      BlobDescriptor stored =
          client.upload(
              stub.baseUrl(), "hello".getBytes(StandardCharsets.UTF_8), "text/plain", "b167", "Nostr abc123");

      StubHttpServer.Request sent = stub.lastRequest();
      assertEquals("PUT", sent.method());
      assertEquals("/upload", sent.path());
      assertEquals("Nostr abc123", sent.authorization());
      assertEquals("hello", new String(sent.body(), StandardCharsets.UTF_8));
      assertEquals("https://cdn.example.com/b167.pdf", stored.url());
      assertEquals(184292, stored.size());
    }
  }

  // Verifies a refusal carries the server's own reason. "HTTP 413" alone does not tell an agent
  // whether to shrink the file or stop trying; "Max allowed size is 100MB" does.
  @Test
  void aRefusalCarriesTheServersReason() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> {
              exchange.getResponseHeaders().add("X-Reason", "File too large. Max is 100MB.");
              StubHttpServer.respond(exchange, 413, "");
            })) {

      ToolException refused =
          assertThrows(
              ToolException.class,
              () -> client.upload(stub.baseUrl(), new byte[] {1}, "image/png", "b167", "Nostr t"));

      assertEquals(ToolFailure.BLOB_SERVER_REJECTED, refused.getFailure());
      assertTrue(refused.getMessage().contains("Max is 100MB"), refused.getMessage());
    }
  }

  // Verifies a server fault is told apart from a bad request. A 5xx may be worth retrying and a
  // 4xx never is, so collapsing them into one code would have the agent retry a refusal forever.
  @Test
  void aServerFaultIsUnreachableRatherThanRejected() {
    try (StubHttpServer stub =
        StubHttpServer.started(exchange -> StubHttpServer.respond(exchange, 503, ""))) {

      ToolException failed =
          assertThrows(
              ToolException.class,
              () -> client.upload(stub.baseUrl(), new byte[] {1}, "image/png", "b167", "Nostr t"));

      assertEquals(ToolFailure.BLOB_SERVER_UNREACHABLE, failed.getFailure());
    }
  }

  // Verifies a missing blob is an answer, not an error. "This server does not have it" is the
  // normal result of looking across several servers for a blob that lives on one of them.
  @Test
  void aBlobThatIsNotThereReadsAsAbsentRatherThanFailing() {
    try (StubHttpServer stub =
        StubHttpServer.started(exchange -> StubHttpServer.respond(exchange, 404, ""))) {

      assertEquals(Optional.empty(), client.head(stub.baseUrl(), "b167"));
    }
  }

  // Verifies a present blob reports the size and type the server gave, which is the whole point
  // of asking with HEAD rather than downloading it.
  @Test
  void aPresentBlobReportsItsSizeAndType() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> StubHttpServer.respond(exchange, 200, "", "application/pdf"))) {

      BlobDescriptor found = client.head(stub.baseUrl(), "b167").orElseThrow();

      assertEquals("application/pdf", found.type());
      assertEquals(stub.baseUrl() + "/b167", found.url());
    }
  }

  // Verifies a listing parses into descriptors and is authorized, since BUD-12 requires a list
  // token even though listing is a read.
  @Test
  void aListingIsAuthorizedAndParsed() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> StubHttpServer.respond(exchange, 200, "[" + DESCRIPTOR + "]"))) {

      List<BlobDescriptor> blobs = client.list(stub.baseUrl(), "deadbeef", "Nostr listtoken");

      assertEquals(1, blobs.size());
      assertEquals("Nostr listtoken", stub.lastRequest().authorization());
      assertEquals("/list/deadbeef", stub.lastRequest().path());
    }
  }

  // Verifies deleting something that is already gone says so plainly, rather than reporting a
  // generic refusal the agent would read as "try again".
  @Test
  void deletingAMissingBlobSaysItIsNotThere() {
    try (StubHttpServer stub =
        StubHttpServer.started(exchange -> StubHttpServer.respond(exchange, 404, ""))) {

      ToolException failed =
          assertThrows(
              ToolException.class, () -> client.delete(stub.baseUrl(), "b167", "Nostr deltoken"));

      assertEquals(ToolFailure.BLOB_NOT_FOUND, failed.getFailure());
    }
  }

  // Verifies a descriptor missing its size still parses. blossom-server reports "size": 0 on
  // every upload, so a parser that treated an absent or zero size as a failure would reject a
  // real server's ordinary answer.
  @Test
  void aDescriptorWithNoUsableSizeStillParses() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange ->
                StubHttpServer.respond(
                    exchange, 201, "{\"url\":\"https://cdn.example.com/b167.txt\",\"sha256\":\"b167\",\"size\":0}"))) {

      BlobDescriptor stored =
          client.upload(stub.baseUrl(), new byte[] {1}, "text/plain", "b167", "Nostr t");

      assertEquals(0, stored.size());
      assertEquals("application/octet-stream", stored.type());
      assertEquals("https://cdn.example.com/b167.txt", stored.url());
    }
  }

  // Verifies a redirect is not followed. The server argument is a URL an agent chose, so a host
  // that passes the address check can answer 302 pointing at link-local and have this process
  // fetch it. Confirmed against the JDK: it does forward the request and return the body, though
  // it does drop the Authorization header on the way.
  @Test
  void aRedirectFromAServerIsNotFollowed() {
    try (StubHttpServer internal =
            StubHttpServer.started(
                exchange -> StubHttpServer.respond(exchange, 200, "[{\"sha256\":\"leaked\"}]"));
        StubHttpServer redirector =
            StubHttpServer.started(
                exchange -> {
                  exchange.getResponseHeaders().add("Location", "http://127.0.0.1:1/internal");
                  StubHttpServer.respond(exchange, 302, "");
                })) {

      ToolException refused =
          assertThrows(
              ToolException.class,
              () -> client.list(redirector.baseUrl(), "deadbeef", "Nostr listtoken"));

      assertEquals(ToolFailure.BLOB_SERVER_REJECTED, refused.getFailure(), refused.getMessage());
      assertEquals(0, internal.received().size(), "the redirect target was contacted");
    }
  }

  // Verifies a redirect on HEAD still reports the blob as present. BUD-01 lets a server point at
  // a CDN, and refusing to follow it must not turn "here it is, over there" into "not found".
  @Test
  void aRedirectOnHeadMeansTheBlobIsThere() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> {
              exchange.getResponseHeaders().add("Location", "https://cdn.example.com/b167");
              StubHttpServer.respond(exchange, 302, "");
            })) {

      BlobDescriptor found = client.head(stub.baseUrl(), "b167").orElseThrow();

      assertEquals(stub.baseUrl() + "/b167", found.url());
    }
  }

  // Verifies a response the JDK itself cannot parse becomes a code rather than an uncoded crash.
  // A malformed Content-Length is rejected inside HttpClient.send, before any of this class's
  // own parsing runs, so guarding only the Long.parseLong would have missed it entirely.
  @Test
  void aResponseTheClientCannotParseBecomesACode() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> {
              exchange.getResponseHeaders().add("Content-Length", "not-a-number");
              StubHttpServer.respond(exchange, 200, "", "image/png");
            })) {

      ToolException refused =
          assertThrows(ToolException.class, () -> client.head(stub.baseUrl(), "b167"));

      assertEquals(ToolFailure.BLOB_SERVER_REJECTED, refused.getFailure(), refused.getMessage());
    }
  }

  // Verifies a listing that is not an array is a diagnosable failure. Iterating a JSON object
  // yields its values, silently turning an error envelope into a list of empty descriptors.
  @Test
  void aListingThatIsNotAnArrayIsRefused() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> StubHttpServer.respond(exchange, 200, "{\"message\":\"nope\"}"))) {

      ToolException refused =
          assertThrows(
              ToolException.class, () -> client.list(stub.baseUrl(), "deadbeef", "Nostr t"));

      assertEquals(ToolFailure.BLOB_SERVER_REJECTED, refused.getFailure());
    }
  }

  // Verifies a 204 counts as deleted. BUD-12 allows both 200 and 204, and treating the empty
  // one as a failure would have the agent retry a delete that already happened.
  @Test
  void aDeleteWithNoBodySucceeds() {
    try (StubHttpServer stub =
        StubHttpServer.started(exchange -> StubHttpServer.respond(exchange, 204, ""))) {

      client.delete(stub.baseUrl(), "b167", "Nostr deltoken");

      assertEquals("DELETE", stub.lastRequest().method());
      assertEquals("/b167", stub.lastRequest().path());
    }
  }
}
