package nostr.mcp.tool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The single place that decides which tools an agent can see.
 *
 * <p>Concentrating registration here is what makes the surface reviewable: policy and
 * single-identity mode both work by <em>not registering</em> a tool rather than by rejecting a
 * call at runtime, and a tool an agent cannot see is a tool it cannot misuse. That only holds if
 * there is one list to look at.
 *
 * <p>Registering the same name twice is rejected rather than silently resolved, since which of
 * two tools an agent got would otherwise depend on iteration order.
 */
public final class NostrToolRegistry {

  private final Map<String, NostrTool> toolsByName = new LinkedHashMap<>();

  /**
   * Add a tool to the surface.
   *
   * @param tool the tool to register
   * @return this registry
   * @throws IllegalStateException if a tool of that name is already registered
   */
  public NostrToolRegistry register(@NonNull NostrTool tool) {
    NostrTool existing = toolsByName.putIfAbsent(tool.name(), tool);
    if (existing != null) {
      throw new IllegalStateException("A tool named " + tool.name() + " is already registered");
    }
    return this;
  }

  /**
   * The registered tool names, in registration order.
   *
   * @return the names an agent will see
   */
  public List<String> registeredNames() {
    return List.copyOf(toolsByName.keySet());
  }

  /**
   * Render the surface as MCP tool specifications.
   *
   * @return one specification per registered tool
   */
  public List<SyncToolSpecification> toSpecifications() {
    return toolsByName.values().stream().map(NostrToolRegistry::toSpecification).toList();
  }

  private static SyncToolSpecification toSpecification(NostrTool tool) {
    return SyncToolSpecification.builder()
        .tool(
            Tool.builder()
                .name(tool.name())
                .description(tool.description())
                .inputSchema(tool.inputSchema())
                .build())
        .callHandler((exchange, request) -> tool.call(request))
        .build();
  }
}
