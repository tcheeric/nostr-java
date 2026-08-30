package nostr.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.crypto.nip04.EncryptedDirectMessage;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Encrypts and decrypts messages using NIP-04.
 *
 * @deprecated NIP-04 hides only the message text; the correspondents and the timing stay
 *     public. Prefer {@link Nip17DirectMessageService}, which conceals the metadata too.
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
@Deprecated(since = "2.1.0")
@Data
@AllArgsConstructor
public class MessageCipher04 implements MessageCipher {

  private final byte[] senderPrivateKey;
  private final byte[] recipientPublicKey;

  @Override
  public String encrypt(@NonNull String message) {
    try {
      return EncryptedDirectMessage.encrypt(message, senderPrivateKey, recipientPublicKey);
    } catch (InvalidAlgorithmParameterException
        | InvalidKeyException
        | BadPaddingException
        | NoSuchAlgorithmException
        | IllegalBlockSizeException
        | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String decrypt(@NonNull String message) {
    try {
      return EncryptedDirectMessage.decryptMessage(senderPrivateKey, message, recipientPublicKey);
    } catch (InvalidAlgorithmParameterException
        | InvalidKeyException
        | BadPaddingException
        | NoSuchAlgorithmException
        | IllegalBlockSizeException
        | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }
}
