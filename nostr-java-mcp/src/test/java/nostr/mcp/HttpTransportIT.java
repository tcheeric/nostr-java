package nostr.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import nostr.client.relay.FakeRelay;
import nostr.client.relay.RelayPool;
import nostr.id.Identity;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.relay.RelayDirectory;
import nostr.mcp.social.McpDirectMessageService;
import nostr.mcp.subscription.SubscriptionLimits;
import nostr.mcp.subscription.SubscriptionRegistry;
import nostr.mcp.tool.NostrToolRegistry;
import nostr.mcp.tool.ToolSurface;
import nostr.mcp.transport.BindAddress;
import nostr.mcp.transport.HttpMcpServer;
import nostr.mcp.write.RateLimit;
import nostr.mcp.write.WriteGuard;
import nostr.mcp.write.WritePolicy;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Speaks real MCP over HTTP to a running server.
 *
 * <p>The transport is where protocol framing, session handling and the servlet container meet,
 * none of which a unit test exercises. A server that compiles and registers its tools can still
 * fail to complete a handshake over the wire, and only an actual client discovers that.
 */
class HttpTransportIT {

  private static final String ALIAS = "personal";
  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  // Verifies a host can connect over HTTP, complete the handshake, and discover the same tools
  // it would over stdio.
  @Test
  void aHostCanConnectOverHttpAndDiscoverTheTools() throws Exception {
    try (RelayPool pool = poolOf();
        IdentityVault vault = vaultOf();
        SubscriptionRegistry subscriptions = subscriptionsOf(pool);
        HttpMcpServer server = serverOn(pool, vault, subscriptions)) {

      try (McpSyncClient client = connectTo(server)) {
        client.initialize();

        List<String> tools = client.listTools().tools().stream().map(Tool::name).toList();

        assertTrue(tools.contains("nostr_list_relays"), tools.toString());
        assertTrue(tools.contains("nostr_list_identities"), tools.toString());
      }
    }
  }

  // Verifies a tool call round-trips over HTTP, proving the transport carries results and not
  // only the handshake.
  @Test
  void aToolCallRoundTripsOverHttp() throws Exception {
    try (RelayPool pool = poolOf();
        IdentityVault vault = vaultOf();
        SubscriptionRegistry subscriptions = subscriptionsOf(pool);
        HttpMcpServer server = serverOn(pool, vault, subscriptions)) {

      try (McpSyncClient client = connectTo(server)) {
        client.initialize();

        CallToolResult result =
            client.callTool(new CallToolRequest("nostr_list_identities", Map.of()));

        assertFalse(Boolean.TRUE.equals(result.isError()), textOf(result));
        assertTrue(textOf(result).contains(ALIAS), textOf(result));
      }
    }
  }

  // Verifies the server binds the loopback interface by default, which is the only thing
  // protecting an unauthenticated transport from anything that can route to the host.
  @Test
  void theServerBindsLoopbackByDefault() throws Exception {
    try (RelayPool pool = poolOf();
        IdentityVault vault = vaultOf();
        SubscriptionRegistry subscriptions = subscriptionsOf(pool);
        HttpMcpServer server = serverOn(pool, vault, subscriptions)) {

      assertTrue(BindAddress.fromConfiguredValue(null).isLoopback());
      assertTrue(server.port() > 0, "the server did not bind a port");
    }
  }

  // Verifies the write policy still governs the surface over HTTP, so the transport is a way in
  // and not a way around the safety model.
  @Test
  void theWritePolicyStillAppliesOverHttp() throws Exception {
    try (RelayPool pool = poolOf();
        IdentityVault vault = vaultOf();
        SubscriptionRegistry subscriptions = subscriptionsOf(pool);
        HttpMcpServer server =
            new HttpMcpServer(
                surfaceOf(pool, vault, subscriptions, WritePolicy.DENY),
                "test",
                subscriptions,
                BindAddress.fromConfiguredValue(null),
                0)) {

      try (McpSyncClient client = connectTo(server)) {
        client.initialize();

        List<String> tools = client.listTools().tools().stream().map(Tool::name).toList();

        assertTrue(tools.stream().noneMatch(name -> name.contains("publish")), tools.toString());
      }
    }
  }

  private McpSyncClient connectTo(HttpMcpServer server) {
    return McpClient.sync(
            HttpClientStreamableHttpTransport.builder(
                    "http://127.0.0.1:" + server.port())
                .endpoint("/mcp")
                .build())
        .requestTimeout(TIMEOUT)
        .build();
  }

  private HttpMcpServer serverOn(
      RelayPool pool, IdentityVault vault, SubscriptionRegistry subscriptions) throws Exception {
    return new HttpMcpServer(
        surfaceOf(pool, vault, subscriptions, WritePolicy.CONFIRM),
        "test",
        subscriptions,
        BindAddress.fromConfiguredValue(null),
        0);
  }

  private NostrToolRegistry surfaceOf(
      RelayPool pool, IdentityVault vault, SubscriptionRegistry subscriptions, WritePolicy policy) {
    return ToolSurface.forServer(
        new RelayDirectory(Map.of(RelayDirectory.READ, List.of("wss://relay.one"))),
        pool,
        vault,
        QueryLimits.defaults(),
        Clock.systemUTC(),
        new WriteGuard(
            pool, vault, policy, new RateLimit(100, Duration.ofMinutes(1), Clock.systemUTC())),
        policy,
        null,
        IdentityPolicy.fromConfiguredValue(null, policy),
        subscriptions,
        new McpDirectMessageService(vault, pool, Set.of()));
  }

  private SubscriptionRegistry subscriptionsOf(RelayPool pool) {
    return new SubscriptionRegistry(
        pool, SubscriptionLimits.defaults(), Clock.systemUTC(), subscriptionId -> {});
  }

  private RelayPool poolOf() {
    return new RelayPool(List.of("wss://relay.one"), relayUri -> FakeRelay.accepting(relayUri));
  }

  private IdentityVault vaultOf() {
    byte[] key =
        HexFormat.of().parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString());
    return new IdentityVault(
        new KeySource() {
          @Override
          public Map<String, byte[]> loadKeys(IdentityBinding binding) {
            return Map.of(ALIAS, key);
          }

          @Override
          public String type() {
            return "test";
          }
        },
        null);
  }

  private String textOf(CallToolResult result) {
    return result.content().stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .map(TextContent::text)
        .findFirst()
        .orElse("");
  }
}
