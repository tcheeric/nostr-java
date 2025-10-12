package nostr.api.unit;

import nostr.api.NIP04;
import nostr.base.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for NIP-04 (Encrypted Direct Messages).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Encryption/decryption round-trip correctness</li>
 *   <li>Error handling for invalid inputs</li>
 *   <li>Edge cases (empty messages, special characters, large content)</li>
 *   <li>Event structure validation</li>
 * </ul>
 */
public class NIP04Test {

  private Identity sender;
  private Identity recipient;

  @BeforeEach
  void setup() {
    sender = Identity.generateRandomIdentity();
    recipient = Identity.generateRandomIdentity();
  }

  @Test
  public void testCreateAndDecryptDirectMessage() {
    String content = "hello";

    NIP04 nip04 = new NIP04(sender, recipient.getPublicKey());
    nip04.createDirectMessageEvent(content);

    GenericEvent event = nip04.getEvent();
    assertEquals(Kind.ENCRYPTED_DIRECT_MESSAGE.getValue(), event.getKind());
    assertTrue(event.getTags().stream().anyMatch(t -> t instanceof PubKeyTag));

    String decrypted = NIP04.decrypt(recipient, event);
    assertEquals(content, decrypted);
  }

  @Test
  public void testEncryptDecryptRoundtrip() {
    String originalMessage = "This is a secret message!";

    // Encrypt the message
    String encrypted = NIP04.encrypt(sender, originalMessage, recipient.getPublicKey());

    // Verify it's encrypted (not plaintext)
    assertNotNull(encrypted);
    assertNotEquals(originalMessage, encrypted);
    assertTrue(encrypted.contains("?iv="), "Encrypted message should contain IV separator");

    // Decrypt and verify
    String decrypted = NIP04.decrypt(recipient, encrypted, sender.getPublicKey());
    assertEquals(originalMessage, decrypted);
  }

  @Test
  public void testSenderCanDecryptOwnMessage() {
    String content = "Message from sender";

    NIP04 nip04 = new NIP04(sender, recipient.getPublicKey());
    nip04.createDirectMessageEvent(content);

    GenericEvent event = nip04.getEvent();

    // Sender should be able to decrypt their own message
    String decryptedBySender = NIP04.decrypt(sender, event);
    assertEquals(content, decryptedBySender);

    // Recipient should also be able to decrypt
    String decryptedByRecipient = NIP04.decrypt(recipient, event);
    assertEquals(content, decryptedByRecipient);
  }

  @Test
  public void testDecryptWithWrongRecipientFails() {
    String content = "Secret message";

    NIP04 nip04 = new NIP04(sender, recipient.getPublicKey());
    nip04.createDirectMessageEvent(content);

    GenericEvent event = nip04.getEvent();

    // Create unrelated third party
    Identity thirdParty = Identity.generateRandomIdentity();

    // Third party attempting to decrypt should fail
    assertThrows(RuntimeException.class, () -> NIP04.decrypt(thirdParty, event),
        "Unrelated party should not be able to decrypt");
  }

  @Test
  public void testEncryptEmptyMessage() {
    String emptyContent = "";

    NIP04 nip04 = new NIP04(sender, recipient.getPublicKey());
    nip04.createDirectMessageEvent(emptyContent);

    GenericEvent event = nip04.getEvent();

    // Should successfully encrypt and decrypt empty string
    String decrypted = NIP04.decrypt(recipient, event);
    assertEquals(emptyContent, decrypted);
  }

  @Test
  public void testEncryptLargeMessage() {
    // Create a large message (10KB)
    StringBuilder largeContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeContent.append("This is line ").append(i).append(" of a very long message.\n");
    }
    String content = largeContent.toString();

    NIP04 nip04 = new NIP04(sender, recipient.getPublicKey());
    nip04.createDirectMessageEvent(content);

    GenericEvent event = nip04.getEvent();

    // Should handle large messages
    String decrypted = NIP04.decrypt(recipient, event);
    assertEquals(content, decrypted);
    assertTrue(decrypted.length() > 10000, "Decrypted message should preserve length");
  }

  @Test
  public void testEncryptSpecialCharacters() {
    // Test with Unicode, emojis, and special characters
    String content = "Hello 世界! 🔐 Encrypted: \"quotes\" 'apostrophes' <tags> & symbols €£¥";

    NIP04 nip04 = new NIP04(sender, recipient.getPublicKey());
    nip04.createDirectMessageEvent(content);

    GenericEvent event = nip04.getEvent();

    // Should preserve all special characters
    String decrypted = NIP04.decrypt(recipient, event);
    assertEquals(content, decrypted);
  }

  @Test
  public void testDecryptInvalidEventKindThrowsException() {
    // Create a non-DM event
    Identity identity = Identity.generateRandomIdentity();
    GenericEvent invalidEvent = new GenericEvent(identity.getPublicKey(), Kind.TEXT_NOTE);
    invalidEvent.setContent("Not encrypted");

    // Attempting to decrypt wrong kind should fail
    assertThrows(IllegalArgumentException.class, () -> NIP04.decrypt(sender, invalidEvent),
        "Should throw IllegalArgumentException for non-kind-4 event");
  }
}
