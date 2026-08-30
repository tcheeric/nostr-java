package nostr.mcp;

import lombok.NonNull;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.transport.BindAddress;
import nostr.mcp.subscription.SubscriptionLimits;
import nostr.mcp.write.RateLimit;
import nostr.mcp.write.WritePolicy;

import java.time.Duration;
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
  private static final int DEFAULT_WRITES_PER_MINUTE = 10;
  private static final String DEFAULT_TRANSPORT = "stdio";
  private static final String HTTP_TRANSPORT = "http";
  private static final int DEFAULT_HTTP_PORT = 8080;

  private final List<String> readRelays;
  private final List<String> writeRelays;
  private final String keystoreType;
  private final String keystorePath;
  private final List<String> identityAliases;
  private final String defaultIdentity;
  private final IdentityBinding identityBinding;

  private McpConfiguration(
      List<String> readRelays,
      List<String> writeRelays,
      String keystoreType,
      String keystorePath,
      List<String> identityAliases,
      String defaultIdentity,
      IdentityBinding identityBinding) {
    this.readRelays = List.copyOf(readRelays);
    this.writeRelays = List.copyOf(writeRelays);
    this.keystoreType = keystoreType;
    this.keystorePath = keystorePath;
    this.identityAliases = List.copyOf(identityAliases);
    this.defaultIdentity = defaultIdentity;
    this.identityBinding = identityBinding;
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
        setting("identity.default"),
        IdentityBinding.fromConfiguredAlias(setting("identity")));
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
        readRelays,
        writeRelays,
        DEFAULT_KEYSTORE_TYPE,
        defaultKeystorePath(),
        List.of(),
        null,
        IdentityBinding.unbound());
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
   * Whether this process is bound to a single identity.
   *
   * @return the binding described by {@code nostr.mcp.identity}
   */
  public IdentityBinding identityBinding() {
    return identityBinding;
  }

  /**
   * The bounds every read stays inside.
   *
   * @return the configured limits, or the specification's defaults
   */
  /**
   * How much freedom an agent has to publish.
   *
   * @return the configured policy, defaulting to requiring confirmation
   */
  /**
   * Which transport to serve on.
   *
   * @return {@code stdio} unless HTTP was configured
   */
  public String transport() {
    return settingOr("transport", DEFAULT_TRANSPORT);
  }

  /**
   * Whether this server serves over HTTP rather than stdio.
   *
   * @return true when the HTTP transport was chosen
   */
  public boolean usesHttpTransport() {
    return HTTP_TRANSPORT.equalsIgnoreCase(transport());
  }

  /**
   * Where the HTTP transport listens.
   *
   * @return the configured address, defaulting to loopback
   */
  public BindAddress bindAddress() {
    return BindAddress.fromConfiguredValue(setting("bind-address"));
  }

  /**
   * Which port the HTTP transport listens on.
   *
   * @return the configured port
   */
  public int httpPort() {
    return positiveIntOr("port", DEFAULT_HTTP_PORT);
  }

  public WritePolicy writePolicy() {
    return WritePolicy.fromConfiguredValue(setting("write-policy"));
  }

  /**
   * The cap on how often one identity may publish.
   *
   * @param clock the source of time for the sliding window
   * @return the configured rate limit
   */
  /**
   * How much freedom an agent has to change the keystore.
   *
   * @return the configured policy, never more permissive than the write policy
   */
  /**
   * The bounds every subscription lives inside.
   *
   * @return the configured limits, or the specification's defaults
   */
  public SubscriptionLimits subscriptionLimits() {
    SubscriptionLimits defaults = SubscriptionLimits.defaults();
    return new SubscriptionLimits(
        positiveIntOr("limits.max-subscriptions", defaults.maxSubscriptions()),
        positiveIntOr("limits.subscription-buffer", defaults.bufferCapacity()),
        durationOr("limits.subscription-idle-timeout", defaults.idleTimeout()));
  }

  /**
   * Which identities the model may read private messages for.
   *
   * <p>Empty by default. Decrypting correspondence puts it into the conversation and so into the
   * host's logs, which is a decision for the person whose messages they are.
   *
   * @return the aliases whose owner has allowed decryption
   */
  public java.util.Set<String> identitiesPermittedToDecrypt() {
    return java.util.Set.copyOf(commaSeparated("dm.decrypt-for", List.of()));
  }

  public IdentityPolicy identityPolicy() {
    return IdentityPolicy.fromConfiguredValue(setting("identity-policy"), writePolicy());
  }

  public RateLimit writeRateLimit(java.time.Clock clock) {
    return new RateLimit(
        positiveIntOr("limits.writes-per-minute", DEFAULT_WRITES_PER_MINUTE),
        Duration.ofMinutes(1),
        clock);
  }

  public QueryLimits queryLimits() {
    QueryLimits defaults = QueryLimits.defaults();
    return new QueryLimits(
        positiveIntOr("limits.max-events-per-query", defaults.maxEventsPerQuery()),
        durationOr("limits.query-timeout", defaults.queryTimeout()));
  }

  /**
   * Reads a positive whole number, ignoring a value that makes no sense.
   *
   * <p>A limit of zero or less would make every query return nothing, which is never what an
   * operator meant, so a nonsensical setting falls back rather than disabling reads.
   */
  private static int positiveIntOr(String key, int fallback) {
    String value = setting(key);
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      int parsed = Integer.parseInt(value.trim());
      return parsed > 0 ? parsed : fallback;
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  /** Accepts the {@code 15s} form an operator writes as well as plain seconds. */
  private static Duration durationOr(String key, Duration fallback) {
    String value = setting(key);
    if (value == null || value.isBlank()) {
      return fallback;
    }
    String trimmed = value.trim().toLowerCase(java.util.Locale.ROOT);
    try {
      if (trimmed.endsWith("ms")) {
        return Duration.ofMillis(Long.parseLong(trimmed.substring(0, trimmed.length() - 2)));
      }
      if (trimmed.endsWith("h")) {
        return Duration.ofHours(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
      }
      if (trimmed.endsWith("m")) {
        return Duration.ofMinutes(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
      }
      if (trimmed.endsWith("s")) {
        return Duration.ofSeconds(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
      }
      return Duration.ofSeconds(Long.parseLong(trimmed));
    } catch (NumberFormatException e) {
      return fallback;
    }
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
    return System.getenv(environmentVariableFor(key));
  }

  /**
   * The environment variable name for a setting.
   *
   * <p>Hyphens become underscores as well as dots. A shell cannot set a variable whose name
   * contains a hyphen, so translating only the dots left every hyphenated setting, including
   * {@code write-policy} and {@code bind-address}, impossible to configure from the environment
   * and therefore from a container.
   *
   * @param key the setting name, as written in configuration
   * @return the environment variable that sets it
   */
  static String environmentVariableFor(String key) {
    return ("NOSTR_MCP_" + key).toUpperCase(java.util.Locale.ROOT).replace('.', '_').replace('-', '_');
  }
}
