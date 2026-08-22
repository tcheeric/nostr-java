package nostr.crypto.nip44;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * NIP-44 v2 against the specification's own vectors.
 *
 * <p>The fixture is {@code nip44.vectors.json}, taken verbatim from the reference
 * implementation at https://github.com/paulmillr/nip44 — the same file every other
 * NIP-44 library is checked against.
 *
 * <p>The whole class runs with the BouncyCastle JCE provider <em>de-registered</em>, and
 * that is the point of it. NIP-44 used to reach for {@code Cipher.getInstance("ChaCha20")}
 * with an {@code IvParameterSpec}, which only BouncyCastle's provider accepts; SunJCE
 * demands a {@code ChaCha20ParameterSpec} and throws. So encryption worked only in a JVM
 * where something had already called {@code Security.addProvider(new BouncyCastleProvider())}
 * — in practice {@code Schnorr.generatePrivateKey()}, as a side effect. Any test that
 * generated a key first passed while the library was broken for everyone who loaded a key
 * from storage, and broken on Android outright (there, adding a provider named "BC" is a
 * no-op, because the platform already owns that name). Removing the provider here is what
 * keeps that from coming back.
 */
class EncryptedPayloadsTest {

  private static JsonNode vectors;
  private static Provider removedBouncyCastle;

  @BeforeAll
  static void loadVectorsAndRemoveBouncyCastleProvider() throws Exception {
    try (InputStream in =
        EncryptedPayloadsTest.class.getResourceAsStream("/nip44.vectors.json")) {
      vectors = new ObjectMapper().readTree(in).get("v2").get("valid");
    }
    removedBouncyCastle = Security.getProvider("BC");
    Security.removeProvider("BC");
  }

  @AfterAll
  static void restoreBouncyCastleProvider() {
    if (removedBouncyCastle != null) {
      Security.addProvider(removedBouncyCastle);
    }
  }

  /**
   * The vectors carry {@code pub2} as a nostr x-only public key; {@code getConversationKey}
   * wants a compressed SEC1 point, so the "02" prefix goes on here exactly as
   * {@code MessageCipher44} puts it on.
   */
  @ParameterizedTest(name = "conversation key [{index}]")
  @MethodSource("conversationKeyVectors")
  void getConversationKeyMatchesTheSpecVectors(String sec1, String pub2, String expected) {
    assertArrayEquals(
        EncryptedPayloads.hexStringToByteArray(expected),
        EncryptedPayloads.getConversationKey(sec1, "02" + pub2));
  }

  @ParameterizedTest(name = "encrypt [{index}]")
  @MethodSource("encryptDecryptVectors")
  void encryptMatchesTheSpecVectors(
      String conversationKey, String nonce, String plaintext, String payload) throws Exception {
    assertEquals(
        payload,
        EncryptedPayloads.encrypt(
            plaintext,
            EncryptedPayloads.hexStringToByteArray(conversationKey),
            EncryptedPayloads.hexStringToByteArray(nonce)));
  }

  @ParameterizedTest(name = "decrypt [{index}]")
  @MethodSource("encryptDecryptVectors")
  void decryptMatchesTheSpecVectors(
      String conversationKey, String nonce, String plaintext, String payload) throws Exception {
    assertEquals(
        plaintext,
        EncryptedPayloads.decrypt(
            payload, EncryptedPayloads.hexStringToByteArray(conversationKey)));
  }

  /**
   * The end-to-end shape a caller actually uses: derive a conversation key from a keypair
   * and round-trip a message. Distinct from the vector tests above in that it starts from
   * keys rather than from a conversation key — the path that used to need a private key to
   * have been generated in this JVM first.
   */
  @Test
  void roundTripsWithoutTheBouncyCastleProviderRegistered() throws Exception {
    JsonNode vector = vectors.get("encrypt_decrypt").get(0);
    byte[] conversationKey =
        EncryptedPayloads.hexStringToByteArray(vector.get("conversation_key").asText());
    byte[] nonce = EncryptedPayloads.hexStringToByteArray(vector.get("nonce").asText());

    String payload = EncryptedPayloads.encrypt("hello nip-44", conversationKey, nonce);

    assertEquals("hello nip-44", EncryptedPayloads.decrypt(payload, conversationKey));
  }

  static List<Arguments> conversationKeyVectors() {
    List<Arguments> arguments = new ArrayList<>();
    vectors
        .get("get_conversation_key")
        .forEach(
            vector ->
                arguments.add(
                    Arguments.of(
                        vector.get("sec1").asText(),
                        vector.get("pub2").asText(),
                        vector.get("conversation_key").asText())));
    return arguments;
  }

  static List<Arguments> encryptDecryptVectors() {
    List<Arguments> arguments = new ArrayList<>();
    vectors
        .get("encrypt_decrypt")
        .forEach(
            vector ->
                arguments.add(
                    Arguments.of(
                        vector.get("conversation_key").asText(),
                        vector.get("nonce").asText(),
                        vector.get("plaintext").asText(),
                        vector.get("payload").asText())));
    return arguments;
  }
}
