package nostr.mcp.argument;

import nostr.id.Identity;
import nostr.mcp.tool.ToolException;
import nostr.mcp.tool.ToolFailure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies identifiers are accepted in whichever form a user pasted, and refused clearly. */
class NostrIdentifierTest {

  // Verifies a hex public key is accepted, since that is what a relay speaks.
  @Test
  void aHexPublicKeyIsAccepted() {
    String hex = Identity.generateRandomIdentity().getPublicKey().toHexString();

    assertEquals(hex, NostrIdentifier.publicKey("pubkey", hex).hex());
  }

  // Verifies an npub is accepted and decoded to the same key, since that is what a person
  // copies out of a client.
  @Test
  void anNpubDecodesToTheSameKey() {
    Identity identity = Identity.generateRandomIdentity();
    String hex = identity.getPublicKey().toHexString();
    String npub = identity.getPublicKey().toBech32String();

    assertEquals(hex, NostrIdentifier.publicKey("pubkey", npub).hex());
  }

  // Verifies uppercase hex and surrounding whitespace are tolerated, because a pasted value
  // frequently carries both and neither changes the key.
  @Test
  void pastedFormattingIsTolerated() {
    String hex = Identity.generateRandomIdentity().getPublicKey().toHexString();

    assertEquals(hex, NostrIdentifier.publicKey("pubkey", "  " + hex.toUpperCase() + "\n").hex());
  }

  // Verifies an nsec in a public argument is refused with advice to rotate, since by the time a
  // tool sees one the key has already been exposed to the model.
  @Test
  void aPrivateKeyInAPublicArgumentIsRefusedWithAdvice() {
    String nsec = Identity.generateRandomIdentity().getPrivateKey().toBech32String();

    ToolException refused =
        assertThrows(ToolException.class, () -> NostrIdentifier.publicKey("pubkey", nsec));

    assertEquals(ToolFailure.INVALID_ARGUMENT, refused.getFailure());
    assertTrue(refused.getMessage().contains("compromised"), refused.getMessage());
  }

  // Verifies an npub given where an event id belongs names the confusion, since both forms are
  // bech32 of the same length and "decode failed" would not help.
  @Test
  void anNpubWhereAnEventIdBelongsNamesTheMistake() {
    String npub = Identity.generateRandomIdentity().getPublicKey().toBech32String();

    ToolException refused =
        assertThrows(ToolException.class, () -> NostrIdentifier.eventId("id", npub));

    assertTrue(refused.getMessage().contains("note"), refused.getMessage());
  }

  // Verifies a value that is neither form is refused rather than silently truncated.
  @Test
  void somethingThatIsNeitherFormIsRefused() {
    ToolException refused =
        assertThrows(ToolException.class, () -> NostrIdentifier.publicKey("pubkey", "wat"));

    assertEquals(ToolFailure.INVALID_ARGUMENT, refused.getFailure());
  }

  // Verifies an empty argument is named, so an agent that omitted a value learns which one.
  @Test
  void anEmptyArgumentIsNamed() {
    ToolException refused =
        assertThrows(ToolException.class, () -> NostrIdentifier.publicKey("author", "  "));

    assertTrue(refused.getMessage().contains("author"), refused.getMessage());
  }

  // Verifies hex of the wrong length is refused, since a truncated key would otherwise query
  // for events that can never match.
  @Test
  void hexOfTheWrongLengthIsRefused() {
    assertThrows(ToolException.class, () -> NostrIdentifier.publicKey("pubkey", "abcdef"));
  }

  // Verifies the decoded identifier can be used as a public key for addressing.
  @Test
  void aDecodedIdentifierIsUsableAsAPublicKey() {
    Identity identity = Identity.generateRandomIdentity();

    assertEquals(
        identity.getPublicKey().toHexString(),
        NostrIdentifier.publicKey("pubkey", identity.getPublicKey().toBech32String())
            .asPublicKey()
            .toHexString());
  }
}
