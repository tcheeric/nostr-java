package nostr.crypto.nip04;

import lombok.NonNull;
import nostr.crypto.schnorr.Schnorr;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class EncryptedDirectMessage {

    public static String encrypt(@NonNull String message, @NonNull byte[] senderPrivKey, @NonNull byte[] rcptPubKey) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return encryptMessage(senderPrivKey, rcptPubKey, message);
    }

    public static String decryptMessage(@NonNull byte[] senderPrivKey, @NonNull String encContent, @NonNull byte[] rcptPubKey) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
            var sharedSecret = getSharedSecretKeySpec(senderPrivKey, rcptPubKey);
            return decryptMessage(sharedSecret, encContent);
    }

    private static String decryptMessage(SecretKeySpec sharedSecretKey, String encodedMessage) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        final var parts = encodedMessage.split("\\?iv=");

        final var Base64Decoder = Base64.getDecoder();
        final var encryptedMessage = Base64Decoder.decode(parts[0]);
        final var iv = Base64Decoder.decode(parts[1]);

        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        var ivParamSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, sharedSecretKey, ivParamSpec);

        return new String(cipher.doFinal(encryptedMessage), StandardCharsets.UTF_8);
    }

    static String encryptMessage(byte[] senderPrivateKey, byte[] rcptPublicKey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        var sharedSecretKey = getSharedSecretKeySpec(senderPrivateKey, rcptPublicKey);

        var iv = NostrUtil.createRandomByteArray(16);
        var ivParamSpec = new IvParameterSpec(iv);

        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey, ivParamSpec);

        final var msg = message.getBytes(StandardCharsets.UTF_8);
        var encryptedMessage = cipher.doFinal(msg);

        final var Base64Encoder = Base64.getEncoder();
        var encryptedMessage64 = Base64Encoder.encode(encryptedMessage);

        var iv64 = Base64Encoder.encode(ivParamSpec.getIV());

        return new String(encryptedMessage64) + "?iv=" + new String(iv64);
    }

    private static SecretKeySpec getSharedSecretKeySpec(byte[] privateKey, byte[] publicKey) {
        final String secKeyHex = NostrUtil.bytesToHex(privateKey);
        final String pubKeyHex = NostrUtil.bytesToHex(publicKey);

        var sharedPoint = getSharedSecret(secKeyHex, pubKeyHex);
        var sharedX = Arrays.copyOfRange(sharedPoint, 1, 33);

        return new SecretKeySpec(sharedX, "AES");
    }

    private static byte[] getSharedSecret(String privateKeyHex, String publicKeyHex) {

        SecP256K1Curve curve = new SecP256K1Curve();
        ECPoint pubKeyPt = curve.decodePoint(NostrUtil.hexToBytes("02" + publicKeyHex));
        BigInteger tweakVal = new BigInteger(1, NostrUtil.hexToBytes(privateKeyHex));
        return pubKeyPt.multiply(tweakVal).getEncoded(true);
    }

}
