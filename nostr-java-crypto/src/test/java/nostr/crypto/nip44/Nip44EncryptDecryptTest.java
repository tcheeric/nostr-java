package nostr.crypto.nip44;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cross-implementation test vectors for NIP-44 decryption compatibility.
 * 
 * These test vectors are from the official NIP-44 specification test vectors
 * (https://github.com/paulmillr/nip44/blob/main/nip44.vectors.json) and verify
 * that this Java implementation can decrypt messages encrypted by
 * JavaScript implementations (nostr-tools) and other language implementations.
 * 
 * This ensures DM interoperability across different Nostr client implementations.
 */
public class Nip44EncryptDecryptTest {

  /**
   * Test that decryption correctly recovers plaintext from official test vectors.
   * This tests the decrypt-only path to ensure compatibility with messages encrypted
   * by other implementations.
   */
  @Test
  public void testDecryptWithOfficialVectors() throws Exception {
    // Vector 1: Single character
    assertDecrypt(
        "c41c775356fd92eadc63ff5a0dc1da211b268cbea22316767095b2871ea1412d",
        "AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABee0G5VSK0/9YypIObAtDKfYEAjD35uVkHyB0F4DwrcNaCXlCWZKaArsGrY6M9wnuTMxWfp1RTN9Xga8no+kF5Vsb",
        "a"
    );

    // Vector 2: Emoji
    assertDecrypt(
        "c41c775356fd92eadc63ff5a0dc1da211b268cbea22316767095b2871ea1412d",
        "AvAAAAAAAAAAAAAAAAAAAPAAAAAAAAAAAAAAAAAAAAAPSKSK6is9ngkX2+cSq85Th16oRTISAOfhStnixqZziKMDvB0QQzgFZdjLTPicCJaV8nDITO+QfaQ61+KbWQIOO2Yj",
        "🍕🫃"
    );

    // Vector 3: Complex Unicode
    assertDecrypt(
        "3e2b52a63be47d34fe0a80e34e73d436d6963bc8f39827f327057a9986c20a45",
        "ArY1I2xC2yDwIbuNHN/1ynXdGgzHLqdCrXUPMwELJPc7s7JqlCMJBAIIjfkpHReBPXeoMCyuClwgbT419jUWU1PwaNl4FEQYKCDKVJz+97Mp3K+Q2YGa77B6gpxB/lr1QgoqpDf7wDVrDmOqGoiPjWDqy8KzLueKDcm9BVP8xeTJIxs=",
        "表ポあA鷗ŒéＢ逍Üßªąñ丂㐀𠀀"
    );

    // Vector 4: Multi-language
    assertDecrypt(
        "d5a2f879123145a4b291d767428870f5a8d9e5007193321795b40183d4ab8c2b",
        "ArIJia3D3cQc0sQ1lSwNWakTFdjFIY1QQFc/w3SVQ6yvbG2S0x4Yu86QGwPTy7mP3961I1XqB6SFFTzqDZZavhxoWMj7mEVGMQIsh2RLWI5EYQaQDIePSnXPlzf7CIt+voTD",
        "ability🤝的 ȺȾ"
    );

    // Vector 5: Cyrillic with emoji
    assertDecrypt(
        "3b15c977e20bfe4b8482991274635edd94f366595b1a3d2993515705ca3cedb8",
        "Ao1EQnE+udR5EXXLBA2Y1vxb6IZNbsL4nPCJWisrctGxY3AduCS+jTUgAAnfvKafkmpy15+i9YMwCdccisRa8SvzW671T2JO4LFSPX31K4kYUKelSAdSPwe9NwO6LhOsnoJ+",
        "pepper👀їжак"
    );
  }

  /**
   * Helper method to assert decryption of official test vector.
   *
   * @param conversationKeyHex Conversation key as hex string
   * @param payload Base64-encoded encrypted payload
   * @param expectedPlaintext Expected decrypted plaintext
   */
  private void assertDecrypt(String conversationKeyHex, String payload, String expectedPlaintext)
      throws Exception {
    byte[] conversationKey = EncryptedPayloads.hexStringToByteArray(conversationKeyHex);

    String decrypted = EncryptedPayloads.decrypt(payload, conversationKey);
    assertEquals(
        expectedPlaintext,
        decrypted,
        String.format(
            "Decryption mismatch for payload=%s...",
            payload.length() > 20 ? payload.substring(0, 20) + "..." : payload));
  }
}
