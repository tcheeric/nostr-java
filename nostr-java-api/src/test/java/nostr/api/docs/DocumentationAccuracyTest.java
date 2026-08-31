package nostr.api.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Holds the documentation to the same standard as the code it describes.
 *
 * <p>Documentation rots differently from code: nothing fails when a method is renamed out from
 * under a guide, so the guide keeps confidently teaching an API that no longer exists. A reader
 * following it does not conclude the docs are stale, they conclude the library is broken. These
 * checks turn that silent decay into a build failure.
 *
 * <p>Deliberately structural rather than semantic. It cannot tell whether a guide explains
 * something well, only whether the types and methods it names are real, its links resolve, and
 * its stated version matches the build. Those are the failures a reader hits first.
 */
class DocumentationAccuracyTest {

  private static final Path DOCS = Path.of("../docs");
  private static final Path REPOSITORY_ROOT = Path.of("..");

  /**
   * Guides that teach the API, as opposed to explaining a design or proposing a change.
   *
   * <p>Only these are held to "every symbol must exist". A proposal describes code that
   * deliberately does not exist yet, and an architecture note may name a type from a
   * dependency, so applying the rule there would force writers to stop discussing anything
   * hypothetical.
   */
  private static final List<String> INSTRUCTIONAL_DIRECTORIES = List.of("howto", "reference");

  private static final Pattern JAVA_BLOCK = Pattern.compile("```java\\n(.*?)```", Pattern.DOTALL);
  /**
   * A call on something named like a type: at least one lowercase letter after the first.
   *
   * <p>SCREAMING_CASE is excluded because a sample's constants, such as a {@code SENDER}
   * identity declared earlier in a guide, are values rather than types and have no source file
   * to check against.
   */
  private static final Pattern TYPE_USE =
      Pattern.compile("\\b([A-Z][A-Za-z0-9]*[a-z][A-Za-z0-9]*)\\.([a-z][A-Za-z0-9]*)\\s*\\(");
  private static final Pattern MARKDOWN_LINK = Pattern.compile("\\]\\(([^)]+)\\)");

  /**
   * Types that appear in samples but are not this project's to define.
   *
   * <p>The JDK and third-party libraries are legitimately referenced by a guide, and pinning
   * their methods here would test the JDK rather than this documentation.
   */
  private static final Set<String> NOT_OURS_TO_DEFINE =
      Set.of(
          "List", "Map", "Set", "Collections", "Optional", "Stream", "Duration", "Instant",
          "System", "Math", "Thread", "String", "Objects", "Arrays", "HexFormat", "HEX",
          "CompletableFuture", "Executors", "TimeUnit", "UUID", "MDC", "Counter", "Timer",
          "Files", "Path", "Pattern", "LocalDate", "ChronoUnit", "Logger", "LoggerFactory",
          "ObjectMapper", "JsonNode", "Assertions", "Mockito");

  // Verifies every type an instructional guide tells a reader to call actually exists. A guide
  // naming a class that was renamed or removed teaches an API the reader cannot use, and nothing
  // else in the build notices.
  @Test
  void everyTypeTaughtByAGuideExists() {
    Map<String, String> missing = new LinkedHashMap<>();

    for (Path guide : instructionalGuides()) {
      for (String type : typesCalledIn(read(guide))) {
        if (!NOT_OURS_TO_DEFINE.contains(type) && sourceFileFor(type).isEmpty()) {
          missing.put(type, guide.toString());
        }
      }
    }

    assertEquals(Map.of(), missing, "guides name types that do not exist in the source");
  }

  // Verifies every method an instructional guide calls exists on the type it is called on. This
  // is the failure that bit us: the reference taught BaseMessage.read(json), which had never
  // existed, while the real decoder was a different class entirely.
  @Test
  void everyMethodTaughtByAGuideExists() {
    Map<String, String> missing = new LinkedHashMap<>();

    for (Path guide : instructionalGuides()) {
      for (Map.Entry<String, String> call : methodCallsIn(read(guide)).entrySet()) {
        String type = call.getKey().split("\\.")[0];
        if (NOT_OURS_TO_DEFINE.contains(type)) {
          continue;
        }
        sourceFileFor(type)
            .ifPresent(
                source -> {
                  if (!declaresMethod(read(source), call.getValue())) {
                    missing.put(call.getKey(), guide.toString());
                  }
                });
      }
    }

    assertEquals(Map.of(), missing, "guides call methods that do not exist on those types");
  }

  // Verifies every relative link between documents resolves. A broken link in a documentation
  // set is worse than a missing page, because it implies the answer exists and was mislaid.
  @Test
  void everyInternalLinkResolves() {
    List<String> broken = new ArrayList<>();

    for (Path document : allDocuments()) {
      Matcher links = MARKDOWN_LINK.matcher(read(document));
      while (links.find()) {
        String target = links.group(1).split("#")[0];
        if (target.isEmpty() || target.startsWith("http") || target.startsWith("mailto:")) {
          continue;
        }
        Path resolved = document.getParent().resolve(target).normalize();
        if (!Files.exists(resolved)) {
          broken.add(document + " -> " + target);
        }
      }
    }

    assertEquals(List.of(), broken, "these documentation links point at nothing");
  }

  // Verifies the version in an install snippet is the version being built. A reader who copies a
  // stale coordinate gets an older library and none of what the guide then describes.
  //
  // Only snippets a reader would copy to install the library are checked. A migration guide
  // naming the version being upgraded from, a BOM range, or a workflow illustrating a bump are
  // all correct while differing from the current version, so a rule that flagged every literal
  // would force those documents to lie.
  @Test
  void installSnippetsNameTheVersionBeingBuilt() {
    String version = projectVersion();
    List<String> stale = new ArrayList<>();

    for (Path document : instructionalGuides()) {
      Matcher declared =
          Pattern.compile("<artifactId>nostr-java-[a-z]+</artifactId>\\s*\\n\\s*<version>([^<]+)</version>")
              .matcher(read(document));
      while (declared.find()) {
        String declaredVersion = declared.group(1);
        boolean isIllustrative = declaredVersion.contains("+") || declaredVersion.contains("X");
        if (!isIllustrative && !declaredVersion.equals(version)) {
          stale.add(document + " says " + declaredVersion + ", project is " + version);
        }
      }
    }

    assertEquals(List.of(), stale, "install snippets name a version other than the one built");
  }

  // Verifies every documentation file is reachable from the index, since a page nobody links to
  // is a page nobody reads, however well written it is.
  @Test
  void everyGuideIsReachableFromTheIndex() {
    String index = read(DOCS.resolve("README.md"));
    List<String> orphaned = new ArrayList<>();

    for (Path document : allDocuments()) {
      Path relative = DOCS.relativize(document);
      if (relative.toString().equals("README.md") || relative.startsWith("decisions")) {
        continue;
      }
      if (!index.contains(relative.toString())) {
        orphaned.add(relative.toString());
      }
    }

    assertEquals(List.of(), orphaned, "these documents are not linked from docs/README.md");
  }

  // Verifies the entry-point examples name every type they use. A newcomer copies these first,
  // and a sample missing an import or naming a type that moved sends them to the issue tracker
  // before they have published anything.
  //
  // This checks the symbols resolve, not that the block compiles: a snippet is a fragment, and
  // wrapping fragments in a synthetic class tests the wrapper as much as the documentation.
  // The full compile is done deliberately when these samples change.
  @Test
  void entryPointExamplesNameOnlyRealTypes() {
    List<Path> entryPoints =
        List.of(
            REPOSITORY_ROOT.resolve("README.md"),
            DOCS.resolve("GETTING_STARTED.md"));
    Map<String, String> missing = new LinkedHashMap<>();

    for (Path entryPoint : entryPoints) {
      for (Map.Entry<String, String> call : methodCallsIn(read(entryPoint)).entrySet()) {
        String type = call.getKey().split("\\.")[0];
        if (NOT_OURS_TO_DEFINE.contains(type)) {
          continue;
        }
        if (sourceFileFor(type).isEmpty()) {
          missing.put(call.getKey(), entryPoint.toString());
          continue;
        }
        sourceFileFor(type)
            .ifPresent(
                source -> {
                  if (!declaresMethod(read(source), call.getValue())) {
                    missing.put(call.getKey(), entryPoint.toString());
                  }
                });
      }
    }

    assertEquals(Map.of(), missing, "the entry-point examples use types or methods that do not exist");
  }

  private List<Path> instructionalGuides() {
    return allDocuments().stream()
        .filter(
            document ->
                INSTRUCTIONAL_DIRECTORIES.stream()
                    .anyMatch(directory -> document.toString().contains("/" + directory + "/")))
        .toList();
  }

  private List<Path> allDocuments() {
    try (Stream<Path> walk = Files.walk(DOCS)) {
      return walk.filter(path -> path.toString().endsWith(".md")).sorted().toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not walk the documentation", e);
    }
  }

  private Set<String> typesCalledIn(String markdown) {
    Set<String> types = new LinkedHashSet<>();
    methodCallsIn(markdown).keySet().forEach(call -> types.add(call.split("\\.")[0]));
    return types;
  }

  /** Every {@code Type.method(} appearing in a Java block, mapped to its method name. */
  private Map<String, String> methodCallsIn(String markdown) {
    Map<String, String> calls = new LinkedHashMap<>();
    Matcher blocks = JAVA_BLOCK.matcher(markdown);
    while (blocks.find()) {
      Matcher uses = TYPE_USE.matcher(blocks.group(1));
      while (uses.find()) {
        calls.put(uses.group(1) + "." + uses.group(2), uses.group(2));
      }
    }
    return calls;
  }

  /**
   * Finds a type's source file, if this project defines it.
   *
   * <p>An absent result means the name belongs to something else, such as a local variable a
   * sample happened to capitalise, so callers treat it as "not ours" rather than as a failure.
   */
  private java.util.Optional<Path> sourceFileFor(String type) {
    try (Stream<Path> walk = Files.walk(REPOSITORY_ROOT)) {
      return walk.filter(path -> path.toString().endsWith("/" + type + ".java"))
          .filter(path -> path.toString().contains("/src/main/java/"))
          .filter(path -> !path.toString().contains("/target/"))
          .findFirst();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not search for " + type, e);
    }
  }

  /**
   * Whether a source file declares a method by that name.
   *
   * <p>Name only, not signature: a guide may legitimately call an overload, and matching
   * arguments would reject correct documentation for the sake of precision nobody needs here.
   * Builder methods generated by Lombok are accepted through the field they come from.
   */
  private boolean declaresMethod(String source, String method) {
    if (Pattern.compile("\\b" + Pattern.quote(method) + "\\s*\\(").matcher(source).find()) {
      return true;
    }
    boolean isLombokBuilder = source.contains("@Builder") || source.contains("@Data");
    return isLombokBuilder
        && Pattern.compile("\\b" + Pattern.quote(method) + "\\b").matcher(source).find();
  }

  private String projectVersion() {
    Matcher version =
        Pattern.compile("<artifactId>nostr-java</artifactId>\\s*\\n\\s*<version>([^<]+)</version>")
            .matcher(read(REPOSITORY_ROOT.resolve("pom.xml")));
    assertTrue(version.find(), "could not read the project version from the root pom");
    return version.group(1);
  }

  private String read(Path file) {
    try {
      return Files.readString(file);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + file, e);
    }
  }
}
