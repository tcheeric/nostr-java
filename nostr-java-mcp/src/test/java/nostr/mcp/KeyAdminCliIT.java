package nostr.mcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the key-admin CLI as a person does, from a real command line.
 *
 * <p>The unit tests exercise the CLI against an in-memory store, which cannot show that the same
 * jar serves both roles, that the encrypted keystore is actually written to disk, or that a
 * server started afterwards finds the key. Those are the claims the ticket makes, so they are
 * checked by running the real entry point in a separate JVM.
 */
class KeyAdminCliIT {

  private static final String PASSPHRASE_VARIABLE = "NOSTR_MCP_KEYSTORE_PASSPHRASE";
  private static final String PASSPHRASE = "test-passphrase";

  @TempDir Path keystoreDirectory;

  // Verifies the same jar that serves MCP also creates a key, and that the key lands in the
  // keystore file rather than only in a message.
  @Test
  void theSameEntryPointCreatesAKeyOnDisk() throws Exception {
    CommandResult created = runCli("keygen", "personal");

    assertEquals(0, created.exitCode(), created.output());
    assertTrue(created.output().contains("npub1"), created.output());
    assertTrue(Files.exists(keystorePath()), "the keystore file was not written");

    CommandResult listed = runCli("list");
    assertTrue(listed.output().contains("personal"), listed.output());
  }

  // Verifies the created private key never reaches standard output, which is the whole reason
  // key administration is a command rather than a tool.
  @Test
  void theCreatedPrivateKeyIsNeverPrinted() throws Exception {
    CommandResult created = runCli("keygen", "personal");

    assertFalse(created.output().contains("nsec1"), created.output());
  }

  // Verifies the keystore is created readable only by its owner, since the passphrase is the
  // only other thing protecting it.
  @Test
  void theKeystoreIsReadableOnlyByItsOwner() throws Exception {
    runCli("keygen", "personal");

    assertEquals("rw-------", posixPermissions(keystorePath()));
  }

  // Verifies removing an identity really removes it, so a later server cannot sign with it.
  @Test
  void aRemovedIdentityIsGoneFromTheKeystore() throws Exception {
    runCli("keygen", "personal");

    CommandResult removed = runCli("remove", "personal");
    assertEquals(0, removed.exitCode(), removed.output());

    CommandResult listed = runCli("list");
    assertFalse(listed.output().contains("personal"), listed.output());
  }

  // Verifies a server bound to an identity that does not exist refuses to start and says how to
  // create it, rather than starting and failing when an agent first tries to post.
  @Test
  void aServerBoundToAMissingIdentityRefusesToStart() throws Exception {
    runCli("keygen", "personal");

    CommandResult server = runServerBoundTo("typo");

    assertNotEquals(0, server.exitCode(), server.output());
    assertTrue(server.output().contains("typo"), server.output());
    assertTrue(server.output().contains("keygen"), server.output());
  }

  private String posixPermissions(Path file) throws IOException {
    return java.nio.file.attribute.PosixFilePermissions.toString(
        Files.getPosixFilePermissions(file));
  }

  private Path keystorePath() {
    return keystoreDirectory.resolve("keys.p12");
  }

  private CommandResult runCli(String... commands) throws Exception {
    return run(processArguments(List.of(commands)));
  }

  /**
   * Starts the server bound to an alias and waits for it to fail.
   *
   * <p>A successful start would block forever by design, so this is only used for the failure
   * path, with a timeout so a regression that lets it start is a test failure rather than a hang.
   */
  private CommandResult runServerBoundTo(String alias) throws Exception {
    return run(processArguments(List.of(), "-Dnostr.mcp.identity=" + alias));
  }

  private List<String> processArguments(List<String> commands, String... extraProperties) {
    List<String> arguments = new ArrayList<>();
    arguments.add("java");
    arguments.add("-Dnostr.mcp.keystore.type=encrypted-file");
    arguments.add("-Dnostr.mcp.keystore.path=" + keystorePath());
    arguments.add("-Dnostr.mcp.relays.read=ws://localhost:1");
    arguments.addAll(List.of(extraProperties));
    arguments.add("-cp");
    arguments.add(System.getProperty("java.class.path"));
    arguments.add(NostrMcpApplication.class.getName());
    arguments.addAll(commands);
    return arguments;
  }

  private CommandResult run(List<String> arguments) throws Exception {
    Process process = withPassphrase(new ProcessBuilder(arguments).redirectErrorStream(true)).start();
    process.getOutputStream().close();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    if (!process.waitFor(60, TimeUnit.SECONDS)) {
      process.destroyForcibly();
      throw new IllegalStateException("The process did not exit:\n" + output);
    }
    return new CommandResult(process.exitValue(), output);
  }

  private record CommandResult(int exitCode, String output) {}

  /** The encrypted keystore refuses to open without one, so every run supplies the same value. */
  private ProcessBuilder withPassphrase(ProcessBuilder builder) {
    builder.environment().put(PASSPHRASE_VARIABLE, PASSPHRASE);
    return builder;
  }
}
