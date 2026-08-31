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

  private static final Path RELAY_BACKED_TESTS = Path.of("src/test/java/nostr/mcp/integration");
  private static final Path MODEL_DRIVEN_TEST =
      Path.of("src/test/java/nostr/mcp/integration/OllamaAgentIT.java");

  // Verifies every tool is exercised against a real relay, not only against fakes. Most of these
  // tools exist to talk to a relay, and the failures worth catching are the ones a stand-in
  // cannot produce: nostr_relay_info passed its unit tests while being unable to read any real
  // relay's document.
  @Test
  void everyToolIsExercisedAgainstARealRelay() {
    Set<String> againstARelay = toolsCalledIn(sourcesUnder(RELAY_BACKED_TESTS));

    List<String> untested =
        registeredTools().stream().filter(tool -> !againstARelay.contains(tool)).toList();

    assertEquals(List.of(), untested, "these tools are never called against a real relay");
  }

  // Verifies every tool is offered to a real model and chosen for a plausible request. A tool
  // can work perfectly and still be unreachable, because its description does not distinguish it
  // from a neighbour, and nothing else here can detect that.
  @Test
  void everyToolIsReachedByAModel() {
    String modelTest = read(MODEL_DRIVEN_TEST);

    List<String> unreached =
        registeredTools().stream()
            .filter(tool -> !modelTest.contains('"' + tool + '"'))
            .filter(tool -> !EXEMPT_FROM_MODEL_SELECTION.contains(tool))
            .toList();

    assertEquals(List.of(), unreached, "no model-selection case reaches these tools");
  }

  /**
   * Tools deliberately not offered to the model as a selection case.
   *
   * <p>Both are reached only through an explicit instruction rather than a plausible request, so
   * asking a model to pick them tests the phrasing of the prompt rather than the surface.
   * {@code nostr_remove_identity} is the one irreversible tool, and inviting a model to choose it
   * is a bad habit to build into a test suite; {@code nostr_publish_event} is the escape hatch,
   * which by design overlaps every other publishing tool.
   */
  private static final Set<String> EXEMPT_FROM_MODEL_SELECTION =
      Set.of("nostr_remove_identity", "nostr_publish_event");

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
    return toolsCalledIn(testSources());
  }

  private Set<String> toolsCalledIn(List<Path> sources) {
    Set<String> called = new LinkedHashSet<>();
    for (Path source : sources) {
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

  private List<Path> sourcesUnder(Path directory) {
    List<Path> sources = new ArrayList<>();
    try (Stream<Path> walk = Files.walk(directory)) {
      walk.filter(path -> path.toString().endsWith(".java")).forEach(sources::add);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not walk " + directory, e);
    }
    return sources;
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
