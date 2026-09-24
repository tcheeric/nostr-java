package nostr.mcp.blossom;

import nostr.mcp.tool.ToolException;
import nostr.mcp.tool.ToolFailure;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bounds on fetching something an agent named.
 *
 * <p>These tests use the permissive guard, because the stub server is on loopback. The refusals
 * the strict guard makes are covered by {@link PublicHttpUrlTest}; what is left to prove here is
 * the size cap and the redirect refusal, which are the two ways a URL that passes the address
 * check can still cause trouble.
 */
class BlobSourceTest {

  private static final long CAP = 64;

  private final BlobSource source = new BlobSource(new PublicHttpUrl(true), CAP);

  // Verifies a blob is fetched with its type and hashed, since the hash is what the upload
  // token must commit to before the bytes can be sent anywhere.
  @Test
  void aFetchedBlobCarriesItsTypeAndItsHash() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> StubHttpServer.respond(exchange, 200, "hello", "image/png"))) {

      BlobSource.Blob blob = source.fetch(stub.baseUrl() + "/photo.png");

      assertEquals("hello", new String(blob.bytes(), StandardCharsets.UTF_8));
      assertEquals("image/png", blob.contentType());
      assertEquals(
          "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", blob.sha256());
    }
  }

  // Verifies the charset parameter is dropped. "text/plain; charset=utf-8" as a stored MIME type
  // makes a blob's type differ from the same blob uploaded elsewhere.
  @Test
  void aContentTypeLosesItsParameters() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> StubHttpServer.respond(exchange, 200, "hi", "text/plain; charset=utf-8"))) {

      assertEquals("text/plain", source.fetch(stub.baseUrl() + "/note.txt").contentType());
    }
  }

  // Verifies the cap holds. Bytes are buffered in memory to compute the hash, so without this
  // an agent could name a URL that serves gigabytes and take the server down.
  @Test
  void aBlobLargerThanTheCapIsRefused() {
    String tooBig = "x".repeat((int) CAP + 1);
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> StubHttpServer.respond(exchange, 200, tooBig, "text/plain"))) {

      ToolException refused =
          assertThrows(ToolException.class, () -> source.fetch(stub.baseUrl() + "/big.txt"));

      assertEquals(ToolFailure.INVALID_ARGUMENT, refused.getFailure());
      assertTrue(refused.getMessage().contains("larger than"), refused.getMessage());
    }
  }

  // Verifies a blob exactly at the cap is allowed, because an off-by-one here silently refuses
  // the largest file the operator said was fine.
  @Test
  void aBlobExactlyAtTheCapIsAllowed() {
    String exact = "x".repeat((int) CAP);
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> StubHttpServer.respond(exchange, 200, exact, "text/plain"))) {

      assertEquals(CAP, source.fetch(stub.baseUrl() + "/exact.txt").size());
    }
  }

  // Verifies a redirect is refused rather than followed. A redirect is the simplest way past
  // the address check: the named URL resolves publicly and then points at link-local.
  @Test
  void aRedirectIsRefusedRatherThanFollowed() {
    try (StubHttpServer stub =
        StubHttpServer.started(
            exchange -> {
              exchange.getResponseHeaders().add("Location", "http://169.254.169.254/");
              StubHttpServer.respond(exchange, 302, "");
            })) {

      ToolException refused =
          assertThrows(ToolException.class, () -> source.fetch(stub.baseUrl() + "/sneaky"));

      assertEquals(ToolFailure.INVALID_ARGUMENT, refused.getFailure());
      assertTrue(refused.getMessage().contains("169.254.169.254"), refused.getMessage());
    }
  }

  // Verifies an empty response is refused. Uploading zero bytes would store a blob whose hash
  // is the hash of nothing, on every server, forever.
  @Test
  void anEmptyResponseIsRefused() {
    try (StubHttpServer stub =
        StubHttpServer.started(exchange -> StubHttpServer.respond(exchange, 200, ""))) {

      ToolException refused =
          assertThrows(ToolException.class, () -> source.fetch(stub.baseUrl() + "/empty"));

      assertTrue(refused.getMessage().contains("no content"), refused.getMessage());
    }
  }

  // Verifies a source that answers with an error is reported as unreachable rather than
  // uploaded as if the error page were the media.
  @Test
  void aSourceThatErrorsIsNotUploaded() {
    try (StubHttpServer stub =
        StubHttpServer.started(exchange -> StubHttpServer.respond(exchange, 404, "nope"))) {

      ToolException failed =
          assertThrows(ToolException.class, () -> source.fetch(stub.baseUrl() + "/missing"));

      assertEquals(ToolFailure.BLOB_SERVER_UNREACHABLE, failed.getFailure());
    }
  }
}
