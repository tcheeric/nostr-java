package nostr.mcp.blossom;

import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Which Blossom server a tool call acts on.
 *
 * <p>An agent that must invent a server URL will invent a plausible one, so the operator
 * configures the servers this deployment uses and a call that names none gets the first of them.
 * A call may still name a server, because the whole point of a hash-addressed network is that
 * the same blob lives on several, and a user's own BUD-03 list is discovered at runtime rather
 * than configured.
 *
 * <p>A named server is checked by {@link PublicHttpUrl}; a configured one is not. The difference
 * is who chose it. Configuration is a person's decision, and a person may legitimately point
 * this at a server on their own network.
 */
public final class BlossomServers {

  private final List<String> configured;
  private final PublicHttpUrl publicHttpUrl;

  /**
   * @param configured the servers this deployment uses, most preferred first
   * @param publicHttpUrl the guard applied to a server an agent names
   */
  public BlossomServers(@NonNull List<String> configured, @NonNull PublicHttpUrl publicHttpUrl) {
    this.configured = configured.stream().map(BlossomServers::normalise).distinct().toList();
    this.publicHttpUrl = publicHttpUrl;
  }

  /**
   * Resolve the server a call should act on.
   *
   * @param requested the {@code server} argument, or empty when the call named none
   * @return the server's base URL, without a trailing slash
   * @throws nostr.mcp.tool.ToolException when none was named and none is configured
   */
  public String resolve(@NonNull Optional<String> requested) {
    if (requested.isPresent()) {
      String server = normalise(requested.get());
      if (!configured.contains(server)) {
        publicHttpUrl.require("server", server);
      }
      return server;
    }
    return configured.stream()
        .findFirst()
        .orElseThrow(
            () ->
                ToolFailure.INVALID_ARGUMENT.raise(
                    "No Blossom server was given and none is configured. Pass 'server' with the"
                        + " server's URL, or set nostr.mcp.blossom.servers."));
  }

  /**
   * Every server to try when looking for a blob that could be on any of them.
   *
   * @param requested the {@code server} argument, or empty to search all configured servers
   * @return the servers to try, in preference order
   * @throws nostr.mcp.tool.ToolException when none was named and none is configured
   */
  public List<String> resolveAll(@NonNull Optional<String> requested) {
    if (requested.isPresent()) {
      return List.of(resolve(requested));
    }
    if (configured.isEmpty()) {
      return List.of(resolve(requested));
    }
    return configured;
  }

  /**
   * The servers the operator configured, for a tool schema to name.
   *
   * @return the configured server URLs
   */
  public List<String> configured() {
    return configured;
  }

  /**
   * Build the URL a blob is served from.
   *
   * @param server the server's base URL
   * @param sha256 the blob's hash
   * @return the blob's URL
   */
  public static String blobUrl(@NonNull String server, @NonNull String sha256) {
    return normalise(server) + "/" + sha256;
  }

  /**
   * Strip a trailing slash so two spellings of one server are one server.
   *
   * <p>{@code https://cdn.example.com/} and {@code https://cdn.example.com} would otherwise be
   * different keys, and every path this class builds would grow a double slash for one of them.
   */
  private static String normalise(String server) {
    String trimmed = server.trim();
    while (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length() - 1);
    }
    return trimmed;
  }

  /**
   * Read the servers out of a BUD-03 list, keeping the order the author chose.
   *
   * @param serverUrls the values of the event's {@code server} tags
   * @return the distinct servers, normalised
   */
  public static List<String> fromServerTags(@NonNull List<String> serverUrls) {
    Set<String> ordered = new LinkedHashSet<>();
    serverUrls.stream()
        .filter(url -> !url.isBlank())
        .map(BlossomServers::normalise)
        .forEach(ordered::add);
    return List.copyOf(ordered);
  }

  /**
   * Check that a server URL is well formed before it is published in a BUD-03 list.
   *
   * <p>Deliberately a syntax check and not {@link PublicHttpUrl}. Publishing a server list
   * fetches nothing, so there is no request to forge, and applying the address check here would
   * do two wrong things: stop an operator running Blossom on their own network from ever
   * advertising it, and fail the whole publish on a transient DNS failure for any one URL. What
   * matters is that readers of the event get something they can use.
   *
   * @param server the URL as written
   * @return the normalised URL
   * @throws nostr.mcp.tool.ToolException when it is not an http(s) URL with a host
   */
  public static URI requireUsable(@NonNull String server) {
    String normalised = normalise(server);
    URI uri;
    try {
      uri = new URI(normalised);
    } catch (URISyntaxException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise("'servers' is not a valid URL: " + server);
    }
    String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
    if (!"http".equals(scheme) && !"https".equals(scheme)) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'servers' entries must be http or https URLs, but had '" + server + "'");
    }
    if (uri.getHost() == null || uri.getHost().isBlank()) {
      throw ToolFailure.INVALID_ARGUMENT.raise("'servers' entry has no host: " + server);
    }
    return uri;
  }
}
