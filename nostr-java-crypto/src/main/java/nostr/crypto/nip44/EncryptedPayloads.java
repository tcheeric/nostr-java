package nostr.crypto.nip44;

import lombok.NonNull;
import lombok.extern.java.Log;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;

@Log
public class EncryptedPayloads {

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
            log.log(Level.FINE, "Calculated MAC = {0} --- Mac = {1}", new Object[]{Arrays.toString(calculatedMac), Arrays.toString(mac)});
            throw new Exception("Invalid MAC");
        }

        Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(chachaKey, Constants.ENCRYPTION_ALGORITHM), new IvParameterSpec(chachaNonce));
        byte[] paddedPlaintext = cipher.doFinal(ciphertext);

        return EncryptedPayloads.unpad(paddedPlaintext);
    }

    public static byte[] getConversationKey(String privkeyA, String pubkeyB) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Get the ECNamedCurveParameterSpec for the secp256k1 curve
        ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");

        // Create an ECDomainParameters from the ECNamedCurveParameterSpec
        ECDomainParameters domainParameters = new ECDomainParameters(ecSpec.getCurve(), ecSpec.getG(), ecSpec.getN(), ecSpec.getH(), ecSpec.getSeed());

        // Convert the private key string to a BigInteger
        BigInteger d = new BigInteger(privkeyA, 16);

        // Create a private key parameter
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(d, domainParameters);

        // Convert the public key string to an ECPoint
        ECPoint Q = domainParameters.getCurve().decodePoint(hexStringToByteArray(pubkeyB));

        // Create a public key parameter
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(Q, domainParameters);

        // Perform the point multiplication
        ECPoint pointQ = new FixedPointCombMultiplier().multiply(publicKeyParameters.getQ(), privateKeyParameters.getD());

        // The result of the point multiplication is the shared secret
        byte[] sharedX = pointQ.normalize().getAffineXCoord().getEncoded();

        // Derive the key using HKDF
        char[] sharedXChars = new String(sharedX, StandardCharsets.UTF_8).toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(sharedXChars, "nip44-v2".getBytes(), 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = factory.generateSecret(keySpec).getEncoded();

        return key;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static int calcPaddedLen(int unpaddedLen) {
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
            return chunk * ((int) Math.floor((double) (unpaddedLen - 1) / chunk) + 1);
        }
    }

    private static byte[] pad(String plaintext) throws Exception {
        byte[] unpadded = plaintext.getBytes(StandardCharsets.UTF_8);
        int unpaddedLen = unpadded.length;
        if (unpaddedLen < Constants.MIN_PLAINTEXT_SIZE || unpaddedLen > Constants.MAX_PLAINTEXT_SIZE) {
            throw new Exception("Invalid plaintext length");
        }
        byte[] prefix = ByteBuffer.allocate(2).putShort((short) unpaddedLen).array();
        byte[] suffix = new byte[EncryptedPayloads.calcPaddedLen(unpaddedLen) - unpaddedLen];
        return concat(prefix, unpadded, suffix);
    }

    private static String unpad(byte[] padded) throws Exception {
        ByteBuffer wrapped = ByteBuffer.wrap(padded, 0, 2); // big-endian by default
        int unpaddedLen = Short.toUnsignedInt(wrapped.getShort());
        byte[] unpadded = Arrays.copyOfRange(padded, 2, 2 + unpaddedLen);
        if (unpaddedLen == 0 || unpadded.length != unpaddedLen || padded.length != 2 + calcPaddedLen(unpaddedLen)) {
            throw new Exception("Invalid padding");
        }
        return new String(unpadded, StandardCharsets.UTF_8);
    }

    private static byte[][] decodePayload(@NonNull String payload) throws Exception {
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

    private static byte[] hmacAad(byte[] key, byte[] message, byte[] aad) throws Exception {
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

    public static byte[] hkdf_extract(byte[] IKM, byte[] salt) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(IKM, salt, null));

        byte[] okm = new byte[32];  // Output key material
        hkdf.generateBytes(okm, 0, okm.length);

        return okm;
    }

    private static byte[][] getMessageKeys(byte[] conversationKey, byte[] nonce) throws Exception {
        if (conversationKey.length != Constants.CONVERSATION_KEY_LENGTH) {
            throw new Exception("Invalid conversation_key length");
        }
        if (nonce.length != Constants.NONCE_LENGTH) {
            throw new Exception("Invalid nonce length");
        }

        SecretKeyFactory skf = SecretKeyFactory.getInstance(Constants.KEY_DERIVATION_ALGORITHM);
        KeySpec spec = new PBEKeySpec(new String(conversationKey, StandardCharsets.UTF_8).toCharArray(), nonce, 65536, (Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH + Constants.HMAC_KEY_LENGTH) * 8);
        byte[] keys = skf.generateSecret(spec).getEncoded();

        byte[] chachaKey = Arrays.copyOfRange(keys, 0, Constants.CHACHA_KEY_LENGTH);
        byte[] chachaNonce = Arrays.copyOfRange(keys, Constants.CHACHA_KEY_LENGTH, Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH);
        byte[] hmacKey = Arrays.copyOfRange(keys, Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH, keys.length);

        return new byte[][]{chachaKey, chachaNonce, hmacKey};
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
        //private static final String CHARACTER_ENCODING = StandardCharsets.UTF_8.name();
        private static final int CONVERSATION_KEY_LENGTH = 32;
        private static final int NONCE_LENGTH = 32;
        private static final int HMAC_KEY_LENGTH = 32;
        private static final int CHACHA_KEY_LENGTH = 32;
        private static final int CHACHA_NONCE_LENGTH = 12;
        private static final int VERSION = 2;

    }
}