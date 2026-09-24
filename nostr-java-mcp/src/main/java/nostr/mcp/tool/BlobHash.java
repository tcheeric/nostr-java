package nostr.mcp.tool;

import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;

import java.util.Locale;

/**
 * Reads a blob hash argument, insisting it is one.
 *
 * <p>Blossom addresses everything by sha256, so a malformed hash is the most likely argument
 * mistake and the least informative to discover at the far end: a server answers 404 whether the
 * hash is wrong or the blob is simply elsewhere. Checking here separates the two.
 *
 * <p>Uppercase hex is accepted and folded down. BUD-11 requires lowercase and a server will
 * reject the other, but a model that has read a hash off a rendered page has no way to know that
 * and refusing would be pedantry.
 */
final class BlobHash {

  private static final int SHA256_HEX_LENGTH = 64;

  private BlobHash() {}

  /**
   * Read a required sha256 argument.
   *
   * @param arguments the call's arguments
   * @param name the argument holding the hash
   * @return the hash, lowercased
   * @throws ToolException when it is absent or not a sha256
   */
  static String require(@NonNull ToolArguments arguments, @NonNull String name) {
    String value = arguments.requireText(name).toLowerCase(Locale.ROOT);
    if (value.length() != SHA256_HEX_LENGTH || !value.chars().allMatch(BlobHash::isHexDigit)) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'" + name + "' should be a 64-character sha256 hash in hex, but was '" + value + "'");
    }
    return value;
  }


  /**
   * How to describe the {@code server} argument, given what is configured.
   *
   * <p>With nothing configured the tools still work — an agent can name a server it found in
   * someone's BUD-03 list — so they stay registered. Advertising "Omit for the first configured:
   * []" would be a schema telling the model to omit an argument that is then required.
   */
  static String describeServerArgument(java.util.List<String> configured, String action) {
    return configured.isEmpty()
        ? "The Blossom server to " + action + ", as a full https:// URL. Required: this server has"
            + " none configured."
        : "The Blossom server to " + action + ". Omit for the first configured: " + configured;
  }

  private static boolean isHexDigit(int character) {
    return (character >= '0' && character <= '9') || (character >= 'a' && character <= 'f');
  }
}
