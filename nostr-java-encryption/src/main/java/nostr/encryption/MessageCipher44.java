package nostr.encryption;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.crypto.nip44.EncryptedPayloads;
import nostr.util.NostrUtil;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Data
@AllArgsConstructor
public class MessageCipher44 implements MessageCipher {

  private static final int NONCE_LENGTH = 32;

  private final byte[] senderPrivateKey;
  private final byte[] recipientPublicKey;

  @Override
  public String encrypt(@NonNull String message) {
    try {
      byte[] convoKey = getConversationKey();
      byte[] nonce = generateNonce();
      return EncryptedPayloads.encrypt(message, convoKey, nonce);
    } catch (Exception e) {
      throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
    }
  }

  @Override
  public String decrypt(@NonNull String payload) {
    try {
      byte[] convoKey = getConversationKey();
      return EncryptedPayloads.decrypt(payload, convoKey);
    } catch (Exception e) {
      throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
    }
  }

  private byte[] getConversationKey() {
    try {
      return EncryptedPayloads.getConversationKey(
          NostrUtil.bytesToHex(senderPrivateKey), "02" + NostrUtil.bytesToHex(recipientPublicKey));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  private byte[] generateNonce() {
    return NostrUtil.createRandomByteArray(NONCE_LENGTH);
  }
}
