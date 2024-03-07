package nostr.encryption.nip44;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.crypto.nip44.EncryptedPayloads;
import nostr.encryption.MessageCipher;
import nostr.util.NostrUtil;

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
    public String decrypt(@NonNull String message) {
        try {
            byte[] convoKey = getConversationKey();
            return EncryptedPayloads.decrypt(message, convoKey);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private byte[] getConversationKey() throws Exception {
        return EncryptedPayloads.getConversationKey(senderPrivateKey, recipientPublicKey);
    }

    private byte[] generateNonce() {
        return NostrUtil.createRandomByteArray(NONCE_LENGTH);
    }
}