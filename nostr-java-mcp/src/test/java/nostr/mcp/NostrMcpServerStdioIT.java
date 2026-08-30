package nostr.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Launches the server the way an MCP host does and drives it as a real client.
 *
 * <p>Unit tests can prove a tool computes the right answer while the server never speaks the
 * protocol at all: the transport, the JSON framing, the capability handshake and the process
 * lifecycle are all outside them. This test is the acceptance path, running the built classes
 * in a separate JVM over stdin and stdout, exactly as Claude Desktop or an IDE agent would.
 *
 * <p>It also guards a failure mode unique to stdio: anything written to standard output that is
 * not a protocol frame corrupts the stream. A logging line in the wrong place breaks every host,
 * and only an end-to-end run catches it.
 */
class NostrMcpServerStdioIT {

  private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(30);
  private static final String UNREACHABLE_RELAY = "ws://localhost:1";

  // Verifies a host can launch the server, complete the handshake, and discover the tool
  // surface, which is the whole point of the module existing.
  @Test
  void aHostCanLaunchTheServerAndDiscoverItsTools() {
    try (McpSyncClient client = launchServer()) {
      client.initialize();

      ListToolsResult tools = client.listTools();

      assertEquals(
          List.of("nostr_list_relays", "nostr_list_identities"),
          tools.tools().stream().map(Tool::name).toList());
    }
  }

  // Verifies the discovered tool carries a description and an input schema, since a host shows
  // the first to a model and validates against the second.
  @Test
  void theDiscoveredToolDescribesItself() {
    try (McpSyncClient client = launchServer()) {
      client.initialize();

      Tool tool = client.listTools().tools().getFirst();

      assertNotNull(tool.description());
      assertFalse(tool.description().isBlank());
      assertNotNull(tool.inputSchema());
    }
  }

  // Verifies calling the tool over the protocol returns a usable answer, proving the round trip
  // from host to tool and back rather than only that the tool exists.
  @Test
  void aHostCanCallTheToolAndReadTheAnswer() {
    try (McpSyncClient client = launchServer()) {
      client.initialize();

      CallToolResult result =
          client.callTool(new CallToolRequest("nostr_list_relays", java.util.Map.of()));

      assertFalse(Boolean.TRUE.equals(result.isError()), "the tool reported an error");
      assertTrue(summaryOf(result).contains("relays connected"), summaryOf(result));
      assertNotNull(result.structuredContent());
    }
  }

  // Verifies an agent can discover which identities the server signs with, and that an empty
  // keystore is reported as a usable state rather than a startup failure.
  @Test
  void aHostCanSeeWhichIdentitiesTheServerHolds() {
    try (McpSyncClient client = launchServer()) {
      client.initialize();

      CallToolResult result =
          client.callTool(new CallToolRequest("nostr_list_identities", java.util.Map.of()));

      assertFalse(Boolean.TRUE.equals(result.isError()), "listing identities reported an error");
      assertTrue(summaryOf(result).contains("No identities are configured"), summaryOf(result));
    }
  }


  // Verifies the server starts and serves even when no relay can be reached, since an MCP host
  // launches it before anyone has checked the network, and a server that dies on a dead relay
  // is one a host reports as broken.
  @Test
  void theServerServesEvenWhenNoRelayIsReachable() {
    try (McpSyncClient client = launchServer()) {
      client.initialize();

      CallToolResult result =
          client.callTool(new CallToolRequest("nostr_list_relays", java.util.Map.of()));

      assertTrue(summaryOf(result).startsWith("0 of "), summaryOf(result));
    }
  }

  /**
   * Starts the packaged server in its own JVM, pointed at a relay that does not exist.
   *
   * <p>An unreachable relay is deliberate: this test is about the protocol, and a real relay
   * would make it depend on Docker for no benefit. It doubles as the evidence that the server
   * survives a dead relay set.
   */
  private McpSyncClient launchServer() {
    ServerParameters parameters =
        ServerParameters.builder("java")
            .args(
                "-Dnostr.mcp.relays.read=" + UNREACHABLE_RELAY,
                "-cp",
                runtimeClasspath(),
                NostrMcpApplication.class.getName())
            .build();

    return McpClient.sync(new StdioClientTransport(parameters, McpJsonDefaults.getMapper()))
        .requestTimeout(STARTUP_TIMEOUT)
        .build();
  }

  /** The classpath this test is running with, which already contains the module and the SDK. */
  private String runtimeClasspath() {
    return System.getProperty("java.class.path");
  }

  private String summaryOf(CallToolResult result) {
    return ((TextContent) result.content().getFirst()).text();
  }
}
