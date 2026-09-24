package nostr.mcp.blossom;

import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;
import nostr.util.NostrUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * Fetches the bytes an agent asked to have uploaded.
 *
 * <p>This is the one place in the module where the server retrieves something an agent named, so
 * it is where the request stops being a message and starts being an action taken on the server's
 * own network. Two things bound it: {@link PublicHttpUrl} decides where it may go, and a byte
 * cap decides how much it will hold.
 *
 * <p>The bytes are buffered rather than streamed because BUD-11 requires the blob's hash in the
 * upload token, and the hash cannot be known until the last byte has been read. The cap is what
 * makes buffering safe.
 */
public final class BlobSource {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);
  private static final String FALLBACK_TYPE = "application/octet-stream";

  private final HttpClient httpClient;
  private final PublicHttpUrl publicHttpUrl;
  private final long maxBytes;

  /**
   * @param publicHttpUrl the guard deciding which URLs may be fetched
   * @param maxBytes the largest blob this server will carry
   */
  public BlobSource(@NonNull PublicHttpUrl publicHttpUrl, long maxBytes) {
    this(
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(TIMEOUT)
            .build(),
        publicHttpUrl,
        maxBytes);
  }

  /**
   * @param httpClient the client to fetch with, so a test need not reach the network
   * @param publicHttpUrl the guard deciding which URLs may be fetched
   * @param maxBytes the largest blob this server will carry
   */
  public BlobSource(
      @NonNull HttpClient httpClient, @NonNull PublicHttpUrl publicHttpUrl, long maxBytes) {
    this.httpClient = httpClient;
    this.publicHttpUrl = publicHttpUrl;
    this.maxBytes = maxBytes;
  }

  /**
   * Fetch a blob by URL.
   *
   * @param sourceUrl where the media currently lives
   * @return the bytes, their type and their hash
   * @throws nostr.mcp.tool.ToolException when the URL is refused, unreachable, or too large
   */
  public Blob fetch(@NonNull String sourceUrl) {
    URI uri = publicHttpUrl.require("sourceUrl", sourceUrl);
    HttpRequest request = HttpRequest.newBuilder(uri).timeout(TIMEOUT).GET().build();
    try {
      HttpResponse<InputStream> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      // The body is closed on every path, not only the one that reads it: a refused redirect
      // and a non-2xx both throw before readCapped opens it, and an unclosed stream holds its
      // connection until garbage collection.
      try (InputStream body = response.body()) {
        refuseRedirect(response, sourceUrl);
        requireSuccess(response, sourceUrl);
        byte[] bytes = readCapped(body, sourceUrl);
        return new Blob(bytes, contentTypeOf(response), hashOf(bytes));
      }
    } catch (IOException e) {
      throw ToolFailure.BLOB_SERVER_UNREACHABLE.raise(
          "Could not fetch " + sourceUrl + ": " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw ToolFailure.TIMEOUT.raise("Interrupted while fetching " + sourceUrl);
    }
  }

  /**
   * Refuses to follow a redirect, rather than following it somewhere that was never checked.
   *
   * <p>A redirect is the simplest way past an address check: the URL the agent gives resolves
   * publicly, and the server it reaches answers {@code 302 Location: http://169.254.169.254/}.
   * Following it safely would mean re-running the guard at every hop, so the agent is handed the
   * destination and can pass it back if it is a URL they actually meant.
   */
  private void refuseRedirect(HttpResponse<InputStream> response, String sourceUrl) {
    int status = response.statusCode();
    if (status < 300 || status >= 400) {
      return;
    }
    String location = response.headers().firstValue("Location").orElse("(none given)");
    throw ToolFailure.INVALID_ARGUMENT.raise(
        sourceUrl
            + " redirects to "
            + location
            + ", and redirects are not followed because the destination was never checked. Pass"
            + " the final URL instead.");
  }

  private void requireSuccess(HttpResponse<InputStream> response, String sourceUrl) {
    int status = response.statusCode();
    if (status < 200 || status >= 300) {
      throw ToolFailure.BLOB_SERVER_UNREACHABLE.raise(
          sourceUrl + " answered with HTTP " + status);
    }
  }

  /**
   * Reads the body, stopping one byte past the cap.
   *
   * <p>Reading one byte more than is allowed is what distinguishes "exactly at the limit" from
   * "over it" without trusting {@code Content-Length}, which a server is free to understate.
   */
  private byte[] readCapped(InputStream body, String sourceUrl) throws IOException {
    {
      byte[] bytes = body.readNBytes(oneMoreThanTheCap());
      if (bytes.length > maxBytes) {
        throw ToolFailure.INVALID_ARGUMENT.raise(
            sourceUrl
                + " is larger than this server will carry ("
                + maxBytes
                + " bytes). Raise nostr.mcp.blossom.max-blob-bytes to allow more.");
      }
      if (bytes.length == 0) {
        throw ToolFailure.INVALID_ARGUMENT.raise(sourceUrl + " returned no content");
      }
      return bytes;
    }
  }

  /**
   * How many bytes to ask for: one past the cap, without overflowing.
   *
   * <p>Reading one more than is allowed is what tells "exactly at the limit" from "over it".
   * Computing it as {@code maxBytes + 1} wraps negative at {@code Long.MAX_VALUE}, and casting
   * a multi-gigabyte cap to {@code int} truncates it silently, so both ends are clamped here.
   */
  private int oneMoreThanTheCap() {
    return (int) Math.min(maxBytes, Integer.MAX_VALUE - 1L) + 1;
  }

  private String contentTypeOf(HttpResponse<InputStream> response) {
    return response
        .headers()
        .firstValue("Content-Type")
        .map(type -> type.split(";")[0].trim().toLowerCase(Locale.ROOT))
        .filter(type -> !type.isEmpty())
        .orElse(FALLBACK_TYPE);
  }

  private String hashOf(byte[] bytes) {
    try {
      return NostrUtil.bytesToHex(NostrUtil.sha256(bytes));
    } catch (NoSuchAlgorithmException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise("This JVM cannot compute SHA-256: " + e.getMessage());
    }
  }

  /**
   * A blob held in memory, on its way to a server.
   *
   * @param bytes the blob itself
   * @param contentType its MIME type, as the source served it
   * @param sha256 its hash, lowercase hex
   */
  public record Blob(byte[] bytes, String contentType, String sha256) {

    /**
     * @return how many bytes the blob is
     */
    public int size() {
      return bytes.length;
    }

    /**
     * @return the type, never null
     */
    public String contentTypeOrDefault() {
      return Optional.ofNullable(contentType).filter(type -> !type.isBlank()).orElse(FALLBACK_TYPE);
    }
  }
}
