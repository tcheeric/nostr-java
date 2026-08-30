package nostr.mcp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the version a host is told matches the version that was built.
 *
 * <p>It was previously a constant in the source, which is a copy of the pom that nothing keeps
 * in step: the next release would have reported the previous version to every host, and nothing
 * would have failed.
 */
class ServerVersionTest {

  private static final Pattern PARENT_VERSION =
      Pattern.compile("<parent>.*?<version>([^<]+)</version>", Pattern.DOTALL);

  // Verifies the reported version is the one in the pom, so a release cannot ship announcing
  // the version before it.
  @Test
  void theReportedVersionIsTheBuiltVersion() {
    assertEquals(versionFromPom(), ServerVersion.current());
  }

  // Verifies the version was actually filtered in, rather than the placeholder surviving into
  // the jar, which would tell every host the literal text of a Maven property.
  @Test
  void theVersionWasFilteredRatherThanLeftAsAPlaceholder() {
    String version = ServerVersion.current();

    assertNotEquals("unknown", version, "the build resource is missing from the classpath");
    assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"), "not a version: " + version);
  }

  private String versionFromPom() {
    Matcher matcher = PARENT_VERSION.matcher(read(Path.of("pom.xml")));
    assertTrue(matcher.find(), "the module pom has no parent version");
    return matcher.group(1);
  }

  private String read(Path file) {
    try {
      return Files.readString(file);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + file, e);
    }
  }
}
