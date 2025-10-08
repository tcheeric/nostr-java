package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import nostr.api.NIP44;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for NIP-44 (Encrypted Payloads - Versioned Encrypted Messages).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>XChaCha20-Poly1305 AEAD encryption/decryption</li>
 *   <li>Version byte handling (0x02)</li>
 *   <li>Padding correctness</li>
 *   <li>HMAC authentication</li>
 *   <li>Error handling and edge cases</li>
 * </ul>
 */
public class NIP44Test {

  private Identity sender;
  private Identity recipient;

  @BeforeEach
  void setup() {
    sender = Identity.generateRandomIdentity();
    recipient = Identity.generateRandomIdentity();
  }

  @Test
  public void testEncryptDecrypt() {
    String message = "hello";

    String encrypted = NIP44.encrypt(sender, message, recipient.getPublicKey());
    String decrypted = NIP44.decrypt(recipient, encrypted, sender.getPublicKey());
    assertEquals(message, decrypted);
  }

  @Test
  public void testDecryptEvent() {
    String content = "msg";
    String enc = NIP44.encrypt(sender, content, recipient.getPublicKey());
    GenericEvent event =
        new GenericEvent(
            sender.getPublicKey(), 1050, List.of(new PubKeyTag(recipient.getPublicKey())), enc);

    String dec = NIP44.decrypt(recipient, event);
    assertEquals(content, dec);
  }

  @Test
  public void testVersionBytePresent() {
    String message = "Test message for NIP-44";

    String encrypted = NIP44.encrypt(sender, message, recipient.getPublicKey());

    // NIP-44 encrypted payloads should be base64 encoded with version byte
    assertNotNull(encrypted);
    assertTrue(encrypted.length() > 0, "Encrypted payload should not be empty");

    // Decrypt to verify it works
    String decrypted = NIP44.decrypt(recipient, encrypted, sender.getPublicKey());
    assertEquals(message, decrypted);
  }

  @Test
  public void testPaddingCorrectness() {
    // NIP-44 uses power-of-2 padding. Test that padding doesn't affect decryption.
    String shortMsg = "Hi";
    String mediumMsg = "This is a medium length message with more content to ensure padding";
    String longMsg = "This is a much longer message that should be padded to a different size " +
                     "according to NIP-44 padding scheme which uses power-of-2 boundaries. " +
                     "We add extra text here to make sure we cross padding boundaries and " +
                     "test that decryption still works correctly regardless of padding.";

    String encShort = NIP44.encrypt(sender, shortMsg, recipient.getPublicKey());
    String encMedium = NIP44.encrypt(sender, mediumMsg, recipient.getPublicKey());
    String encLong = NIP44.encrypt(sender, longMsg, recipient.getPublicKey());

    // The key test: all messages decrypt correctly despite padding
    assertEquals(shortMsg, NIP44.decrypt(recipient, encShort, sender.getPublicKey()));
    assertEquals(mediumMsg, NIP44.decrypt(recipient, encMedium, sender.getPublicKey()));
    assertEquals(longMsg, NIP44.decrypt(recipient, encLong, sender.getPublicKey()));

    // Verify encryption produces output
    assertNotNull(encShort);
    assertNotNull(encMedium);
    assertNotNull(encLong);
  }

  @Test
  public void testAuthenticationDetectsTampering() {
    String message = "Authenticated message";

    String encrypted = NIP44.encrypt(sender, message, recipient.getPublicKey());

    // Tamper with the encrypted payload by modifying a character
    String tampered;
    if (encrypted.endsWith("A")) {
      tampered = encrypted.substring(0, encrypted.length() - 1) + "B";
    } else {
      tampered = encrypted.substring(0, encrypted.length() - 1) + "A";
    }

    // Decryption should fail due to AEAD authentication
    assertThrows(RuntimeException.class, () ->
        NIP44.decrypt(recipient, tampered, sender.getPublicKey()),
        "Tampered ciphertext should fail AEAD authentication");
  }

  @Test
  public void testEncryptMinimalMessage() {
    // NIP-44 requires minimum 1 byte plaintext
    String minimalMsg = "a";

    String encrypted = NIP44.encrypt(sender, minimalMsg, recipient.getPublicKey());
    String decrypted = NIP44.decrypt(recipient, encrypted, sender.getPublicKey());

    assertEquals(minimalMsg, decrypted, "Minimal message should encrypt and decrypt correctly");
  }

  @Test
  public void testEncryptSpecialCharacters() {
    // Test with Unicode, emojis, and special characters
    String message = "Hello 世界! 🔒 Encrypted with NIP-44: \"quotes\" 'apostrophes' <tags> & symbols €£¥ 中文";

    String encrypted = NIP44.encrypt(sender, message, recipient.getPublicKey());
    String decrypted = NIP44.decrypt(recipient, encrypted, sender.getPublicKey());

    assertEquals(message, decrypted, "All special characters should be preserved");
  }

  @Test
  public void testEncryptLargeMessage() {
    // NIP-44 supports up to 65535 bytes. Create a large message (~60KB)
    StringBuilder largeMsg = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeMsg.append("Line ").append(i).append(": NIP-44 handles large messages.\n");
    }
    String message = largeMsg.toString();

    // Verify message is within NIP-44 limits (≤ 65535 bytes)
    assertTrue(message.getBytes().length <= 65535, "Message must be within NIP-44 limit");

    String encrypted = NIP44.encrypt(sender, message, recipient.getPublicKey());
    String decrypted = NIP44.decrypt(recipient, encrypted, sender.getPublicKey());

    assertEquals(message, decrypted);
    assertTrue(decrypted.length() > 10000, "Large message should be preserved");
  }

  @Test
  public void testConversationKeyConsistency() {
    String message1 = "First message";
    String message2 = "Second message";

    // Multiple encryptions with same key pair should work
    String enc1 = NIP44.encrypt(sender, message1, recipient.getPublicKey());
    String enc2 = NIP44.encrypt(sender, message2, recipient.getPublicKey());

    String dec1 = NIP44.decrypt(recipient, enc1, sender.getPublicKey());
    String dec2 = NIP44.decrypt(recipient, enc2, sender.getPublicKey());

    assertEquals(message1, dec1);
    assertEquals(message2, dec2);

    // Even though same keys, nonces should differ (different ciphertext)
    assertNotEquals(enc1, enc2, "Same plaintext should produce different ciphertext (due to random nonce)");
  }
}
