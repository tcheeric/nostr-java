package nostr.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nostr.crypto.schnorr.Schnorr;
import org.junit.jupiter.api.Test;

class MessageCipherTest {

  @Test
  void testMessageCipher04EncryptDecrypt() throws Exception {
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
  void testMessageCipher44EncryptDecrypt() throws Exception {
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
