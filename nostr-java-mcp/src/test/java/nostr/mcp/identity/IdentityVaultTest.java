package nostr.mcp.identity;

import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the vault signs without ever handing out the key that signs. */
class IdentityVaultTest {

  // Verifies the vault unlocks the keys its source holds and reports them by alias.
  @Test
  void everyKeyTheSourceHoldsIsUnlocked() {
    try (IdentityVault vault = vaultOf("personal", "project-bot")) {
      assertEquals(
          List.of("personal", "project-bot"),
          vault.list().stream().map(IdentitySummary::alias).toList());
    }
  }

  // Verifies a summary carries only public material, which is what makes "no tool can return a
  // key" a property of the type rather than a rule to remember.
  @Test
  void aSummaryCarriesOnlyPublicMaterial() {
    try (IdentityVault vault = vaultOf("personal")) {
      IdentitySummary summary = vault.list().getFirst();

      assertEquals("personal", summary.alias());
      assertTrue(summary.npub().startsWith("npub"));
      assertEquals(64, summary.publicKey().length());
      assertEquals(3, IdentitySummary.class.getRecordComponents().length);
    }
  }

  // Verifies signing happens inside the vault: the caller gets a signed event and no way to
  // reach the key that signed it.
  @Test
  void signingHappensInsideTheVault() {
    try (IdentityVault vault = vaultOf("personal")) {
      GenericEvent event = noteBy(vault.publicKeyOf("personal"));

      vault.signAs("personal", event);

      assertNotNull(event.getSignature(), "the event was not signed");
    }
  }

  // Verifies naming an unknown identity lists the ones that exist, so an agent can correct
  // itself rather than guessing again.
  @Test
  void anUnknownIdentityListsTheKnownAliases() {
    try (IdentityVault vault = vaultOf("personal")) {
      IdentityUnknownException thrown =
          assertThrows(IdentityUnknownException.class, () -> vault.publicKeyOf("nobody"));

      assertTrue(thrown.getMessage().contains("personal"));
      assertEquals(List.of("personal"), List.copyOf(thrown.getKnownAliases()));
    }
  }

  // Verifies a lone identity becomes the default, since requiring a caller to name the only
  // possible choice would be pedantry.
  @Test
  void aLoneIdentityBecomesTheDefault() {
    try (IdentityVault vault = vaultOf("personal")) {
      assertEquals("personal", vault.defaultAlias().orElseThrow());
    }
  }

  // Verifies several identities with no explicit default stay ambiguous, because guessing which
  // account to post from is a public and irreversible mistake.
  @Test
  void severalIdentitiesWithNoChoiceStayAmbiguous() {
    try (IdentityVault vault = vaultOf("personal", "project-bot")) {
      assertTrue(vault.defaultAlias().isEmpty());
    }
  }

  // Verifies an explicit default is honoured when it names a real identity.
  @Test
  void anExplicitDefaultIsHonoured() {
    try (IdentityVault vault =
        new IdentityVault(sourceHolding("personal", "project-bot"), "project-bot")) {
      assertEquals("project-bot", vault.defaultAlias().orElseThrow());
    }
  }

  // Verifies a default naming an identity that does not exist fails at startup rather than at
  // the first signing attempt, when an agent is already mid-task.
  @Test
  void aDefaultNamingNothingFailsAtStartup() {
    assertThrows(
        IdentityUnknownException.class,
        () -> new IdentityVault(sourceHolding("personal"), "absent").close());
  }

  // Verifies closing the vault wipes its key material, shortening the window in which a heap
  // dump would yield a key.
  @Test
  void closingWipesTheKeyMaterial() {
    byte[] keyMaterial = randomKey();

    new IdentityVault(sourceOf(Map.of("personal", keyMaterial)), null).close();

    assertTrue(allZero(keyMaterial), "the key material survived close()");
  }

  // Verifies an empty keystore is an ordinary reportable state rather than a failure, since a
  // first run legitimately has no keys yet.
  @Test
  void anEmptyKeystoreIsReportedRatherThanFatal() {
    try (IdentityVault vault = new IdentityVault(sourceOf(Map.of()), null)) {
      assertTrue(vault.isEmpty());
      assertTrue(vault.list().isEmpty());
      assertTrue(vault.defaultAlias().isEmpty());
    }
  }

  // Verifies an identity can be looked up by alias for addressing, without unlocking signing.
  @Test
  void anIdentityCanBeFoundByAlias() {
    try (IdentityVault vault = vaultOf("personal")) {
      assertTrue(vault.find("personal").isPresent());
      assertFalse(vault.find("nobody").isPresent());
    }
  }

  private IdentityVault vaultOf(String... aliases) {
    return new IdentityVault(sourceHolding(aliases), null);
  }

  private KeySource sourceHolding(String... aliases) {
    LinkedHashMap<String, byte[]> keys = new LinkedHashMap<>();
    for (String alias : aliases) {
      keys.put(alias, randomKey());
    }
    return sourceOf(keys);
  }

  /** A source holding exactly what a test hands it. */
  private KeySource sourceOf(Map<String, byte[]> keys) {
    return new KeySource() {
      @Override
      public Map<String, byte[]> loadKeys() {
        return keys;
      }

      @Override
      public String type() {
        return "test";
      }
    };
  }

  private byte[] randomKey() {
    return HexFormat.of()
        .parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString());
  }

  private GenericEvent noteBy(PublicKey author) {
    return GenericEvent.builder().pubKey(author).kind(1).content("signed in the vault").build();
  }

  private boolean allZero(byte[] key) {
    for (byte b : key) {
      if (b != 0) {
        return false;
      }
    }
    return true;
  }
}
