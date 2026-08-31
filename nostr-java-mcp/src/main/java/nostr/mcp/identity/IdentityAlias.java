package nostr.mcp.identity;

import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.util.regex.Pattern;

/**
 * The name a person gives one of their identities.
 *
 * <p>Aliases appear in resource URIs such as {@code nostr://identity/personal}, so they are
 * restricted to characters that survive a URI unescaped. Validating on the way in means the rest
 * of the module can treat an alias as a plain path segment rather than escaping it at every use,
 * and it rules out an alias whose slashes or spaces would silently address something else.
 */
public final class IdentityAlias {

  private static final Pattern PERMITTED = Pattern.compile("[a-z0-9-]{1,32}");

  private IdentityAlias() {}

  /**
   * Check an alias, explaining the rule when it fails.
   *
   * @param alias the proposed name
   * @return the alias, unchanged
   * @throws nostr.mcp.tool.ToolException when it would not be safe in a URI
   */
  public static String validated(@NonNull String alias) {
    String trimmed = alias.trim();
    if (!PERMITTED.matcher(trimmed).matches()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'"
              + alias
              + "' is not a usable alias. Use 1 to 32 characters of lowercase letters, digits and"
              + " hyphens, such as 'project-bot'.");
    }
    return trimmed;
  }
}
