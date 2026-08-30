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

  private static final String DEFAULT_KEYSTORE_TYPE = "os-keychain";

  private final List<String> readRelays;
  private final List<String> writeRelays;
  private final String keystoreType;
  private final String keystorePath;
  private final List<String> identityAliases;
  private final String defaultIdentity;

  private McpConfiguration(
      List<String> readRelays,
      List<String> writeRelays,
      String keystoreType,
      String keystorePath,
      List<String> identityAliases,
      String defaultIdentity) {
    this.readRelays = List.copyOf(readRelays);
    this.writeRelays = List.copyOf(writeRelays);
    this.keystoreType = keystoreType;
    this.keystorePath = keystorePath;
    this.identityAliases = List.copyOf(identityAliases);
    this.defaultIdentity = defaultIdentity;
  }

  /**
   * Read the configuration from system properties and the environment.
   *
   * @return the configuration, fully defaulted
   */
  public static McpConfiguration fromEnvironment() {
    List<String> read = relayList("relays.read", DEFAULT_RELAYS);
    List<String> write = relayList("relays.write", read);
    return new McpConfiguration(
        read,
        write,
        settingOr("keystore.type", DEFAULT_KEYSTORE_TYPE),
        settingOr("keystore.path", defaultKeystorePath()),
        commaSeparated("identities", List.of()),
        setting("identity"));
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
    return new McpConfiguration(
        readRelays, writeRelays, DEFAULT_KEYSTORE_TYPE, defaultKeystorePath(), List.of(), null);
  }

  /**
   * Which keystore backend to read keys from.
   *
   * @return the configured backend type
   */
  public String keystoreType() {
    return keystoreType;
  }

  /**
   * Where the encrypted-file keystore lives.
   *
   * @return the keystore path
   */
  public String keystorePath() {
    return keystorePath;
  }

  /**
   * The identities to look for, needed by backends that cannot enumerate their own contents.
   *
   * @return the configured aliases
   */
  public List<String> identityAliases() {
    return identityAliases;
  }

  /**
   * The identity to sign as when a caller names none.
   *
   * @return the default alias, or {@code null} when none is configured
   */
  public String defaultIdentity() {
    return defaultIdentity;
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
    List<String> configured = commaSeparated(key, fallback);
    return configured.isEmpty() ? fallback : configured;
  }

  private static List<String> commaSeparated(String key, List<String> fallback) {
    String raw = setting(key);
    if (raw == null || raw.isBlank()) {
      return fallback;
    }
    return List.of(raw.split("\\s*,\\s*"));
  }

  private static String settingOr(String key, String fallback) {
    String value = setting(key);
    return value == null || value.isBlank() ? fallback : value;
  }

  private static String defaultKeystorePath() {
    return System.getProperty("user.home") + "/.nostr-java/keys.p12";
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
