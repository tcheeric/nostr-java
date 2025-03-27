package nostr.encryption;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.crypto.nip04.EncryptedDirectMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Data
@AllArgsConstructor
public class MessageCipher04 implements MessageCipher {

    private final byte[] senderPrivateKey;
    private final byte[] recipientPublicKey;

    @Override
    public String encrypt(@NonNull String message) {
        try {
            return EncryptedDirectMessage.encrypt(message, senderPrivateKey, recipientPublicKey);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
                 NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(@NonNull String message) {
        try {
            return EncryptedDirectMessage.decryptMessage(senderPrivateKey, message, recipientPublicKey);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
                 NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
