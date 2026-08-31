package nostr.mcp.transport;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Where the HTTP transport listens, and whether that is safe.
 *
 * <p>The transport has no authentication of its own, so anything that can reach it can publish
 * as every identity the server holds and read every message it can decrypt. Binding to loopback
 * is what makes that acceptable by default, and a deployment that binds wider has made a
 * decision it should be told about rather than one it can drift into.
 */
@Slf4j
public final class BindAddress {

  private static final String LOOPBACK = "127.0.0.1";

  private final String host;

  private BindAddress(String host) {
    this.host = host;
  }

  /**
   * Read the configured address, defaulting to loopback.
   *
   * @param configured the value from configuration, which may be null
   * @return the address to bind
   */
  public static BindAddress fromConfiguredValue(String configured) {
    return new BindAddress(configured == null || configured.isBlank() ? LOOPBACK : configured.trim());
  }

  /**
   * The host to bind.
   *
   * @return the address
   */
  public String host() {
    return host;
  }

  /**
   * Whether this address is reachable only from the machine itself.
   *
   * @return true when it is a loopback address
   */
  public boolean isLoopback() {
    try {
      return InetAddress.getByName(host).isLoopbackAddress();
    } catch (UnknownHostException unresolvable) {
      return false;
    }
  }

  /**
   * Warns when the server is about to be reachable from the network.
   *
   * <p>Noisy rather than silent, on the same principle as the unprotected keystore backend: the
   * weaker choice should announce itself, because the person who made it may not be the person
   * reading the logs.
   */
  public void warnIfReachableFromTheNetwork() {
    if (!isLoopback()) {
      log.warn(
          "The MCP HTTP transport is bound to {}, which is reachable beyond this machine, and it"
              + " has no authentication of its own. Anything that can reach it can publish as"
              + " every identity this server holds. Put a reverse proxy with real credentials in"
              + " front of it, or bind {} instead.",
          host,
          LOOPBACK);
    }
  }
}
