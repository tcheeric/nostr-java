package nostr.mcp.identity;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Keys read from environment variables, for development only.
 *
 * <p>Zero setup and it works anywhere, which is exactly why it is tempting and why it warns. A
 * process environment is readable by child processes, appears in {@code /proc} on Linux, and is
 * captured wholesale by most crash reporters, so a key here should be one nobody minds losing.
 *
 * <p>Variables are named {@code NOSTR_MCP_KEY_<ALIAS>}, so {@code NOSTR_MCP_KEY_PERSONAL}
 * becomes the alias {@code personal}.
 */
public final class EnvironmentKeySource implements KeySource {

  /** The value that selects this backend. */
  public static final String TYPE = "env";

  private static final String PREFIX = "NOSTR_MCP_KEY_";

  private final Supplier<Map<String, String>> environment;

  /** Reads the process environment. */
  public EnvironmentKeySource() {
    this(System::getenv);
  }

  /**
   * @param environment the variables to read, so a test need not mutate the process environment
   */
  public EnvironmentKeySource(Supplier<Map<String, String>> environment) {
    this.environment = environment;
  }

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public boolean protectsKeysAtRest() {
    return false;
  }

  @Override
  public Map<String, byte[]> loadKeys(IdentityBinding binding) {
    Map<String, byte[]> keys = new LinkedHashMap<>();
    environment
        .get()
        .forEach(
            (name, value) -> {
              if (name.startsWith(PREFIX) && !value.isBlank()) {
                String alias = aliasOf(name);
                if (binding.permitted(Set.of(alias)).contains(alias)) {
                  keys.put(alias, decode(name, value));
                }
              }
            });
    return keys;
  }

  private String aliasOf(String variableName) {
    return variableName.substring(PREFIX.length()).toLowerCase().replace('_', '-');
  }

  private byte[] decode(String variableName, String value) {
    try {
      return HexFormat.of().parseHex(value.trim());
    } catch (IllegalArgumentException e) {
      throw new KeystoreException(
          variableName + " does not hold a hex private key", e);
    }
  }
}
