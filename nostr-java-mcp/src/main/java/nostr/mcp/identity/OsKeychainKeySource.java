package nostr.mcp.identity;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Keys held by the operating system's own secret store.
 *
 * <p>The default, because it needs no passphrase and so does not block an unattended start, and
 * because on a desktop the platform protects a secret better than a file this process can read.
 *
 * <p>It reaches the store through the platform's command-line tool rather than a native binding:
 * {@code security} on macOS, {@code secret-tool} on Linux. That keeps the module free of a
 * native dependency at the cost of shelling out, which is acceptable for something done once at
 * startup.
 *
 * <p>When no such tool exists, this source reports itself unavailable rather than throwing.
 * A container has no keychain, and a server that dies there because of a default would be a
 * default that punishes the deployment it was not chosen for.
 */
@Slf4j
public final class OsKeychainKeySource implements KeySource {

  /** The value that selects this backend. */
  public static final String TYPE = "os-keychain";

  private static final String SERVICE_NAME = "nostr-java-mcp";
  private static final long LOOKUP_TIMEOUT_SECONDS = 10;

  private final KeychainCommand command;
  private final List<String> aliases;

  /**
   * Reads the named identities from whichever platform tool is present.
   *
   * @param aliases the identities to look for
   */
  public OsKeychainKeySource(@NonNull List<String> aliases) {
    this(KeychainCommand.forThisPlatform(), aliases);
  }

  /**
   * @param command the platform tool to invoke, so a test need not touch a real keychain
   * @param aliases the identities to look for
   */
  public OsKeychainKeySource(@NonNull KeychainCommand command, @NonNull List<String> aliases) {
    this.command = command;
    this.aliases = List.copyOf(aliases);
  }

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public Map<String, byte[]> loadKeys() {
    if (!command.isAvailable()) {
      log.warn(
          "No platform keychain is available; configure keystore.type={} instead",
          EncryptedFileKeySource.TYPE);
      return Map.of();
    }
    Map<String, byte[]> keys = new LinkedHashMap<>();
    for (String alias : aliases) {
      command.readSecret(SERVICE_NAME, alias).ifPresent(hex -> keys.put(alias, decode(alias, hex)));
    }
    return keys;
  }

  private byte[] decode(String alias, String hex) {
    try {
      return HexFormat.of().parseHex(hex.trim());
    } catch (IllegalArgumentException e) {
      throw new KeystoreException("Keychain entry '" + alias + "' is not a hex private key", e);
    }
  }

  /**
   * The platform tool this source talks to.
   *
   * <p>An interface rather than a branch on the OS name, so a test can exercise the source
   * without a keychain and a new platform is a new implementation.
   */
  public interface KeychainCommand {

    /**
     * Whether this platform's secret store can be reached.
     *
     * @return true when the tool exists and responds
     */
    boolean isAvailable();

    /**
     * Read one secret.
     *
     * @param service the service the key is filed under
     * @param alias the key's alias
     * @return the stored value, or empty when there is none
     */
    Optional<String> readSecret(String service, String alias);

    /**
     * The tool for the running platform.
     *
     * @return a command that reports itself unavailable when no tool is present
     */
    static KeychainCommand forThisPlatform() {
      return new ProcessKeychainCommand();
    }
  }

  /** Invokes the platform tool as a subprocess. */
  private static final class ProcessKeychainCommand implements KeychainCommand {

    private static final String MACOS_TOOL = "security";
    private static final String LINUX_TOOL = "secret-tool";

    @Override
    public boolean isAvailable() {
      return toolName() != null;
    }

    @Override
    public Optional<String> readSecret(String service, String alias) {
      String tool = toolName();
      if (tool == null) {
        return Optional.empty();
      }
      List<String> arguments =
          MACOS_TOOL.equals(tool)
              ? List.of(tool, "find-generic-password", "-s", service, "-a", alias, "-w")
              : List.of(tool, "lookup", "service", service, "account", alias);
      return runQuietly(arguments);
    }

    private String toolName() {
      if (isOnPath(MACOS_TOOL) && System.getProperty("os.name", "").toLowerCase().contains("mac")) {
        return MACOS_TOOL;
      }
      return isOnPath(LINUX_TOOL) ? LINUX_TOOL : null;
    }

    private boolean isOnPath(String tool) {
      return runQuietly(List.of("which", tool)).isPresent();
    }

    private Optional<String> runQuietly(List<String> arguments) {
      try {
        Process process =
            new ProcessBuilder(arguments).redirectErrorStream(false).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(LOOKUP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
          process.destroyForcibly();
          return Optional.empty();
        }
        return process.exitValue() == 0 && !output.isBlank()
            ? Optional.of(output)
            : Optional.empty();
      } catch (IOException e) {
        return Optional.empty();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return Optional.empty();
      }
    }
  }
}
