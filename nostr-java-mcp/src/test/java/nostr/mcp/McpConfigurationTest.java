package nostr.mcp;

import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.write.WritePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies configuration is read as documented, and that the documentation matches the code. */
class McpConfigurationTest {

  private static final Path GUIDE = Path.of("../docs/howto/run-the-mcp-server.md");
  private static final Pattern SETTING_IN_CODE =
      Pattern.compile("(?:setting|settingOr|commaSeparated|positiveIntOr|positiveLongOr|durationOr|relayList)\\(\"([a-z0-9.-]+)\"");

  private final List<String> propertiesSet = new java.util.ArrayList<>();

  @AfterEach
  void clearProperties() {
    propertiesSet.forEach(System::clearProperty);
  }

  // Verifies every setting the code reads is documented, so the guide cannot quietly fall
  // behind a new option that only its author knows about.
  @Test
  void everySettingTheCodeReadsIsDocumented() {
    String guide = read(GUIDE);

    for (String setting : settingsReadByTheCode()) {
      assertTrue(
          guide.contains("`" + setting + "`"),
          "nostr.mcp." + setting + " is read by McpConfiguration but absent from the guide");
    }
  }

  // Verifies the guide states the HTTP transport is unauthenticated, since a deferral of
  // authentication is only safe while the constraint replacing it is visible.
  @Test
  void theGuideWarnsThatTheHttpTransportIsUnauthenticated() {
    String guide = read(GUIDE);

    assertTrue(guide.contains("no authentication"), "the guide does not say the transport is unauthenticated");
    assertTrue(guide.contains("reverse proxy"), "the guide does not say what to do instead");
    assertTrue(guide.contains("127.0.0.1"), "the guide does not state the default binding");
  }

  // Verifies the default transport is stdio, since that is what an MCP host launches.
  @Test
  void theDefaultTransportIsStdio() {
    assertEquals("stdio", McpConfiguration.fromEnvironment().transport());
    assertTrue(McpConfiguration.fromEnvironment().bindAddress().isLoopback());
  }

  // Verifies the HTTP transport is selected by configuration alone, so no code change is needed
  // to move a deployment between transports.
  @Test
  void theHttpTransportIsSelectedByConfiguration() {
    set("nostr.mcp.transport", "http");

    assertTrue(McpConfiguration.fromEnvironment().usesHttpTransport());
  }

  // Verifies a nonsensical limit falls back rather than disabling reads, since a typo should not
  // make every query return nothing.
  @Test
  void aNonsensicalLimitFallsBack() {
    set("nostr.mcp.limits.max-events-per-query", "0");

    assertEquals(500, McpConfiguration.fromEnvironment().queryLimits().maxEventsPerQuery());
  }

  // Verifies durations are read in the forms an operator actually writes.
  @Test
  void durationsAreReadInTheFormsPeopleWrite() {
    set("nostr.mcp.limits.query-timeout", "30s");
    assertEquals(Duration.ofSeconds(30), McpConfiguration.fromEnvironment().queryLimits().queryTimeout());

    set("nostr.mcp.limits.subscription-idle-timeout", "2h");
    assertEquals(
        Duration.ofHours(2), McpConfiguration.fromEnvironment().subscriptionLimits().idleTimeout());
  }

  // Verifies a read-only server cannot mutate the keystore however identity-policy is set,
  // since a server that cannot post should not be able to destroy an account.
  @Test
  void aReadOnlyServerCannotMutateTheKeystore() {
    set("nostr.mcp.write-policy", "deny");
    set("nostr.mcp.identity-policy", "allow");

    assertEquals(WritePolicy.DENY, McpConfiguration.fromEnvironment().writePolicy());
    assertEquals(IdentityPolicy.DENY, McpConfiguration.fromEnvironment().identityPolicy());
  }

  // Verifies decrypting messages is off unless named, since it exposes private correspondence.
  @Test
  void decryptingMessagesIsOffUntilNamed() {
    assertEquals(Set.of(), McpConfiguration.fromEnvironment().identitiesPermittedToDecrypt());

    set("nostr.mcp.dm.decrypt-for", "personal, work");
    assertEquals(
        Set.of("personal", "work"), McpConfiguration.fromEnvironment().identitiesPermittedToDecrypt());
  }

  // Verifies every setting is reachable from the environment, since a container configures the
  // server that way and a shell cannot set a variable whose name contains a hyphen.
  @Test
  void everySettingCanBeSetFromTheEnvironment() {
    for (String setting : settingsReadByTheCode()) {
      String variable = McpConfiguration.environmentVariableFor(setting);

      assertTrue(
          variable.matches("[A-Z0-9_]+"),
          setting + " maps to '" + variable + "', which no shell can set");
    }
  }

  // Verifies hyphenated settings translate to usable variable names, which is the specific case
  // that made write-policy and bind-address unconfigurable from a container.
  @Test
  void hyphenatedSettingsBecomeUnderscoredVariables() {
    assertEquals("NOSTR_MCP_BIND_ADDRESS", McpConfiguration.environmentVariableFor("bind-address"));
    assertEquals("NOSTR_MCP_WRITE_POLICY", McpConfiguration.environmentVariableFor("write-policy"));
    assertEquals(
        "NOSTR_MCP_LIMITS_MAX_EVENTS_PER_QUERY",
        McpConfiguration.environmentVariableFor("limits.max-events-per-query"));
  }

  private Set<String> settingsReadByTheCode() {
    Matcher matcher =
        SETTING_IN_CODE.matcher(read(Path.of("src/main/java/nostr/mcp/McpConfiguration.java")));
    Set<String> settings = new java.util.LinkedHashSet<>();
    while (matcher.find()) {
      settings.add(matcher.group(1));
    }
    return settings;
  }

  private void set(String property, String value) {
    System.setProperty(property, value);
    propertiesSet.add(property);
  }

  private String read(Path file) {
    try {
      return Files.readString(file);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + file, e);
    }
  }
}
