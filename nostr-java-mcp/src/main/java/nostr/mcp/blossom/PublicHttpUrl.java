package nostr.mcp.blossom;

import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Refuses a URL that would make the server fetch something on its own network.
 *
 * <p>Every other tool in this module sends an agent's words to a relay. Blossom is the first
 * that makes the server itself fetch a URL the agent chose, which turns the server into a proxy
 * for whatever it can reach and the agent cannot: a metadata endpoint on a cloud instance, a
 * database admin page on localhost, a printer on the office LAN. That is server-side request
 * forgery, and it is the reason this class exists rather than a bare {@code URI.create}.
 *
 * <p>Applied to both the blob's source URL and to a server named in a tool argument. Servers the
 * operator configured are not checked: those are a deliberate choice by a person, which is
 * exactly what an agent-supplied URL is not.
 */
public final class PublicHttpUrl {

  private static final String HTTP = "http";
  private static final String HTTPS = "https";

  private final boolean allowPrivateHosts;

  /**
   * @param allowPrivateHosts whether to permit addresses that are not publicly routable, which a
   *     self-hosted or LAN deployment needs and a public one must not have
   */
  public PublicHttpUrl(boolean allowPrivateHosts) {
    this.allowPrivateHosts = allowPrivateHosts;
  }

  /**
   * Check a URL the agent supplied.
   *
   * @param name the argument it came from, so a refusal says which one to fix
   * @param value the URL as written
   * @return the parsed URL, safe to fetch
   * @throws nostr.mcp.tool.ToolException when it is malformed, not HTTP, or not public
   */
  public URI require(@NonNull String name, @NonNull String value) {
    URI uri = parse(name, value);
    requireHttpScheme(name, uri);
    String host = uri.getHost();
    if (host == null || host.isBlank()) {
      throw ToolFailure.INVALID_ARGUMENT.raise("'" + name + "' has no host: " + value);
    }
    if (!allowPrivateHosts) {
      requirePubliclyRoutable(name, host);
    }
    return uri;
  }

  private URI parse(String name, String value) {
    try {
      return new URI(value.trim());
    } catch (URISyntaxException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise("'" + name + "' is not a valid URL: " + value);
    }
  }

  private void requireHttpScheme(String name, URI uri) {
    String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
    if (!HTTP.equals(scheme) && !HTTPS.equals(scheme)) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'" + name + "' must be an http or https URL, but was '" + uri + "'");
    }
  }

  /**
   * Refuses a host that resolves anywhere but the public internet.
   *
   * <p>Every resolved address has to pass, not just the first. A name that answers with one
   * public address and one loopback address is the cheapest way to defeat a check that stops at
   * {@code getByName}.
   *
   * <p>ponytail: this resolves the name, and the HTTP client then resolves it again, so a name
   * that changes answers between the two calls still gets through. Closing that needs an
   * HttpClient with a pinned resolver; the cap on response size limits what it could be worth.
   */
  private void requirePubliclyRoutable(String name, String host) {
    InetAddress[] addresses;
    try {
      addresses = InetAddress.getAllByName(host);
    } catch (UnknownHostException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'" + name + "' names a host that does not resolve: " + host);
    }
    for (InetAddress address : addresses) {
      if (isPrivate(address)) {
        throw ToolFailure.INVALID_ARGUMENT.raise(
            "'"
                + name
                + "' resolves to "
                + address.getHostAddress()
                + ", which is not on the public internet. This server will not fetch from its own"
                + " network. Set nostr.mcp.blossom.allow-private-hosts=true if that is intended.");
      }
    }
  }

  /**
   * Whether an address is one this server should refuse to fetch from.
   *
   * <p>{@code isSiteLocalAddress} covers 10/8, 172.16/12 and 192.168/16;
   * {@code isLinkLocalAddress} covers 169.254/16, which is where cloud instance metadata lives
   * and so the single most valuable target. IPv6 unique-local (fc00::/7) has no JDK predicate
   * and is checked by its leading bits.
   */
  private boolean isPrivate(InetAddress address) {
    return address.isLoopbackAddress()
        || address.isLinkLocalAddress()
        || address.isSiteLocalAddress()
        || address.isAnyLocalAddress()
        || address.isMulticastAddress()
        || isUniqueLocalIpv6(address);
  }

  private boolean isUniqueLocalIpv6(InetAddress address) {
    byte[] bytes = address.getAddress();
    return bytes.length == 16 && (bytes[0] & 0xFE) == 0xFC;
  }
}
