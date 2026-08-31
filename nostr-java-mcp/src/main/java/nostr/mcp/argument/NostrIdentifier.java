package nostr.mcp.argument;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.mcp.tool.ToolException;
import nostr.mcp.tool.ToolFailure;

import java.util.HexFormat;
import java.util.Locale;

/**
 * A public key or event id, however the caller chose to write it.
 *
 * <p>Nostr identifiers appear in two forms: the hex a relay speaks and the bech32 a person
 * copies from a client. An agent will be handed whichever the user pasted, so every tool must
 * accept both. Centralising that here is what stops each tool growing its own slightly different
 * parser, and it means a malformed identifier produces one recognisable error instead of a
 * different stack trace per tool.
 *
 * <p>The type is the guarantee: holding a {@code NostrIdentifier} means the value was decoded
 * successfully, so nothing downstream re-validates it.
 */
public final class NostrIdentifier {

  private static final int HEX_LENGTH = 64;
  private static final String PUBLIC_KEY_PREFIX = "npub";
  private static final String NOTE_PREFIX = "note";
  private static final String EVENT_PREFIX = "nevent";
  private static final String PRIVATE_KEY_PREFIX = "nsec";

  private final String hex;

  private NostrIdentifier(String hex) {
    this.hex = hex;
  }

  /**
   * Decode a public key given as hex or {@code npub}.
   *
   * @param argumentName the argument being decoded, so the error names what the caller wrote
   * @param value the caller's text
   * @return the decoded key
   * @throws ToolException when the value is neither form, or is a private key
   */
  public static NostrIdentifier publicKey(@NonNull String argumentName, @NonNull String value) {
    return decode(argumentName, value, PUBLIC_KEY_PREFIX);
  }

  /**
   * Decode an event id given as hex, {@code note} or {@code nevent}.
   *
   * @param argumentName the argument being decoded
   * @param value the caller's text
   * @return the decoded id
   * @throws ToolException when the value is none of those forms
   */
  public static NostrIdentifier eventId(@NonNull String argumentName, @NonNull String value) {
    return decode(argumentName, value, NOTE_PREFIX, EVENT_PREFIX);
  }

  /**
   * The identifier as a relay expects it.
   *
   * @return the 64-character lowercase hex form
   */
  public String hex() {
    return hex;
  }

  /**
   * The identifier as a public key.
   *
   * @return the key, for tools that address by it
   */
  public PublicKey asPublicKey() {
    return new PublicKey(hex);
  }

  private static NostrIdentifier decode(
      String argumentName, String value, String... acceptedPrefixes) {
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(argumentName + " is empty");
    }
    refusePrivateKey(argumentName, trimmed);
    return isBech32(trimmed)
        ? fromBech32(argumentName, trimmed, acceptedPrefixes)
        : fromHex(argumentName, trimmed, acceptedPrefixes);
  }

  /**
   * Refuses a private key wherever a public identifier belongs.
   *
   * <p>An agent that pastes an nsec into a pubkey argument has just put a private key somewhere
   * it may be logged or echoed. Rejecting it by name gives the user a chance to rotate, where a
   * generic decode failure would leave the mistake invisible.
   */
  private static void refusePrivateKey(String argumentName, String value) {
    if (value.toLowerCase(Locale.ROOT).startsWith(PRIVATE_KEY_PREFIX)) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          argumentName
              + " looks like a private key (nsec). Never send a private key to a tool; treat"
              + " this one as compromised and replace it.");
    }
  }

  private static boolean isBech32(String value) {
    return value.contains("1") && !isHex(value);
  }

  private static boolean isHex(String value) {
    if (value.length() != HEX_LENGTH) {
      return false;
    }
    return value.chars().allMatch(NostrIdentifier::isHexDigit);
  }

  private static boolean isHexDigit(int character) {
    return (character >= '0' && character <= '9')
        || (character >= 'a' && character <= 'f')
        || (character >= 'A' && character <= 'F');
  }

  private static NostrIdentifier fromHex(
      String argumentName, String value, String... acceptedPrefixes) {
    if (!isHex(value)) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          argumentName
              + " is not a valid identifier; expected 64 hex characters or a "
              + describe(acceptedPrefixes)
              + " string");
    }
    return new NostrIdentifier(value.toLowerCase(Locale.ROOT));
  }

  private static NostrIdentifier fromBech32(
      String argumentName, String value, String... acceptedPrefixes) {
    refuseUnexpectedPrefix(argumentName, value, acceptedPrefixes);
    try {
      String decoded = Bech32.fromBech32(value);
      return new NostrIdentifier(decoded.toLowerCase(Locale.ROOT));
    } catch (Exception e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          argumentName + " is not a valid " + describe(acceptedPrefixes) + " string");
    }
  }

  /**
   * Names the mistake when the form is valid but means something else.
   *
   * <p>"an npub was given where an event id belongs" is actionable; "decode failed" is not, and
   * the two are easy to confuse when both identifiers are 64 bytes of bech32.
   */
  private static void refuseUnexpectedPrefix(
      String argumentName, String value, String... acceptedPrefixes) {
    for (String prefix : acceptedPrefixes) {
      if (value.startsWith(prefix)) {
        return;
      }
    }
    throw ToolFailure.INVALID_ARGUMENT.raise(
        argumentName
            + " should be "
            + describe(acceptedPrefixes)
            + " or hex, but starts with '"
            + value.substring(0, Math.min(value.indexOf('1') + 1, value.length()))
            + "'");
  }

  private static String describe(String... acceptedPrefixes) {
    return String.join(" or ", acceptedPrefixes);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof NostrIdentifier identifier && hex.equals(identifier.hex);
  }

  @Override
  public int hashCode() {
    return hex.hashCode();
  }

  @Override
  public String toString() {
    return hex;
  }
}
