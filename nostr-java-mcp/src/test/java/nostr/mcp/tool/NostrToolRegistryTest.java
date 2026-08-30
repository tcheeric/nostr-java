package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the registry's own rules: ordering, and refusing a duplicate name.
 *
 * <p>Which tools a server actually exposes is pinned by {@code ToolSurfaceTest} against the real
 * surface. Asserting it here too, over stubs, would only pin this test's fixture, and the golden
 * file would then need updating in two places for one change.
 */
class NostrToolRegistryTest {

  // Verifies tools keep their registration order, so the golden file is stable rather than
  // depending on hash iteration.
  @Test
  void toolsKeepTheirRegistrationOrder() {
    NostrToolRegistry registry =
        new NostrToolRegistry()
            .register(new StubTool("nostr_second"))
            .register(new StubTool("nostr_first"));

    assertEquals(List.of("nostr_second", "nostr_first"), registry.registeredNames());
  }

  // Verifies registering a duplicate name fails loudly, since which of two tools an agent got
  // would otherwise depend on iteration order.
  @Test
  void aDuplicateToolNameIsRejected() {
    NostrToolRegistry registry = new NostrToolRegistry().register(new StubTool("nostr_thing"));

    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> registry.register(new StubTool("nostr_thing")));

    assertEquals("A tool named nostr_thing is already registered", thrown.getMessage());
  }

  // Verifies each registered tool becomes one MCP specification carrying its own name, which is
  // what lets a host discover the surface.
  @Test
  void eachToolBecomesOneSpecification() {
    NostrToolRegistry registry =
        new NostrToolRegistry()
            .register(new StubTool("nostr_one"))
            .register(new StubTool("nostr_two"));

    assertEquals(
        List.of("nostr_one", "nostr_two"),
        registry.toSpecifications().stream().map(spec -> spec.tool().name()).toList());
  }

  /** A tool that exists only to be registered. */
  private record StubTool(String name) implements NostrTool {
    @Override
    public String description() {
      return "A tool used only in tests.";
    }

    @Override
    public Map<String, Object> inputSchema() {
      return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public CallToolResult call(CallToolRequest request) {
      return CallToolResult.builder().addTextContent("stub").build();
    }
  }
}
