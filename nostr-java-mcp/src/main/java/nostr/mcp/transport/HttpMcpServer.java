package nostr.mcp.transport;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.mcp.subscription.SubscriptionRegistry;
import nostr.mcp.subscription.SubscriptionResources;
import nostr.mcp.tool.NostrToolRegistry;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;

/**
 * Serves the MCP tools over streamable HTTP.
 *
 * <p>The alternative to stdio, for a hosted deployment where the host does not launch the
 * process itself. It runs an embedded servlet container rather than requiring one, because a
 * server an operator has to deploy into a container is a server most people will not run.
 *
 * <p><strong>This transport has no authentication.</strong> It binds to loopback by default and
 * says so loudly when it does not, but anything that can reach it can act as every identity the
 * server holds. A deployment exposing it beyond the machine needs a reverse proxy with real
 * credentials in front.
 */
@Slf4j
public final class HttpMcpServer implements AutoCloseable {

  private static final String SERVER_NAME = "nostr-java-mcp";
  private static final String MCP_ENDPOINT = "/mcp";
  private static final String CONTEXT_PATH = "";
  private static final String SERVLET_NAME = "mcp";

  private final McpSyncServer server;
  private final Tomcat tomcat;

  /**
   * Start serving.
   *
   * @param toolRegistry the tools an agent will see
   * @param version the server version reported to the host
   * @param subscriptions open subscriptions to expose as resources, or {@code null} for none
   * @param bindAddress where to listen
   * @param port which port to listen on
   * @throws IOException when the port cannot be bound
   */
  public HttpMcpServer(
      @NonNull NostrToolRegistry toolRegistry,
      @NonNull String version,
      SubscriptionRegistry subscriptions,
      @NonNull BindAddress bindAddress,
      int port)
      throws IOException {
    bindAddress.warnIfReachableFromTheNetwork();

    HttpServletStreamableServerTransportProvider transport =
        HttpServletStreamableServerTransportProvider.builder()
            .jsonMapper(McpJsonDefaults.getMapper())
            .mcpEndpoint(MCP_ENDPOINT)
            .build();

    var builder =
        McpServer.sync(transport)
            .serverInfo(SERVER_NAME, version)
            .capabilities(
                ServerCapabilities.builder()
                    .tools(true)
                    .resources(subscriptions != null, subscriptions != null)
                    .build())
            .tools(toolRegistry.toSpecifications());
    if (subscriptions != null) {
      builder = builder.resources(SubscriptionResources.specification(subscriptions));
    }
    this.server = builder.build();
    this.tomcat = start(transport, bindAddress, port);

    log.info(
        "MCP HTTP transport listening on http://{}:{}{}",
        bindAddress.host(),
        tomcat.getConnector().getLocalPort(),
        MCP_ENDPOINT);
  }

  /**
   * The port actually bound, which differs from the requested one when zero was asked for.
   *
   * @return the listening port
   */
  public int port() {
    return tomcat.getConnector().getLocalPort();
  }

  /**
   * The URL an MCP host connects to.
   *
   * @param bindAddress the address this server was bound to
   * @return the endpoint URL
   */
  public String endpointUrl(@NonNull BindAddress bindAddress) {
    return "http://" + bindAddress.host() + ":" + port() + MCP_ENDPOINT;
  }

  private Tomcat start(
      HttpServletStreamableServerTransportProvider transport, BindAddress bindAddress, int port)
      throws IOException {
    Tomcat embedded = new Tomcat();
    embedded.setBaseDir(temporaryDirectory());
    embedded.setPort(port);
    embedded.getConnector().setProperty("address", bindAddress.host());

    var context = embedded.addContext(CONTEXT_PATH, temporaryDirectory());
    Tomcat.addServlet(context, SERVLET_NAME, transport).setAsyncSupported(true);
    context.addServletMappingDecoded(MCP_ENDPOINT, SERVLET_NAME);

    try {
      embedded.start();
    } catch (Exception e) {
      throw new IOException("Could not start the MCP HTTP transport on port " + port, e);
    }
    return embedded;
  }

  private String temporaryDirectory() throws IOException {
    File directory = File.createTempFile("nostr-mcp-http", "");
    if (!directory.delete() || !directory.mkdirs()) {
      throw new IOException("Could not create a working directory for the HTTP transport");
    }
    directory.deleteOnExit();
    return directory.getAbsolutePath();
  }

  @Override
  public void close() {
    server.close();
    try {
      tomcat.stop();
      tomcat.destroy();
    } catch (Exception e) {
      log.warn("Could not stop the MCP HTTP transport cleanly: {}", e.getMessage());
    }
  }
}
