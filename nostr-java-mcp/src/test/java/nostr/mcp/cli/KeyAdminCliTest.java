package nostr.mcp.cli;

import nostr.mcp.identity.IdentityStore;
import nostr.mcp.identity.KeystoreException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies key administration works for a human and never prints a private key. */
class KeyAdminCliTest {

  private final RecordingStore store = new RecordingStore();
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();

  // Verifies keygen creates a usable identity and reports its public half.
  @Test
  void keygenCreatesAnIdentityAndReportsItsPublicKey() {
    int exitCode = run("keygen", "personal");

    assertEquals(0, exitCode);
    assertEquals(List.of("personal"), store.aliases());
    assertTrue(printed().contains("npub1"), printed());
  }

  // Verifies the generated private key never reaches the terminal, since a printed key ends up
  // in scrollback and shell history by default.
  @Test
  void keygenNeverPrintsThePrivateKey() {
    run("keygen", "personal");

    String privateKeyHex = HexFormat.of().formatHex(store.keyFor("personal"));
    assertFalse(printed().contains(privateKeyHex), "the private key was printed");
    assertFalse(printed().contains("nsec"), "an nsec was printed");
  }

  // Verifies a key is read from standard input rather than an argument, since arguments are
  // visible to every user on the host through the process table.
  @Test
  void importReadsAHexKeyFromStandardInput() {
    byte[] key = randomKeyBytes();

    int exitCode = runWithInput(HexFormat.of().formatHex(key), "import", "personal");

    assertEquals(0, exitCode);
    assertArrayEquals(key, store.keyFor("personal"));
  }

  // Verifies the nsec form a person actually holds is accepted, not only raw hex.
  @Test
  void importAcceptsAnNsec() {
    byte[] key = randomKeyBytes();
    String nsec = new nostr.base.PrivateKey(key).toBech32String();

    int exitCode = runWithInput(nsec, "import", "personal");

    assertEquals(0, exitCode);
    assertArrayEquals(key, store.keyFor("personal"));
  }

  // Verifies unreadable input is refused with a message about the input rather than a stack trace.
  @Test
  void importRefusesSomethingThatIsNotAKey() {
    int exitCode = runWithInput("not-a-key", "import", "personal");

    assertNotEquals(0, exitCode);
    assertTrue(printed().contains("neither hex nor an nsec"), printed());
    assertTrue(store.aliases().isEmpty());
  }

  // Verifies list shows what exists and says how to start when nothing does.
  @Test
  void listShowsTheIdentitiesAndGuidesAnEmptyKeystore() {
    assertEquals(0, run("list"));
    assertTrue(printed().contains("keygen"), printed());

    run("keygen", "personal");
    output.reset();

    assertEquals(0, run("list"));
    assertTrue(printed().contains("personal"), printed());
  }

  // Verifies removing an identity reports that it is irreversible, and that removing a name
  // that never existed is distinguishable from removing a real one.
  @Test
  void removeReportsWhetherAnythingWasActuallyRemoved() {
    run("keygen", "personal");
    output.reset();

    assertEquals(0, run("remove", "personal"));
    assertTrue(printed().contains("cannot be undone"), printed());
    assertTrue(store.aliases().isEmpty());

    output.reset();
    assertNotEquals(0, run("remove", "personal"));
    assertTrue(printed().contains("No identity called"), printed());
  }

  // Verifies an existing alias is never silently overwritten, since replacing a key destroys
  // the account it belonged to with no way back.
  @Test
  void keygenRefusesToOverwriteAnExistingAlias() {
    run("keygen", "personal");
    byte[] original = store.keyFor("personal").clone();
    output.reset();

    assertNotEquals(0, run("keygen", "personal"));
    assertArrayEquals(original, store.keyFor("personal"));
  }

  // Verifies an unknown or missing command explains the real commands rather than failing mutely.
  @Test
  void anUnknownCommandExplainsTheRealOnes() {
    assertNotEquals(0, run("frobnicate"));

    String help = printed();
    assertTrue(help.contains("keygen"), help);
    assertTrue(help.contains("import"), help);
    assertTrue(help.contains("list"), help);
    assertTrue(help.contains("remove"), help);
  }

  private int run(String... arguments) {
    return runWithInput("", arguments);
  }

  private int runWithInput(String standardInput, String... arguments) {
    InputStream original = System.in;
    System.setIn(new ByteArrayInputStream(standardInput.getBytes(StandardCharsets.UTF_8)));
    try {
      return new KeyAdminCli(store, new PrintStream(output, true, StandardCharsets.UTF_8))
          .run(List.of(arguments));
    } finally {
      System.setIn(original);
    }
  }

  private String printed() {
    return output.toString(StandardCharsets.UTF_8);
  }

  private static byte[] randomKeyBytes() {
    return HexFormat.of()
        .parseHex(nostr.id.Identity.generateRandomIdentity().getPrivateKey().toHexString());
  }

  private static void assertArrayEquals(byte[] expected, byte[] actual) {
    org.junit.jupiter.api.Assertions.assertArrayEquals(expected, actual);
  }

  /** An in-memory store, so the tests exercise the CLI rather than a keychain. */
  private static final class RecordingStore implements IdentityStore {

    private final Map<String, byte[]> keys = new LinkedHashMap<>();

    @Override
    public void store(String alias, byte[] keyMaterial) {
      if (keys.containsKey(alias)) {
        throw new KeystoreException("The keystore already holds an identity called '" + alias + "'");
      }
      keys.put(alias, keyMaterial.clone());
    }

    @Override
    public List<String> aliases() {
      return new ArrayList<>(keys.keySet());
    }

    @Override
    public boolean remove(String alias) {
      return keys.remove(alias) != null;
    }

    byte[] keyFor(String alias) {
      return keys.get(alias);
    }
  }
}
