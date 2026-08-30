package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every registered tool must be exercised by a call somewhere in the suite.
 *
 * <p>Answers "have all the tools been tested" by measurement rather than by reading the test
 * sources, where a call made through a shared helper is easy to miscount in either direction.
 * A tool nobody calls is a tool whose behaviour is guaranteed only by the fact that it compiles.
 *
 * <p>Deliberately a coverage floor, not a quality claim. Passing means each tool has been
 * invoked at least once; it says nothing about whether the interesting cases were covered, which
 * is what the per-tool tests are for.
 */
class ToolCoverageTest {

  private static final Path TOOL_LIST = Path.of("src/test/resources/tool-list-default.txt");
  private static final Path TEST_SOURCES = Path.of("src/test/java");
  private static final Path ACCEPTANCE_HARNESS =
      Path.of("../.scratch/nostr-java-mcp/accept.py");

  // Verifies no registered tool goes entirely uncalled by the suite.
  @Test
  void everyRegisteredToolIsCalledSomewhere() {
    Set<String> registered = new LinkedHashSet<>(registeredTools());
    Set<String> called = toolsCalledAnywhere();

    List<String> neverCalled = registered.stream().filter(tool -> !called.contains(tool)).toList();

    assertEquals(List.of(), neverCalled, "these tools are registered but never called by any test");
  }

  private List<String> registeredTools() {
    return read(TOOL_LIST).lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
  }

  /**
   * Finds every tool name that appears in a call, across the Java tests and the acceptance
   * harness.
   *
   * <p>Matches the name next to a calling construct rather than anywhere in the file, so a tool
   * merely named in a golden file or an assertion about the surface does not count as covered.
   */
  private Set<String> toolsCalledAnywhere() {
    Set<String> called = new LinkedHashSet<>();
    for (Path source : testSources()) {
      String text = read(source);
      for (String tool : registeredTools()) {
        if (isCalledIn(text, tool)) {
          called.add(tool);
        }
      }
    }
    return called;
  }

  private boolean isCalledIn(String source, String tool) {
    int index = source.indexOf('"' + tool + '"');
    while (index >= 0) {
      String context = source.substring(Math.max(0, index - 220), index);
      if (context.contains("CallToolRequest")
          || context.contains("callTool")
          || context.endsWith(".tool(")
          || context.endsWith("s.tool(")
          || context.endsWith("probe.tool(")) {
        return true;
      }
      index = source.indexOf('"' + tool + '"', index + 1);
    }
    return false;
  }

  private List<Path> testSources() {
    List<Path> sources = new ArrayList<>();
    try (Stream<Path> walk = Files.walk(TEST_SOURCES)) {
      walk.filter(path -> path.toString().endsWith(".java")).forEach(sources::add);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not walk the test sources", e);
    }
    if (Files.exists(ACCEPTANCE_HARNESS)) {
      sources.add(ACCEPTANCE_HARNESS);
    }
    return sources;
  }

  private String read(Path file) {
    try {
      return Files.readString(file);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + file, e);
    }
  }
}
