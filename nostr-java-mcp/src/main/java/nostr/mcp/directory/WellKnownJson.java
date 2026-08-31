package nostr.mcp.directory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches the small JSON documents Nostr keeps outside the relay protocol.
 *
 * <p>Both NIP-05 addresses and NIP-11 relay metadata are ordinary HTTPS documents, so they share
 * one client, one timeout and one way of failing. Keeping that here means neither caller grows
 * its own HTTP handling, and a network error becomes a code an agent can act on rather than an
 * {@code IOException} surfacing as a stack trace.
 */
public final class WellKnownJson {

  private static final Duration TIMEOUT = Duration.ofSeconds(10);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final HttpClient httpClient;

  /**
   * Uses a client that follows redirects, as both documents commonly do.
   *
   * <p>Pinned to HTTP/1.1. Java's client otherwise offers an HTTP/2 upgrade, and a Nostr relay
   * serves its NIP-11 document from the same host and port as its websocket endpoint: it reads
   * the upgrade headers as a botched websocket handshake and answers {@code 400 Failed to create
   * websocket}. Observed against nostr-rs-relay, where curl succeeded and this client did not.
   */
  public WellKnownJson() {
    this(
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(TIMEOUT)
            .build());
  }

  /**
   * @param httpClient the client to fetch with, so a test need not reach the network
   */
  public WellKnownJson(@NonNull HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Fetch and parse a JSON document.
   *
   * @param uri where the document lives
   * @param accept the media type to request, since NIP-11 is served only when asked for
   * @param describeTarget what to call the target in an error the agent reads
   * @return the parsed document
   * @throws nostr.mcp.tool.ToolException when it could not be fetched or was not JSON
   */
  public JsonNode fetch(@NonNull URI uri, @NonNull String accept, @NonNull String describeTarget) {
    HttpRequest request =
        HttpRequest.newBuilder(uri).header("Accept", accept).timeout(TIMEOUT).GET().build();
    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw ToolFailure.RELAY_UNREACHABLE.raise(
            describeTarget + " answered with HTTP " + response.statusCode());
      }
      return MAPPER.readTree(response.body());
    } catch (IOException e) {
      throw ToolFailure.RELAY_UNREACHABLE.raise(
          "Could not reach " + describeTarget + ": " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw ToolFailure.TIMEOUT.raise("Interrupted while fetching " + describeTarget);
    }
  }
}
