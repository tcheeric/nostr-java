package nostr.mcp.cli;

import lombok.NonNull;
import nostr.base.PrivateKey;
import nostr.crypto.bech32.Bech32;
import nostr.id.Identity;
import nostr.mcp.identity.IdentityStore;
import nostr.mcp.identity.KeystoreException;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

/**
 * Key administration for the human who sets the servers up.
 *
 * <p>Deliberately not a tool. In the process-per-identity deployment an agent operates one key
 * and never administers a keystore, so creating and destroying keys lives on the command line,
 * out of every agent's reach. The same jar serves both, because a separate admin artefact is one
 * more thing to version and to get out of step.
 *
 * <p>Writes to a caller-supplied stream rather than {@code System.out} so the stdio transport is
 * never in doubt: this class is used before the server exists, and a stray print into a live
 * JSON-RPC stream would corrupt the protocol.
 */
public final class KeyAdminCli {

  private static final String NSEC_PREFIX = "nsec";

  private final IdentityStore store;
  private final PrintStream output;

  /**
   * @param store the keystore to administer
   * @param output where to report results
   */
  public KeyAdminCli(@NonNull IdentityStore store, @NonNull PrintStream output) {
    this.store = store;
    this.output = output;
  }

  /**
   * Run one command.
   *
   * @param arguments the command and its operands, as given on the command line
   * @return the process exit code, zero on success
   */
  public int run(@NonNull List<String> arguments) {
    if (arguments.isEmpty()) {
      return usage();
    }
    try {
      return dispatch(arguments.getFirst(), arguments.subList(1, arguments.size()));
    } catch (KeystoreException e) {
      output.println("error: " + e.getMessage());
      return 1;
    }
  }

  private int dispatch(String command, List<String> operands) {
    return switch (command) {
      case "keygen" -> keygen(operands);
      case "import" -> importKey(operands);
      case "list" -> list();
      case "remove" -> remove(operands);
      default -> unknown(command);
    };
  }

  /**
   * Creates a keypair and reports the public half only.
   *
   * <p>The private key is generated, stored and wiped without ever being printed. A person who
   * needs a backup exports one deliberately; printing every new key to a terminal would put it
   * in a scrollback buffer and a shell history by default.
   */
  private int keygen(List<String> operands) {
    if (operands.size() != 1) {
      return misuse("keygen <alias>");
    }
    String alias = operands.getFirst();
    Identity identity = Identity.generateRandomIdentity();
    byte[] keyMaterial = identity.getPrivateKey().getRawData();
    try {
      store.store(alias, keyMaterial);
      output.println("Created identity '" + alias + "'");
      output.println("  public key: " + identity.getPublicKey().toBech32String());
      return 0;
    } finally {
      Arrays.fill(keyMaterial, (byte) 0);
    }
  }

  /**
   * Imports an existing key read from standard input.
   *
   * <p>Read from stdin rather than an argument because arguments are visible in the host's
   * process table, so a key passed that way is a key disclosed to every user on the machine.
   */
  private int importKey(List<String> operands) {
    if (operands.size() != 1) {
      return misuse("import <alias>   (the key is read from standard input)");
    }
    String alias = operands.getFirst();
    byte[] keyMaterial = readKeyFromStandardInput();
    try {
      store.store(alias, keyMaterial);
      output.println("Imported identity '" + alias + "'");
      output.println(
          "  public key: "
              + Identity.create(new PrivateKey(keyMaterial)).getPublicKey().toBech32String());
      return 0;
    } finally {
      Arrays.fill(keyMaterial, (byte) 0);
    }
  }

  private int list() {
    List<String> aliases = store.aliases();
    if (aliases.isEmpty()) {
      output.println("No identities yet. Create one with: keygen <alias>");
      return 0;
    }
    aliases.forEach(output::println);
    return 0;
  }

  /**
   * Removes a key, saying plainly that it cannot be undone.
   *
   * <p>Reports whether anything was actually removed, since "remove a name that was already
   * wrong" and "remove the account you meant to keep" look identical in a silent success.
   */
  private int remove(List<String> operands) {
    if (operands.size() != 1) {
      return misuse("remove <alias>");
    }
    String alias = operands.getFirst();
    if (!store.remove(alias)) {
      output.println("No identity called '" + alias + "'");
      return 1;
    }
    output.println("Removed identity '" + alias + "'. Without a backup this cannot be undone.");
    return 0;
  }

  private byte[] readKeyFromStandardInput() {
    try {
      String text = new String(System.in.readAllBytes()).trim();
      if (text.isEmpty()) {
        throw new KeystoreException("No key was given on standard input");
      }
      return text.startsWith(NSEC_PREFIX) ? decodeNsec(text) : HexFormat.of().parseHex(text);
    } catch (IllegalArgumentException e) {
      throw new KeystoreException("The key on standard input is neither hex nor an nsec", e);
    } catch (java.io.IOException e) {
      throw new KeystoreException("Could not read the key from standard input", e);
    }
  }

  /**
   * Decodes the nsec form a person actually has.
   *
   * <p>{@code new PrivateKey(String)} parses hex only, so an nsec must be converted first rather
   * than handed straight over: doing otherwise fails with a hex error on a perfectly valid key.
   */
  private byte[] decodeNsec(String nsec) {
    try {
      return HexFormat.of().parseHex(Bech32.fromBech32(nsec));
    } catch (Exception e) {
      throw new KeystoreException("The key on standard input is not a valid nsec", e);
    }
  }

  private int unknown(String command) {
    output.println("Unknown command '" + command + "'");
    return usage();
  }

  private int misuse(String form) {
    output.println("usage: " + form);
    return 2;
  }

  private int usage() {
    output.println("""
        Manage the identities this MCP server signs with.

        usage: java -jar nostr-java-mcp.jar <command>

          keygen <alias>   create a new identity
          import <alias>   import a key read from standard input
          list             show the identities in the keystore
          remove <alias>   forget an identity

        With no command the MCP server starts and serves over stdio.""");
    return 2;
  }
}
