package nostr.mcp;

import lombok.NonNull;
import nostr.mcp.relay.RelayDirectory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The settings a server starts with, read from {@code nostr.mcp.*}.
 *
 * <p>Every value has a working default, because a server that cannot start without a
 * hand-written configuration file is one an MCP host cannot launch. Defaults are read from
 * system properties or the environment, so a host entry can override them on the command line
 * without a file existing at all.
 */
public final class McpConfiguration {

  private static final String PREFIX = "nostr.mcp.";
  private static final List<String> DEFAULT_RELAYS =
      List.of("wss://relay.damus.io", "wss://nos.lol");

  private final List<String> readRelays;
  private final List<String> writeRelays;

  private McpConfiguration(List<String> readRelays, List<String> writeRelays) {
    this.readRelays = List.copyOf(readRelays);
    this.writeRelays = List.copyOf(writeRelays);
  }

  /**
   * Read the configuration from system properties and the environment.
   *
   * @return the configuration, fully defaulted
   */
  public static McpConfiguration fromEnvironment() {
    List<String> read = relayList("relays.read", DEFAULT_RELAYS);
    List<String> write = relayList("relays.write", read);
    return new McpConfiguration(read, write);
  }

  /**
   * Build a configuration directly, for tests and embedding.
   *
   * @param readRelays relays events are read from
   * @param writeRelays relays events are published to
   * @return the configuration
   */
  public static McpConfiguration of(
      @NonNull List<String> readRelays, @NonNull List<String> writeRelays) {
    return new McpConfiguration(readRelays, writeRelays);
  }

  /**
   * The relay names an agent can use, mapped to the URIs they stand for.
   *
   * @return the directory
   */
  public RelayDirectory relayDirectory() {
    Map<String, List<String>> byName = new LinkedHashMap<>();
    byName.put(RelayDirectory.READ, readRelays);
    byName.put(RelayDirectory.WRITE, writeRelays);
    return new RelayDirectory(byName);
  }

  /**
   * Every relay this server connects to at startup.
   *
   * @return the distinct relay URIs across read and write sets
   */
  public List<String> allRelayUris() {
    return relayDirectory().allRelayUris();
  }

  private static List<String> relayList(String key, List<String> fallback) {
    String raw = setting(key);
    if (raw == null || raw.isBlank()) {
      return fallback;
    }
    return List.of(raw.split("\\s*,\\s*"));
  }

  /** Looks in system properties first, then the environment, so a host entry can override. */
  private static String setting(String key) {
    String property = System.getProperty(PREFIX + key);
    if (property != null) {
      return property;
    }
    return System.getenv(("NOSTR_MCP_" + key).toUpperCase().replace('.', '_'));
  }
}
