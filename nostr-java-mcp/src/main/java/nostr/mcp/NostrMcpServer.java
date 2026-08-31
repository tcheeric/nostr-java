package nostr.mcp;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import lombok.NonNull;
import nostr.mcp.subscription.SubscriptionRegistry;
import nostr.mcp.subscription.SubscriptionResources;
import nostr.mcp.tool.NostrToolRegistry;

import java.util.List;

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
    this(toolRegistry, version, (SubscriptionRegistry) null);
  }

  /**
   * Serve tools and subscription resources over stdio.
   *
   * @param toolRegistry the tools an agent will see
   * @param version the server version reported to the host
   * @param subscriptions open subscriptions to expose as resources, or {@code null} for none
   */
  public NostrMcpServer(
      @NonNull NostrToolRegistry toolRegistry,
      @NonNull String version,
      SubscriptionRegistry subscriptions) {
    this(
        toolRegistry,
        version,
        new StdioServerTransportProvider(McpJsonDefaults.getMapper()),
        subscriptions,
        List.of(),
        List.of());
  }

  /**
   * Serve tools, subscription resources, context resources and guided prompts.
   *
   * @param toolRegistry the tools an agent will see
   * @param version the server version reported to the host
   * @param subscriptions open subscriptions to expose, or {@code null} for none
   * @param contextResources resources describing this server's identities and relays
   * @param prompts guided sequences teaching a host how to use the tools
   */
  public NostrMcpServer(
      @NonNull NostrToolRegistry toolRegistry,
      @NonNull String version,
      SubscriptionRegistry subscriptions,
      @NonNull List<SyncResourceSpecification> contextResources,
      @NonNull List<SyncPromptSpecification> prompts) {
    this(
        toolRegistry,
        version,
        new StdioServerTransportProvider(McpJsonDefaults.getMapper()),
        subscriptions,
        contextResources,
        prompts);
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
    this(toolRegistry, version, transportProvider, null, List.of(), List.of());
  }


  /**
   * Serve tools, and subscription resources when there are subscriptions to serve.
   *
   * <p>Resources are advertised only when the server actually holds subscriptions, since
   * declaring a capability the server cannot honour would have hosts offer an agent something
   * that always comes back empty.
   *
   * @param toolRegistry the tools an agent will see
   * @param version the server version reported to the host
   * @param transportProvider the transport to serve on
   * @param subscriptions open subscriptions to expose, or {@code null} for none
   */
  public NostrMcpServer(
      @NonNull NostrToolRegistry toolRegistry,
      @NonNull String version,
      @NonNull StdioServerTransportProvider transportProvider,
      SubscriptionRegistry subscriptions,
      @NonNull List<SyncResourceSpecification> contextResources,
      @NonNull List<SyncPromptSpecification> prompts) {
    List<SyncResourceSpecification> resources = new java.util.ArrayList<>(contextResources);
    if (subscriptions != null) {
      resources.add(SubscriptionResources.specification(subscriptions));
    }
    var builder =
        McpServer.sync(transportProvider)
            .serverInfo(SERVER_NAME, version)
            .capabilities(
                ServerCapabilities.builder()
                    .tools(true)
                    .resources(!resources.isEmpty(), subscriptions != null)
                    .prompts(!prompts.isEmpty())
                    .build())
            .tools(toolRegistry.toSpecifications());
    if (!resources.isEmpty()) {
      builder = builder.resources(resources);
    }
    if (!prompts.isEmpty()) {
      builder = builder.prompts(prompts);
    }
    this.server = builder.build();
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
