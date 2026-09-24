package nostr.mcp.blossom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Talks to a Blossom server over HTTP.
 *
 * <p>Separate from {@link nostr.mcp.directory.WellKnownJson}, which serves the same purpose for
 * NIP-05 and NIP-11, because the two have almost nothing in common at the HTTP level: that one
 * is GET-only, String-bodied and always JSON, and Blossom needs PUT with a binary body, HEAD for
 * existence, DELETE, and an {@code Authorization} header on most of it.
 *
 * <p>Every failure becomes a {@link ToolFailure} rather than an {@code IOException}, and a
 * refusal carries the server's {@code X-Reason} where it sent one. BUD-01 is explicit that
 * {@code X-Reason} is for a human to read and must never be parsed for control flow, so it is
 * passed through as prose and nothing here branches on it.
 */
public final class BlossomClient {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String AUTHORIZATION = "Authorization";
  private static final String REASON_HEADER = "X-Reason";
  private static final String UPLOAD_HASH_HEADER = "X-SHA-256";

  private final HttpClient httpClient;

  /**
   * Uses a client that does not follow redirects.
   *
   * <p>BUD-01 lets a server redirect blob retrieval to a CDN, and following that would be
   * convenient. It is also the way past {@link PublicHttpUrl}: the {@code server} argument is a
   * URL an agent chose, so a host that passes the address check can answer {@code 302 Location:
   * http://169.254.169.254/} and have this process fetch it and hand the result back. The JDK
   * drops the {@code Authorization} header across a redirect, so the token does not leak, but
   * the response body does.
   *
   * <p>Nothing is lost by refusing. A redirect on HEAD still means the blob is there, which is
   * the only question that endpoint asks, and the authorized endpoints do not redirect.
   */
  public BlossomClient() {
    this(
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(TIMEOUT)
            .build());
  }

  /**
   * @param httpClient the client to call with, so a test need not reach the network
   */
  public BlossomClient(@NonNull HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Store a blob.
   *
   * @param server the server's base URL
   * @param blob the bytes to store
   * @param contentType the blob's MIME type
   * @param sha256 the blob's hash, which the server may check before reading the body
   * @param authorization the signed upload token
   * @return what the server says it stored
   * @throws nostr.mcp.tool.ToolException when the server could not be reached or refused
   */
  public BlobDescriptor upload(
      @NonNull String server,
      byte @NonNull [] blob,
      @NonNull String contentType,
      @NonNull String sha256,
      @NonNull String authorization) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(server + "/upload"))
            .header(AUTHORIZATION, authorization)
            .header("Content-Type", contentType)
            .header(UPLOAD_HASH_HEADER, sha256)
            .timeout(TIMEOUT)
            .PUT(BodyPublishers.ofByteArray(blob))
            .build();
    HttpResponse<String> response = send(request, HttpResponse.BodyHandlers.ofString(), server);
    requireSuccess(response, server, "store the blob");
    return BlobDescriptor.from(parse(response.body(), server));
  }

  /**
   * Ask whether a server holds a blob, without downloading it.
   *
   * @param server the server's base URL
   * @param sha256 the blob's hash
   * @return the blob's size and type, or empty when the server does not hold it
   * @throws nostr.mcp.tool.ToolException when the server could not be reached
   */
  public Optional<BlobDescriptor> head(@NonNull String server, @NonNull String sha256) {
    String url = BlossomServers.blobUrl(server, sha256);
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(TIMEOUT)
            .method("HEAD", BodyPublishers.noBody())
            .build();
    HttpResponse<Void> response = send(request, HttpResponse.BodyHandlers.discarding(), server);
    if (response.statusCode() == 404 || response.statusCode() == 410) {
      return Optional.empty();
    }
    // A redirect is the server pointing at a CDN, which answers the only question HEAD asks:
    // the blob exists. The hash-addressed URL is still the one to hand back, since BUD-01
    // requires the redirect target to carry the same hash anyway.
    if (!isRedirect(response.statusCode())) {
      requireSuccess(response, server, "look up the blob");
    }
    return Optional.of(
        new BlobDescriptor(
            url,
            sha256,
            contentLengthOf(response),
            header(response, "Content-Type").orElse("application/octet-stream"),
            0));
  }

  /**
   * List the blobs a public key has stored on a server.
   *
   * @param server the server's base URL
   * @param pubkeyHex whose blobs to list
   * @param authorization the signed list token
   * @return the descriptors the server returned
   * @throws nostr.mcp.tool.ToolException when the server could not be reached or refused
   */
  public List<BlobDescriptor> list(
      @NonNull String server, @NonNull String pubkeyHex, @NonNull String authorization) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(server + "/list/" + pubkeyHex))
            .header(AUTHORIZATION, authorization)
            .header("Accept", "application/json")
            .timeout(TIMEOUT)
            .GET()
            .build();
    HttpResponse<String> response = send(request, HttpResponse.BodyHandlers.ofString(), server);
    requireSuccess(response, server, "list blobs");
    JsonNode body = parse(response.body(), server);
    if (!body.isArray()) {
      // Iterating a JSON object yields its values, which would turn an error envelope into a
      // list of empty descriptors rather than something anyone could diagnose.
      throw ToolFailure.BLOB_SERVER_REJECTED.raise(
          server + " answered a listing with " + body.getNodeType() + " where an array was due");
    }
    List<BlobDescriptor> descriptors = new ArrayList<>();
    body.forEach(node -> descriptors.add(BlobDescriptor.from(node)));
    return List.copyOf(descriptors);
  }

  /**
   * Remove a blob from a server.
   *
   * @param server the server's base URL
   * @param sha256 the blob's hash
   * @param authorization the signed delete token
   * @throws nostr.mcp.tool.ToolException when the server could not be reached or refused
   */
  public void delete(
      @NonNull String server, @NonNull String sha256, @NonNull String authorization) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(BlossomServers.blobUrl(server, sha256)))
            .header(AUTHORIZATION, authorization)
            .timeout(TIMEOUT)
            .DELETE()
            .build();
    HttpResponse<String> response = send(request, HttpResponse.BodyHandlers.ofString(), server);
    if (response.statusCode() == 404) {
      throw ToolFailure.BLOB_NOT_FOUND.raise(server + " does not hold a blob " + sha256);
    }
    requireSuccess(response, server, "delete the blob");
  }

  private <T> HttpResponse<T> send(
      HttpRequest request, HttpResponse.BodyHandler<T> handler, String server) {
    try {
      return httpClient.send(request, handler);
    } catch (IOException e) {
      throw ToolFailure.BLOB_SERVER_UNREACHABLE.raise(
          "Could not reach " + server + ": " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw ToolFailure.TIMEOUT.raise("Interrupted while calling " + server);
    } catch (IllegalArgumentException malformed) {
      // The JDK's own parser throws this for a response it cannot make sense of, such as a
      // Content-Length that is not a number, and it does so before any of this class's code
      // sees the response. Uncaught it would leave the tool boundary as an uncoded exception,
      // which is the one failure mode every other path here exists to avoid.
      throw ToolFailure.BLOB_SERVER_REJECTED.raise(
          server + " sent a response this client could not parse: " + malformed.getMessage());
    }
  }

  /**
   * Turns a refusal into something the agent can act on.
   *
   * <p>The status separates the two cases that matter: a 4xx means the request was wrong and
   * repeating it will fail the same way, while a 5xx may be worth another try. The server's own
   * reason is appended when it sent one, because "413" alone does not tell an agent whether to
   * shrink the file or give up.
   */
  private void requireSuccess(HttpResponse<?> response, String server, String attempt) {
    int status = response.statusCode();
    if (status >= 200 && status < 300) {
      return;
    }
    String reason = header(response, REASON_HEADER).map(text -> ": " + text).orElse("");
    String detail = server + " refused to " + attempt + " (HTTP " + status + ")" + reason;
    if (status >= 500) {
      throw ToolFailure.BLOB_SERVER_UNREACHABLE.raise(detail);
    }
    throw ToolFailure.BLOB_SERVER_REJECTED.raise(detail);
  }

  private JsonNode parse(String body, String server) {
    try {
      return MAPPER.readTree(body);
    } catch (IOException e) {
      throw ToolFailure.BLOB_SERVER_REJECTED.raise(
          server + " answered with something that is not JSON: " + e.getMessage());
    }
  }

  private Optional<String> header(HttpResponse<?> response, String name) {
    return response.headers().firstValue(name);
  }

  private boolean isRedirect(int status) {
    return status >= 300 && status < 400;
  }

  /**
   * The Content-Length, treating anything unparseable as absent.
   *
   * <p>Servers omit this header routinely — blossom-server sends none on a HEAD — and a missing
   * size is not worth failing a lookup over. The JDK rejects a malformed value before this is
   * reached, so the catch here is for the cases it does not, such as a header this client never
   * parsed itself.
   */
  private long contentLengthOf(HttpResponse<?> response) {
    try {
      return header(response, "Content-Length").map(Long::parseLong).orElse(0L);
    } catch (NumberFormatException unparseable) {
      return 0;
    }
  }
}
