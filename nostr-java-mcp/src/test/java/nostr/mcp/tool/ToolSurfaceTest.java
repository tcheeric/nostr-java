package nostr.mcp.tool;

import nostr.client.relay.RelayPool;
import nostr.id.Identity;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentitySummary;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.relay.RelayDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the real tool surface, pinned separately for bound and unbound servers.
 *
 * <p>Asserted over the tools a host actually receives rather than over stand-ins, because a
 * golden file of stubs pins the test's own fixture and would stay green while the real surface
 * changed underneath it.
 */
class ToolSurfaceTest {

  private static final Path UNBOUND_GOLDEN = Path.of("src/test/resources/tool-list-default.txt");
  private static final Path BOUND_GOLDEN = Path.of("src/test/resources/tool-list-bound.txt");

  // Verifies an ordinary multi-identity server exposes exactly the golden tool list.
  @Test
  void theUnboundSurfaceMatchesItsGoldenFile() {
    try (RelayPool relayPool = emptyPool();
        IdentityVault vault = vault(IdentityBinding.unbound())) {

      assertEquals(
          readGolden(UNBOUND_GOLDEN),
          String.join("\n", ToolSurface.forServer(directory(), relayPool, vault, QueryLimits.defaults(), Clock.systemUTC()).registeredNames()));
    }
  }

  // Verifies a bound server's tool list is pinned separately, so a tool that should disappear
  // under binding is asserted to be absent rather than assumed to be. The two lists are equal
  // today because no keystore-mutating tool exists yet; the separate file is what will make the
  // first one that does show up here as a diff instead of silently reaching a bound server.
  @Test
  void theBoundSurfaceMatchesItsOwnGoldenFile() {
    try (RelayPool relayPool = emptyPool();
        IdentityVault vault = vault(IdentityBinding.to("personal"))) {

      assertEquals(
          readGolden(BOUND_GOLDEN),
          String.join("\n", ToolSurface.forServer(directory(), relayPool, vault, QueryLimits.defaults(), Clock.systemUTC()).registeredNames()));
    }
  }

  // Verifies a bound server reports only the identity it is bound to, which is the guarantee an
  // operator is buying: the other keys are not merely hidden, they were never unlocked.
  @Test
  void aBoundServerReportsOnlyItsBoundIdentity() {
    try (IdentityVault vault = vault(IdentityBinding.to("personal"))) {
      List<String> aliases = vault.list().stream().map(IdentitySummary::alias).toList();

      assertEquals(List.of("personal"), aliases);
    }
  }

  // Verifies binding leaves no identity ambiguity, since there is nothing left to choose.
  @Test
  void aBoundServerHasAnUnambiguousDefault() {
    try (IdentityVault vault = vault(IdentityBinding.to("project-bot"))) {
      assertTrue(vault.defaultAlias().isPresent());
      assertEquals("project-bot", vault.defaultAlias().orElseThrow());
    }
  }

  private RelayDirectory directory() {
    return new RelayDirectory(
        Map.of(
            RelayDirectory.READ, List.of("wss://relay.example"),
            RelayDirectory.WRITE, List.of("wss://relay.example")));
  }

  private RelayPool emptyPool() {
    return new RelayPool(List.of(), relayUri -> {
      throw new IOException("no relays in this test");
    });
  }

  private IdentityVault vault(IdentityBinding binding) {
    return new IdentityVault(sourceHolding("personal", "project-bot"), null, binding);
  }

  private KeySource sourceHolding(String... aliases) {
    Map<String, byte[]> keys = new LinkedHashMap<>();
    for (String alias : aliases) {
      keys.put(
          alias,
          HexFormat.of().parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString()));
    }
    return new KeySource() {
      @Override
      public Map<String, byte[]> loadKeys(IdentityBinding binding) {
        Map<String, byte[]> permitted = new LinkedHashMap<>();
        keys.forEach(
            (alias, key) -> {
              if (binding.permitted(keys.keySet()).contains(alias)) {
                permitted.put(alias, key);
              }
            });
        return permitted;
      }

      @Override
      public String type() {
        return "test";
      }
    };
  }

  private String readGolden(Path goldenFile) {
    try {
      return Files.readString(goldenFile).strip();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + goldenFile, e);
    }
  }
}
