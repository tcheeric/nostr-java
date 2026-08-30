package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the registry is a reviewable list of what an agent can see.
 *
 * <p>Policy and single-identity mode both work by not registering a tool, so the registry is the
 * enforcement point for the whole safety model, not a convenience.
 */
class NostrToolRegistryTest {

  private static final Path GOLDEN_TOOL_LIST =
      Path.of("src/test/resources/tool-list-default.txt");

  // Verifies the registered tool surface matches the golden file, so a tool appearing or
  // disappearing shows up as a reviewable diff rather than passing unnoticed.
  @Test
  void theDefaultToolSurfaceMatchesItsGoldenFile() {
    NostrToolRegistry registry =
        new NostrToolRegistry()
            .register(new StubTool("nostr_list_relays"))
            .register(new StubTool("nostr_list_identities"))
            .register(new StubTool("nostr_query_events"))
            .register(new StubTool("nostr_get_profile"))
            .register(new StubTool("nostr_relay_info"));

    assertEquals(readGolden(), String.join("\n", registry.registeredNames()));
  }

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

  private String readGolden() {
    try {
      return Files.readString(GOLDEN_TOOL_LIST).strip();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read the golden tool list", e);
    }
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
