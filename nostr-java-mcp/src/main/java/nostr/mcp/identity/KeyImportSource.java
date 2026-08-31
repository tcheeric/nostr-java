package nostr.mcp.identity;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.crypto.bech32.Bech32;
import nostr.mcp.tool.ToolFailure;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Where the server should read a key from, when told to import one.
 *
 * <p>This type exists to make one thing impossible: a private key travelling through the model.
 * An import tool taking an {@code nsec} argument would put the key in the conversation, the
 * host's logs, and very probably a third-party inference API, which is the worst thing this
 * module could do. So the agent names a <em>location</em> and the server reads it directly; the
 * model orchestrates an import it never observes.
 *
 * <p>The same principle as keys never leaving the vault, applied to the way in.
 */
@Slf4j
public final class KeyImportSource {

  private static final String FILE_PREFIX = "file:";
  private static final String ENVIRONMENT_PREFIX = "env:";
  private static final String PROMPT = "prompt";
  private static final String NSEC_PREFIX = "nsec";

  private KeyImportSource() {}

  /**
   * Read the key the source names.
   *
   * @param source {@code file:<path>}, {@code env:<VARIABLE>}, or {@code prompt}
   * @return the private key material, which the caller owns and wipes
   * @throws nostr.mcp.tool.ToolException when the source is unreadable, unrecognised, or holds
   *     something that is not a key
   */
  public static byte[] read(@NonNull String source) {
    String trimmed = source.trim();
    refuseKeyMaterialAsSource(trimmed);
    if (trimmed.startsWith(FILE_PREFIX)) {
      return fromFile(trimmed.substring(FILE_PREFIX.length()));
    }
    if (trimmed.startsWith(ENVIRONMENT_PREFIX)) {
      return fromEnvironment(trimmed.substring(ENVIRONMENT_PREFIX.length()));
    }
    if (PROMPT.equalsIgnoreCase(trimmed)) {
      return fromTerminal();
    }
    throw ToolFailure.INVALID_ARGUMENT.raise(
        "'source' names where the server should read the key from, not the key itself. Use"
            + " file:/path/to/key, env:VARIABLE_NAME, or prompt.");
  }

  /**
   * Refuses a source that is itself a key.
   *
   * <p>An agent handed this tool will sometimes try the obvious thing and paste the key. By then
   * the secret is already in the conversation, so the message says to treat it as compromised
   * rather than merely correcting the argument.
   */
  private static void refuseKeyMaterialAsSource(String source) {
    boolean looksLikeAKey =
        source.toLowerCase(Locale.ROOT).startsWith(NSEC_PREFIX)
            || (source.length() == 64 && source.chars().allMatch(KeyImportSource::isHexDigit));
    if (looksLikeAKey) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "That looks like a private key. Never send key material to a tool: it would be written"
              + " to the conversation log and may reach a third-party API. Treat this key as"
              + " compromised and replace it. To import a key, give a location instead, such as"
              + " file:/path/to/key.");
    }
  }

  private static boolean isHexDigit(int character) {
    return (character >= '0' && character <= '9')
        || (character >= 'a' && character <= 'f')
        || (character >= 'A' && character <= 'F');
  }

  private static byte[] fromFile(String path) {
    Path keyFile = Path.of(path);
    if (!Files.isReadable(keyFile)) {
      throw ToolFailure.INVALID_ARGUMENT.raise("The server cannot read a key file at " + path);
    }
    try {
      return decode(Files.readString(keyFile).trim());
    } catch (IOException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise("Could not read the key file at " + path);
    }
  }

  private static byte[] fromEnvironment(String variableName) {
    String value = System.getenv(variableName);
    if (value == null || value.isBlank()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "The server has no environment variable called " + variableName);
    }
    return decode(value.trim());
  }

  /**
   * Reads from the server's own terminal, where the agent cannot see.
   *
   * <p>Only possible when a console is attached. A server an MCP host launched has its standard
   * streams wired to the protocol, so there is nowhere private to type, and saying so is better
   * than reading the key off a stream the agent is also holding.
   */
  private static byte[] fromTerminal() {
    Console console = System.console();
    if (console == null) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "This server has no terminal to prompt at, because its input and output belong to the"
              + " MCP host. Import from a file instead: file:/path/to/key.");
    }
    char[] typed = console.readPassword("Paste the private key to import (it will not be shown): ");
    try {
      return decode(new String(typed).trim());
    } finally {
      Arrays.fill(typed, '\0');
    }
  }

  private static byte[] decode(String key) {
    if (key.isEmpty()) {
      throw ToolFailure.INVALID_ARGUMENT.raise("The source held no key");
    }
    try {
      return key.startsWith(NSEC_PREFIX)
          ? HexFormat.of().parseHex(Bech32.fromBech32(key))
          : HexFormat.of().parseHex(key);
    } catch (Exception e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "The source did not hold a private key in hex or nsec form");
    }
  }

  /**
   * Overwrite a key file so the imported key does not sit on disk in plaintext.
   *
   * @param source the source that was imported from
   * @return true when a file was shredded
   */
  public static boolean shred(@NonNull String source) {
    if (!source.trim().startsWith(FILE_PREFIX)) {
      return false;
    }
    Path keyFile = Path.of(source.trim().substring(FILE_PREFIX.length()));
    try {
      long length = Files.size(keyFile);
      Files.write(keyFile, new byte[(int) length]);
      Files.delete(keyFile);
      return true;
    } catch (IOException e) {
      log.warn("Could not shred the key file at {}: {}", keyFile, e.getMessage());
      return false;
    }
  }

  /**
   * The character set a key file is expected in, for callers writing one.
   *
   * @return UTF-8
   */
  public static java.nio.charset.Charset charset() {
    return StandardCharsets.UTF_8;
  }
}
