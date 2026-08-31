package nostr.crypto.nip04;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.NonNull;
import nostr.util.NostrUtil;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Encrypts direct messages according to NIP-04.
 *
 * @deprecated NIP-04 conceals only a message's text. The sender, the recipient, the exact time,
 *     and the number of messages exchanged all remain public on every relay that carries the
 *     event, so an observer learns who talks to whom and when. Prefer NIP-17 private direct
 *     messages, which hide all of it: see {@code nostr.encryption.Nip17DirectMessageService}
 *     and the guide at {@code docs/howto/private-direct-messages.md}. Retained for reading
 *     existing conversations and for interoperating with clients that send nothing else.
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/04.md">NIP-04</a>
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
@Deprecated(since = "2.1.0")
public class EncryptedDirectMessage {

  public static String encrypt(@NonNull String message, byte[] senderPrivKey, byte[] rcptPubKey)
      throws InvalidAlgorithmParameterException,
          NoSuchPaddingException,
          IllegalBlockSizeException,
          NoSuchAlgorithmException,
          BadPaddingException,
          InvalidKeyException {
    return encryptMessage(senderPrivKey, rcptPubKey, message);
  }

  public static String decryptMessage(
      byte[] senderPrivKey, @NonNull String encContent, byte[] rcptPubKey)
      throws InvalidAlgorithmParameterException,
          NoSuchPaddingException,
          IllegalBlockSizeException,
          NoSuchAlgorithmException,
          BadPaddingException,
          InvalidKeyException {
    SecretKeySpec sharedSecret = getSharedSecretKeySpec(senderPrivKey, rcptPubKey);
    return decryptMessage(sharedSecret, encContent);
  }

  private static String decryptMessage(SecretKeySpec sharedSecretKey, String encodedMessage)
      throws NoSuchAlgorithmException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException,
          IllegalBlockSizeException,
          BadPaddingException {

    String[] parts = encodedMessage.split("\\?iv=");

    Base64.Decoder Base64Decoder = Base64.getDecoder();
    byte[] encryptedMessage = Base64Decoder.decode(parts[0]);
    byte[] iv = Base64Decoder.decode(parts[1]);

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, sharedSecretKey, ivParamSpec);

    return new String(cipher.doFinal(encryptedMessage), StandardCharsets.UTF_8);
  }

  static String encryptMessage(byte[] senderPrivateKey, byte[] rcptPublicKey, String message)
      throws NoSuchAlgorithmException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException,
          IllegalBlockSizeException,
          BadPaddingException {

    SecretKeySpec sharedSecretKey = getSharedSecretKeySpec(senderPrivateKey, rcptPublicKey);

    byte[] iv = NostrUtil.createRandomByteArray(16);
    IvParameterSpec ivParamSpec = new IvParameterSpec(iv);

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey, ivParamSpec);

    byte[] msg = message.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedMessage = cipher.doFinal(msg);

    Base64.Encoder Base64Encoder = Base64.getEncoder();
    byte[] encryptedMessage64 = Base64Encoder.encode(encryptedMessage);

    byte[] iv64 = Base64Encoder.encode(ivParamSpec.getIV());

    return new String(encryptedMessage64) + "?iv=" + new String(iv64);
  }

  private static SecretKeySpec getSharedSecretKeySpec(byte[] privateKey, byte[] publicKey) {
    final String secKeyHex = NostrUtil.bytesToHex(privateKey);
    final String pubKeyHex = NostrUtil.bytesToHex(publicKey);

    byte[] sharedPoint = getSharedSecret(secKeyHex, pubKeyHex);
    byte[] sharedX = Arrays.copyOfRange(sharedPoint, 1, 33);

    return new SecretKeySpec(sharedX, "AES");
  }

  private static byte[] getSharedSecret(String privateKeyHex, String publicKeyHex) {

    SecP256K1Curve curve = new SecP256K1Curve();
    ECPoint pubKeyPt = curve.decodePoint(NostrUtil.nip04PubKeyHexToBytes("02" + publicKeyHex));
    BigInteger tweakVal = new BigInteger(1, NostrUtil.hexToBytes(privateKeyHex));
    return pubKeyPt.multiply(tweakVal).getEncoded(true);
  }
}
