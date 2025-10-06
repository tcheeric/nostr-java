package nostr.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nostr.crypto.schnorr.Schnorr;
import nostr.crypto.schnorr.SchnorrException;
import org.junit.jupiter.api.Test;

class MessageCipherTest {

  @Test
  // Validates that MessageCipher04 encrypts and decrypts symmetrically
  void testMessageCipher04EncryptDecrypt() throws SchnorrException {
    byte[] alicePriv = Schnorr.generatePrivateKey();
    byte[] alicePub = Schnorr.genPubKey(alicePriv);
    byte[] bobPriv = Schnorr.generatePrivateKey();
    byte[] bobPub = Schnorr.genPubKey(bobPriv);

    MessageCipher04 alice = new MessageCipher04(alicePriv, bobPub);
    String encrypted = alice.encrypt("hello");

    MessageCipher04 bob = new MessageCipher04(bobPriv, alicePub);
    String decrypted = bob.decrypt(encrypted);
    assertEquals("hello", decrypted);
  }

  @Test
  // Validates that MessageCipher44 encrypts and decrypts symmetrically
  void testMessageCipher44EncryptDecrypt() throws SchnorrException {
    byte[] alicePriv = Schnorr.generatePrivateKey();
    byte[] alicePub = Schnorr.genPubKey(alicePriv);
    byte[] bobPriv = Schnorr.generatePrivateKey();
    byte[] bobPub = Schnorr.genPubKey(bobPriv);

    MessageCipher44 alice = new MessageCipher44(alicePriv, bobPub);
    String encrypted = alice.encrypt("hi");

    MessageCipher44 bob = new MessageCipher44(bobPriv, alicePub);
    String decrypted = bob.decrypt(encrypted);
    assertEquals("hi", decrypted);
  }
}
