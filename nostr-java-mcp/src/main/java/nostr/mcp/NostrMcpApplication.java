package nostr.mcp;

import nostr.client.relay.RelayPool;
import nostr.client.relay.RelayConnection;
import nostr.client.relay.RelayConnectionFactory;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySources;
import nostr.mcp.relay.RelayDirectory;
import nostr.mcp.tool.ListIdentitiesTool;
import nostr.mcp.tool.ListRelaysTool;
import nostr.mcp.tool.NostrToolRegistry;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Launches the MCP server over stdio.
 *
 * <p>An MCP host runs this as a subprocess and speaks JSON-RPC to it over standard input and
 * output, so the process must stay alive until the host closes the stream, and must keep its
 * standard output clean of anything but protocol frames.
 */
public final class NostrMcpApplication {

  private static final String VERSION = "2.2.0";
  private static final long RELAY_CONNECT_TIMEOUT_MS = 60_000L;

  private NostrMcpApplication() {}

  /**
   * @param args unused; configuration comes from {@code nostr.mcp.*} properties and the
   *     environment, since an MCP host passes settings that way rather than positionally
   */
  public static void main(String[] args) throws InterruptedException {
    McpConfiguration configuration = McpConfiguration.fromEnvironment();

    try (RelayPool relayPool =
            new RelayPool(configuration.allRelayUris(), NostrMcpApplication::connectToRelay);
        IdentityVault identityVault = openVault(configuration)) {

      NostrToolRegistry registry =
          new NostrToolRegistry()
              .register(new ListRelaysTool(configuration.relayDirectory(), relayPool))
              .register(new ListIdentitiesTool(identityVault));

      try (NostrMcpServer server = new NostrMcpServer(registry, VERSION)) {
        awaitShutdown();
      }
    }
  }

  /**
   * Blocks until the host terminates the process.
   *
   * <p>The stdio transport serves on its own threads, so main has nothing left to do but stay
   * out of the way; returning would end the process mid-conversation.
   */
  private static void awaitShutdown() throws InterruptedException {
    Thread.currentThread().join();
  }

  private static IdentityVault openVault(McpConfiguration configuration) {
    return new IdentityVault(
        KeySources.forType(
            configuration.keystoreType(),
            configuration.keystorePath(),
            configuration.identityAliases()),
        configuration.defaultIdentity());
  }

  private static RelayConnection connectToRelay(String relayUri) throws IOException {
    try {
      return new NostrRelayClient(relayUri, RELAY_CONNECT_TIMEOUT_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while connecting to relay " + relayUri, e);
    } catch (ExecutionException e) {
      throw new IOException("Could not connect to relay " + relayUri, e.getCause());
    }
  }
}
