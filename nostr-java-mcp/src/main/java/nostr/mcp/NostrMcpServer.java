package nostr.mcp;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import lombok.NonNull;
import nostr.mcp.tool.NostrToolRegistry;

/**
 * Serves the registered Nostr tools to an MCP host.
 *
 * <p>Bootstrapping is all this does: it hands the tool surface to the MCP SDK over a transport
 * and gets out of the way. Deciding which tools exist belongs to {@link NostrToolRegistry}, and
 * what they do belongs to the tools themselves.
 *
 * <p>stdio is the transport an MCP host launches directly, so the server writes protocol frames
 * to standard output. Nothing else may: a stray {@code println} corrupts the stream, which is
 * why logging is configured to standard error.
 */
public final class NostrMcpServer implements AutoCloseable {

  private static final String SERVER_NAME = "nostr-java-mcp";

  private final McpSyncServer server;

  /**
   * Serve the given tools over stdio.
   *
   * @param toolRegistry the tools an agent will see
   * @param version the server version reported to the host
   */
  public NostrMcpServer(@NonNull NostrToolRegistry toolRegistry, @NonNull String version) {
    this(toolRegistry, version, new StdioServerTransportProvider(McpJsonDefaults.getMapper()));
  }

  /**
   * Serve the given tools over a transport of the caller's choosing.
   *
   * @param toolRegistry the tools an agent will see
   * @param version the server version reported to the host
   * @param transportProvider the transport to serve on
   */
  public NostrMcpServer(
      @NonNull NostrToolRegistry toolRegistry,
      @NonNull String version,
      @NonNull StdioServerTransportProvider transportProvider) {
    this.server =
        McpServer.sync(transportProvider)
            .serverInfo(SERVER_NAME, version)
            .capabilities(ServerCapabilities.builder().tools(true).build())
            .tools(toolRegistry.toSpecifications())
            .build();
  }

  /**
   * The underlying MCP server, for callers that need its lifecycle directly.
   *
   * @return the server
   */
  public McpSyncServer getServer() {
    return server;
  }

  @Override
  public void close() {
    server.close();
  }
}
