package nostr.crypto.nip44;

import nostr.crypto.schnorr.Schnorr;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class EncryptedPayloads {

    public static int calcPaddedLen(int unpaddedLen) {
        int nextPower = 1 << ((int) Math.floor(Math.log(unpaddedLen - 1) / Math.log(2)) + 1);
        int chunk;
        if (nextPower <= 256) {
            chunk = 32;
        } else {
            chunk = nextPower / 8;
        }
        if (unpaddedLen <= 32) {
            return 32;
        } else {
            return chunk * ((int) Math.floor((unpaddedLen - 1) / chunk) + 1);
        }
    }

    public static byte[] pad(String plaintext) throws Exception {
        byte[] unpadded = plaintext.getBytes(StandardCharsets.UTF_8);
        int unpaddedLen = unpadded.length;
        if (unpaddedLen < Constants.MIN_PLAINTEXT_SIZE || unpaddedLen > Constants.MAX_PLAINTEXT_SIZE) {
            throw new Exception("Invalid plaintext length");
        }
        byte[] prefix = ByteBuffer.allocate(2).putShort((short) unpaddedLen).array();
        byte[] suffix = new byte[EncryptedPayloads.calcPaddedLen(unpaddedLen) - unpaddedLen];
        return concat(prefix, unpadded, suffix);
    }

    public static String unpad(byte[] padded) throws Exception {
        ByteBuffer wrapped = ByteBuffer.wrap(padded, 0, 2); // big-endian by default
        int unpaddedLen = Short.toUnsignedInt(wrapped.getShort());
        byte[] unpadded = Arrays.copyOfRange(padded, 2, 2 + unpaddedLen);
        if (unpaddedLen == 0 || unpadded.length != unpaddedLen || padded.length != 2 + calcPaddedLen(unpaddedLen)) {
            throw new Exception("Invalid padding");
        }
        return new String(unpadded, StandardCharsets.UTF_8);
    }

    public static byte[][] decodePayload(String payload) throws Exception {
        int plen = payload.length();
        if (plen == 0 || payload.charAt(0) == '#') {
            throw new Exception("Unknown version");
        }
        if (plen < 132 || plen > 87472) {
            throw new Exception("Invalid payload size");
        }
        byte[] data = Base64.getDecoder().decode(payload);
        int dlen = data.length;
        if (dlen < 99 || dlen > 65603) {
            throw new Exception("Invalid data size");
        }
        byte vers = data[0];
        if (vers != Constants.VERSION) {
            throw new Exception("Unknown version " + vers);
        }
        byte[] nonce = Arrays.copyOfRange(data, 1, 33);
        byte[] ciphertext = Arrays.copyOfRange(data, 33, dlen - 32);
        byte[] mac = Arrays.copyOfRange(data, dlen - 32, dlen);
        return new byte[][]{nonce, ciphertext, mac};
    }

    public static byte[] hmacAad(byte[] key, byte[] message, byte[] aad) throws Exception {
        if (aad.length != Constants.NONCE_LENGTH) {
            throw new Exception("AAD associated data must be 32 bytes");
        }
        Mac mac = Mac.getInstance(Constants.HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, Constants.HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        mac.update(aad);
        mac.update(message);
        return mac.doFinal();
    }

    public static byte[] getConversationKey(byte[] privateKeyA, byte[] publicKeyB) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        ECPrivateKeySpec privateKeySpecA = new ECPrivateKeySpec(new BigInteger(privateKeyA), Schnorr.getEcSpec());
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpecA);
        ECPublicKeySpec publicKeySpecB = new ECPublicKeySpec(Schnorr.getCurve().decodePoint(publicKeyB), Schnorr.getEcSpec());
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpecB);

        ECPoint sharedX = ((ECPrivateKey) privateKey).getParameters().getG().multiply(((ECPrivateKey) privateKey).getD()).add(((ECPublicKey) publicKey).getQ());

        SecretKeyFactory skf = SecretKeyFactory.getInstance(Constants.KEY_DERIVATION_ALGORITHM);
        KeySpec spec = new PBEKeySpec(new String(sharedX.getEncoded(false), Constants.CHARACTER_ENCODING).toCharArray(), Constants.SALT_PREFIX.getBytes(StandardCharsets.UTF_8), 65536, 256);
        return skf.generateSecret(spec).getEncoded();
    }

    public static byte[][] getMessageKeys(byte[] conversationKey, byte[] nonce) throws Exception {
        if (conversationKey.length != Constants.CONVERSATION_KEY_LENGTH) {
            throw new Exception("Invalid conversation_key length");
        }
        if (nonce.length != Constants.NONCE_LENGTH) {
            throw new Exception("Invalid nonce length");
        }

        SecretKeyFactory skf = SecretKeyFactory.getInstance(Constants.KEY_DERIVATION_ALGORITHM);
        KeySpec spec = new PBEKeySpec(new String(conversationKey, Constants.CHARACTER_ENCODING).toCharArray(), nonce, 65536, (Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH + Constants.HMAC_KEY_LENGTH) * 8);
        byte[] keys = skf.generateSecret(spec).getEncoded();

        byte[] chachaKey = Arrays.copyOfRange(keys, 0, Constants.CHACHA_KEY_LENGTH);
        byte[] chachaNonce = Arrays.copyOfRange(keys, Constants.CHACHA_KEY_LENGTH, Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH);
        byte[] hmacKey = Arrays.copyOfRange(keys, Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH, keys.length);

        return new byte[][]{chachaKey, chachaNonce, hmacKey};
    }

    public static String encrypt(String plaintext, byte[] conversationKey, byte[] nonce) throws Exception {
        byte[][] keys = EncryptedPayloads.getMessageKeys(conversationKey, nonce);
        byte[] chachaKey = keys[0];
        byte[] chachaNonce = keys[1];
        byte[] hmacKey = keys[2];

        byte[] padded = EncryptedPayloads.pad(plaintext);

        Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(chachaKey, Constants.ENCRYPTION_ALGORITHM), new IvParameterSpec(chachaNonce));
        byte[] ciphertext = cipher.doFinal(padded);

        Mac mac = Mac.getInstance(Constants.HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(hmacKey, Constants.HMAC_ALGORITHM));
        mac.update(nonce);
        mac.update(ciphertext);
        byte[] hmac = mac.doFinal();

        ByteBuffer buffer = ByteBuffer.allocate(1 + nonce.length + ciphertext.length + hmac.length);
        buffer.put((byte) Constants.VERSION);
        buffer.put(nonce);
        buffer.put(ciphertext);
        buffer.put(hmac);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    public static String decrypt(String payload, byte[] conversationKey) throws Exception {
        byte[][] decodedPayload = EncryptedPayloads.decodePayload(payload);
        byte[] nonce = decodedPayload[0];
        byte[] ciphertext = decodedPayload[1];
        byte[] mac = decodedPayload[2];

        byte[][] keys = EncryptedPayloads.getMessageKeys(conversationKey, nonce);
        byte[] chachaKey = keys[0];
        byte[] chachaNonce = keys[1];
        byte[] hmacKey = keys[2];

        Mac hmac = Mac.getInstance(Constants.HMAC_ALGORITHM);
        hmac.init(new SecretKeySpec(hmacKey, Constants.HMAC_ALGORITHM));
        hmac.update(nonce);
        hmac.update(ciphertext);
        byte[] calculatedMac = hmac.doFinal();

        if (!MessageDigest.isEqual(calculatedMac, mac)) {
            throw new Exception("Invalid MAC");
        }

        Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(chachaKey, Constants.ENCRYPTION_ALGORITHM), new IvParameterSpec(chachaNonce));
        byte[] paddedPlaintext = cipher.doFinal(ciphertext);

        return EncryptedPayloads.unpad(paddedPlaintext);
    }

    private static byte[] concat(byte[]... arrays) {
        int totalLength = Arrays.stream(arrays).mapToInt(a -> a.length).sum();
        byte[] result = new byte[totalLength];
        int currentPosition = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentPosition, array.length);
            currentPosition += array.length;
        }
        return result;
    }

    private static class Constants {
        public static final int MIN_PLAINTEXT_SIZE = 1;
        public static final int MAX_PLAINTEXT_SIZE = 65535;
        public static final String SALT_PREFIX = "nip44-v2";
        private static final String ENCRYPTION_ALGORITHM = "ChaCha20";
        private static final String HMAC_ALGORITHM = "HmacSHA256";
        private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
        private static final String CHARACTER_ENCODING = StandardCharsets.UTF_8.name();
        private static final int CONVERSATION_KEY_LENGTH = 32;
        private static final int NONCE_LENGTH = 32;
        private static final int HMAC_KEY_LENGTH = 32;
        private static final int CHACHA_KEY_LENGTH = 32;
        private static final int CHACHA_NONCE_LENGTH = 12;
        private static final int VERSION = 2;

    }
}