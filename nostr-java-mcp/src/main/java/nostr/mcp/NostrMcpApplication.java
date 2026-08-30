package nostr.mcp;

import nostr.client.relay.RelayPool;
import nostr.client.relay.RelayConnection;
import nostr.client.relay.RelayConnectionFactory;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.mcp.cli.KeyAdminCli;
import nostr.mcp.identity.IdentityStore;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.identity.KeySources;
import nostr.mcp.identity.KeystoreException;
import nostr.mcp.relay.RelayDirectory;
import nostr.mcp.tool.NostrToolRegistry;
import nostr.mcp.tool.ToolSurface;
import nostr.mcp.write.WriteGuard;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
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
    List<String> commands = commandsIn(args);
    if (!commands.isEmpty()) {
      System.exit(runKeyAdmin(configuration, commands));
    }

    try (RelayPool relayPool =
            new RelayPool(configuration.allRelayUris(), NostrMcpApplication::connectToRelay);
        IdentityVault identityVault = openVault(configuration)) {

      NostrToolRegistry registry =
          ToolSurface.forServer(
              configuration.relayDirectory(),
              relayPool,
              identityVault,
              configuration.queryLimits(),
              Clock.systemUTC(),
              new WriteGuard(
                  relayPool,
                  identityVault,
                  configuration.writePolicy(),
                  configuration.writeRateLimit(Clock.systemUTC())),
              configuration.writePolicy());

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
        keySource(configuration),
        configuration.defaultIdentity(),
        configuration.identityBinding());
  }

  private static KeySource keySource(McpConfiguration configuration) {
    return KeySources.forType(
        configuration.keystoreType(),
        configuration.keystorePath(),
        configuration.identityAliases());
  }

  /**
   * Separates CLI commands from the JVM options a host passes.
   *
   * <p>An MCP host launches the jar with {@code --nostr.mcp.*} flags and no command, so anything
   * that is not a flag is the human asking for key administration instead.
   */
  private static List<String> commandsIn(String[] args) {
    return Arrays.stream(args).filter(argument -> !argument.startsWith("-")).toList();
  }

  /**
   * Runs key administration, which needs a writable backend rather than merely a readable one.
   *
   * <p>Reported as a configuration error rather than a crash, because "this backend cannot
   * create keys" is something the operator can act on.
   */
  private static int runKeyAdmin(McpConfiguration configuration, List<String> commands) {
    KeySource source = keySource(configuration);
    if (!(source instanceof IdentityStore store)) {
      System.out.println(
          "error: the " + source.type() + " keystore cannot be administered from the command line");
      return 1;
    }
    try {
      return new KeyAdminCli(store, System.out).run(commands);
    } catch (KeystoreException e) {
      System.out.println("error: " + e.getMessage());
      return 1;
    }
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
