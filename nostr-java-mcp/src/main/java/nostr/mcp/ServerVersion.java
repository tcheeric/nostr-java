package nostr.mcp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The version this server reports to an MCP host.
 *
 * <p>Read from a build-filtered resource rather than written in the source, because a constant
 * copied from the pom is a constant that silently disagrees with it after the next release. A
 * host uses this to tell one build from another, so a stale value is worse than an absent one.
 */
@Slf4j
public final class ServerVersion {

  private static final String RESOURCE = "/nostr-mcp-build.properties";
  private static final String UNKNOWN = "unknown";

  private ServerVersion() {}

  /**
   * The built version.
   *
   * @return the version, or {@code unknown} when the resource is missing, which happens only
   *     outside a packaged build
   */
  public static String current() {
    try (InputStream resource = ServerVersion.class.getResourceAsStream(RESOURCE)) {
      if (resource == null) {
        return UNKNOWN;
      }
      Properties properties = new Properties();
      properties.load(resource);
      return properties.getProperty("version", UNKNOWN);
    } catch (IOException e) {
      log.debug("Could not read the build version: {}", e.getMessage());
      return UNKNOWN;
    }
  }
}
