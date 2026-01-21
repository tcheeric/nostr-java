package nostr.crypto.nip44;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Cross-implementation test vectors for NIP-44 conversation key derivation.
 * 
 * These test vectors are from the official NIP-44 specification test vectors
 * (https://github.com/paulmillr/nip44/blob/main/nip44.vectors.json) and verify
 * that this Java implementation produces the same conversation keys as the
 * JavaScript implementations (nostr-tools) and other language implementations.
 * 
 * This ensures DM interoperability across different Nostr client implementations.
 */
public class Nip44ConversationKeyTest {

  /**
   * Test conversation key derivation with official test vectors from NIP-44 spec.
   * Verifies that ECDH key agreement and HKDF-Extract produce compatible results.
   */
  @Test
  public void testConversationKeyWithOfficialVectors() {
    // Vector 1
    assertConversationKey(
        "315e59ff51cb9209768cf7da80791ddcaae56ac9775eb25b6dee1234bc5d2268",
        "02c2f9d9948dc8c7c38321e4b85c8558872eafa0641cd269db76848a6073e69133",
        "3dfef0ce2a4d80a25e7a328accf73448ef67096f65f79588e358d9a0eb9013f1"
    );

    // Vector 2
    assertConversationKey(
        "a1e37752c9fdc1273be53f68c5f74be7c8905728e8de75800b94262f9497c86e",
        "0303bb7947065dde12ba991ea045132581d0954f042c84e06d8c00066e23c1a800",
        "4d14f36e81b8452128da64fe6f1eae873baae2f444b02c950b90e43553f2178b"
    );

    // Vector 3
    assertConversationKey(
        "98a5902fd67518a0c900f0fb62158f278f94a21d6f9d33d30cd3091195500311",
        "02aae65c15f98e5e677b5050de82e3aba47a6fe49b3dab7863cf35d9478ba9f7d1",
        "9c00b769d5f54d02bf175b7284a1cbd28b6911b06cda6666b2243561ac96bad7"
    );

    // Vector 4
    assertConversationKey(
        "86ae5ac8034eb2542ce23ec2f84375655dab7f836836bbd3c54cefe9fdc9c19f",
        "0259f90272378089d73f1339710c02e2be6db584e9cdbe86eed3578f0c67c23585",
        "19f934aafd3324e8415299b64df42049afaa051c71c98d0aa10e1081f2e3e2ba"
    );

    // Vector 5
    assertConversationKey(
        "2528c287fe822421bc0dc4c3615878eb98e8a8c31657616d08b29c00ce209e34",
        "02f66ea16104c01a1c532e03f166c5370a22a5505753005a566366097150c6df60",
        "c833bbb292956c43366145326d53b955ffb5da4e4998a2d853611841903f5442"
    );

    // Vector 6
    assertConversationKey(
        "49808637b2d21129478041813aceb6f2c9d4929cd1303cdaf4fbdbd690905ff2",
        "0274d2aab13e97827ea21baf253ad7e39b974bb2498cc747cdb168582a11847b65",
        "4bf304d3c8c4608864c0fe03890b90279328cd24a018ffa9eb8f8ccec06b505d"
    );

    // Vector 7
    assertConversationKey(
        "af67c382106242c5baabf856efdc0629cc1c5b4061f85b8ceaba52aa7e4b4082",
        "02bdaf0001d63e7ec994fad736eab178ee3c2d7cfc925ae29f37d19224486db57b",
        "a3a575dd66d45e9379904047ebfb9a7873c471687d0535db00ef2daa24b391db"
    );

    // Vector 8
    assertConversationKey(
        "0e44e2d1db3c1717b05ffa0f08d102a09c554a1cbbf678ab158b259a44e682f1",
        "021ffa76c5cc7a836af6914b840483726207cb750889753d7499fb8b76aa8fe0de",
        "a39970a667b7f861f100e3827f4adbf6f464e2697686fe1a81aeda817d6b8bdf"
    );

    // Vector 9
    assertConversationKey(
        "5fc0070dbd0666dbddc21d788db04050b86ed8b456b080794c2a0c8e33287bb6",
        "0231990752f296dd22e146c9e6f152a269d84b241cc95bb3ff8ec341628a54caf0",
        "72c21075f4b2349ce01a3e604e02a9ab9f07e35dd07eff746de348b4f3c6365e"
    );

    // Vector 10
    assertConversationKey(
        "1b7de0d64d9b12ddbb52ef217a3a7c47c4362ce7ea837d760dad58ab313cba64",
        "0224383541dd8083b93d144b431679d70ef4eec10c98fceef1eff08b1d81d4b065",
        "dd152a76b44e63d1afd4dfff0785fa07b3e494a9e8401aba31ff925caeb8f5b1"
    );

    // Vector 11
    assertConversationKey(
        "df2f560e213ca5fb33b9ecde771c7c0cbd30f1cf43c2c24de54480069d9ab0af",
        "03eeea26e552fc8b5e377acaa03e47daa2d7b0c787fac1e0774c9504d9094c430e",
        "770519e803b80f411c34aef59c3ca018608842ebf53909c48d35250bd9323af6"
    );

    // Vector 12
    assertConversationKey(
        "cffff919fcc07b8003fdc63bc8a00c0f5dc81022c1c927c62c597352190d95b9",
        "03eb5c3cca1a968e26684e5b0eb733aecfc844f95a09ac4e126a9e58a4e4902f92",
        "46a14ee7e80e439ec75c66f04ad824b53a632b8409a29bbb7c192e43c00bb795"
    );

    // Vector 13
    assertConversationKey(
        "64ba5a685e443e881e9094647ddd32db14444bb21aa7986beeba3d1c4673ba0a",
        "0250e6a4339fac1f3bf86f2401dd797af43ad45bbf58e0801a7877a3984c77c3c4",
        "968b9dbbfcede1664a4ca35a5d3379c064736e87aafbf0b5d114dff710b8a946"
    );

    // Vector 14
    assertConversationKey(
        "dd0c31ccce4ec8083f9b75dbf23cc2878e6d1b6baa17713841a2428f69dee91a",
        "02b483e84c1339812bed25be55cff959778dfc6edde97ccd9e3649f442472c091b",
        "09024503c7bde07eb7865505891c1ea672bf2d9e25e18dd7a7cea6c69bf44b5d"
    );

    // Vector 15
    assertConversationKey(
        "af71313b0d95c41e968a172b33ba5ebd19d06cdf8a7a98df80ecf7af4f6f0358",
        "022a5c25266695b461ee2af927a6c44a3c598b8095b0557e9bd7f787067435bc7c",
        "fe5155b27c1c4b4e92a933edae23726a04802a7cc354a77ac273c85aa3c97a92"
    );
  }

  /**
   * Test edge case conversation key derivation with boundary values.
   * These vectors test edge cases like sec1 = n-2, sec1 = 2, and sec1 == pub2.
   */
  @Test
  public void testConversationKeyEdgeCases() {
    // Edge case: sec1 = n-2, pub2: random, 0x02
    assertConversationKey(
        "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364139",
        "020000000000000000000000000000000000000000000000000000000000000002",
        "8b6392dbf2ec6a2b2d5b1477fc2be84d63ef254b667cadd31bd3f444c44ae6ba"
    );

    // Edge case: sec1 = 2, pub2: random
    assertConversationKey(
        "0000000000000000000000000000000000000000000000000000000000000002",
        "021234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdeb",
        "be234f46f60a250bef52a5ee34c758800c4ca8e5030bf4cc1a31d37ba2104d43"
    );

    // Edge case: sec1 == pub2 (G point)
    assertConversationKey(
        "0000000000000000000000000000000000000000000000000000000000000001",
        "0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798",
        "3b4610cb7189beb9cc29eb3716ecc6102f1247e8f3101a03a1787d8908aeb54e"
    );
  }

  /**
   * Helper method to assert conversation key derivation matches expected value.
   *
   * @param sec1 Private key as hex string
   * @param pub2 Public key as hex string (compressed format with 02/03 prefix)
   * @param expectedConversationKey Expected conversation key as hex string
   */
  private void assertConversationKey(String sec1, String pub2, String expectedConversationKey) {
    byte[] conversationKey = EncryptedPayloads.getConversationKey(sec1, pub2);
    byte[] expected = EncryptedPayloads.hexStringToByteArray(expectedConversationKey);
    
    assertArrayEquals(
        expected,
        conversationKey,
        String.format(
            "Conversation key mismatch for sec1=%s, pub2=%s. Expected=%s, Got=%s",
            sec1.substring(0, 16) + "...",
            pub2.substring(0, 16) + "...",
            expectedConversationKey.substring(0, 16) + "...",
            bytesToHex(conversationKey).substring(0, 16) + "..."
        )
    );
  }

  /**
   * Convert byte array to hex string for debugging.
   */
  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
