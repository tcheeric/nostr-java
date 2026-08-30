package nostr.mcp.tool;

import nostr.client.relay.RelayPool;
import nostr.id.Identity;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityLifecycle;
import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.identity.IdentitySummary;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.subscription.SubscriptionLimits;
import nostr.mcp.subscription.SubscriptionRegistry;
import nostr.mcp.write.RateLimit;
import nostr.mcp.write.WriteGuard;
import nostr.mcp.write.WritePolicy;
import nostr.mcp.relay.RelayDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
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
  private static final Path READ_ONLY_GOLDEN =
      Path.of("src/test/resources/tool-list-read-only.txt");
  private static final Path NO_MUTATION_GOLDEN =
      Path.of("src/test/resources/tool-list-no-identity-mutation.txt");

  // Verifies an ordinary multi-identity server exposes exactly the golden tool list.
  @Test
  void theUnboundSurfaceMatchesItsGoldenFile() {
    try (RelayPool relayPool = emptyPool();
        IdentityVault vault = vault(IdentityBinding.unbound())) {

      assertEquals(
          readGolden(UNBOUND_GOLDEN),
          String.join("\n", surfaceOf(relayPool, vault, WritePolicy.CONFIRM).registeredNames()));
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
          String.join("\n", surfaceOf(relayPool, vault, WritePolicy.CONFIRM).registeredNames()));
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

  // Verifies a read-only server registers no write tool at all, pinned by its own golden file.
  // A refusal an agent can see is an invitation to rephrase; an absent tool is not.
  @Test
  void aReadOnlyServerRegistersNoWriteTools() {
    try (RelayPool relayPool = emptyPool();
        IdentityVault vault = vault(IdentityBinding.unbound())) {

      assertEquals(
          readGolden(READ_ONLY_GOLDEN),
          String.join("\n", surfaceOf(relayPool, vault, WritePolicy.DENY).registeredNames()));
    }
  }

  // Verifies the allowing policy exposes the same tools as the confirming one, since the
  // difference between them is what a call does, not which tools exist.
  @Test
  void allowingWritesExposesTheSameToolsAsConfirming() {
    try (RelayPool relayPool = emptyPool();
        IdentityVault vault = vault(IdentityBinding.unbound())) {

      assertEquals(
          surfaceOf(relayPool, vault, WritePolicy.CONFIRM).registeredNames(),
          surfaceOf(relayPool, vault, WritePolicy.ALLOW).registeredNames());
    }
  }

  // Verifies a server that may write but may not touch the keystore exposes no lifecycle tool,
  // which is the separation identity-policy exists to provide.
  @Test
  void identityMutationCanBeDeniedWhileWritingIsAllowed() {
    try (RelayPool relayPool = emptyPool();
        IdentityVault vault = vault(IdentityBinding.unbound())) {

      assertEquals(
          readGolden(NO_MUTATION_GOLDEN),
          String.join(
              "\n",
              surfaceOf(relayPool, vault, WritePolicy.CONFIRM, IdentityPolicy.DENY)
                  .registeredNames()));
    }
  }

  // Verifies a read-only server cannot mutate the keystore either, since identity-policy is
  // capped by write-policy and a server that cannot post should not be able to destroy a key.
  @Test
  void aReadOnlyServerCannotMutateTheKeystoreEither() {
    assertEquals(
        IdentityPolicy.DENY, IdentityPolicy.fromConfiguredValue("allow", WritePolicy.DENY));
  }

  // Verifies a backend that cannot be written to offers no lifecycle tools, since a tool that
  // could only ever fail is worse than one that is absent.
  @Test
  void aBackendThatCannotBeAdministeredOffersNoLifecycleTools() {
    try (RelayPool relayPool = emptyPool();
        IdentityVault vault = vault(IdentityBinding.unbound())) {

      NostrToolRegistry registry =
          ToolSurface.forServer(
              directory(),
              relayPool,
              vault,
              QueryLimits.defaults(),
              Clock.systemUTC(),
              writeGuard(relayPool, vault, WritePolicy.CONFIRM),
              WritePolicy.CONFIRM,
              null,
              IdentityPolicy.ALLOW,
              subscriptionRegistry(relayPool));

      assertEquals(readGolden(NO_MUTATION_GOLDEN), String.join("\n", registry.registeredNames()));
    }
  }

  /**
   * Derives the identity policy from the write policy exactly as configuration does, so a test
   * cannot assemble a combination a real deployment could never produce.
   */
  private NostrToolRegistry surfaceOf(RelayPool relayPool, IdentityVault vault, WritePolicy policy) {
    return surfaceOf(
        relayPool, vault, policy, IdentityPolicy.fromConfiguredValue(null, policy));
  }

  private NostrToolRegistry surfaceOf(
      RelayPool relayPool, IdentityVault vault, WritePolicy policy, IdentityPolicy identityPolicy) {
    return ToolSurface.forServer(
        directory(),
        relayPool,
        vault,
        QueryLimits.defaults(),
        Clock.systemUTC(),
        writeGuard(relayPool, vault, policy),
        policy,
        new IdentityLifecycle(vault, new InMemoryStore()),
        identityPolicy,
        subscriptionRegistry(relayPool));
  }

  private SubscriptionRegistry subscriptionRegistry(RelayPool relayPool) {
    return new SubscriptionRegistry(
        relayPool, SubscriptionLimits.defaults(), Clock.systemUTC(), subscriptionId -> {});
  }

  private WriteGuard writeGuard(RelayPool relayPool, IdentityVault vault, WritePolicy policy) {
    return new WriteGuard(
        relayPool, vault, policy, new RateLimit(100, Duration.ofMinutes(1), Clock.systemUTC()));
  }

  /** A store that accepts changes without a keystore, so the surface is what is under test. */
  private static final class InMemoryStore implements nostr.mcp.identity.IdentityStore {
    private final Map<String, byte[]> keys = new LinkedHashMap<>();

    @Override
    public void store(String alias, byte[] keyMaterial) {
      keys.put(alias, keyMaterial.clone());
    }

    @Override
    public List<String> aliases() {
      return List.copyOf(keys.keySet());
    }

    @Override
    public boolean remove(String alias) {
      return keys.remove(alias) != null;
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
