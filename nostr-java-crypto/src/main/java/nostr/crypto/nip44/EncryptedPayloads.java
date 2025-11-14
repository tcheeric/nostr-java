package nostr.crypto.nip44;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class EncryptedPayloads {

  public static String encrypt(String plaintext, byte[] conversationKey, byte[] nonce)
      throws Exception {
    byte[][] keys = EncryptedPayloads.getMessageKeys(conversationKey, nonce);
    byte[] chachaKey = keys[0];
    byte[] chachaNonce = keys[1];
    byte[] hmacKey = keys[2];

    byte[] padded = EncryptedPayloads.pad(plaintext);

    Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);
    cipher.init(
        Cipher.ENCRYPT_MODE,
        new SecretKeySpec(chachaKey, Constants.ENCRYPTION_ALGORITHM),
        new IvParameterSpec(chachaNonce));
    byte[] ciphertext = cipher.doFinal(padded);

    byte[] hmac = hmacAad(hmacKey, ciphertext, nonce);

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

    byte[] calculatedMac = hmacAad(hmacKey, ciphertext, nonce);

    if (!MessageDigest.isEqual(calculatedMac, mac)) {
      log.debug(
          "Calculated MAC {} does not match expected {}",
          Arrays.toString(calculatedMac),
          Arrays.toString(mac));
      throw new Exception("Invalid MAC");
    }

    Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);
    cipher.init(
        Cipher.DECRYPT_MODE,
        new SecretKeySpec(chachaKey, Constants.ENCRYPTION_ALGORITHM),
        new IvParameterSpec(chachaNonce));
    byte[] paddedPlaintext = cipher.doFinal(ciphertext);

    return EncryptedPayloads.unpad(paddedPlaintext);
  }

  public static byte[] getConversationKey(String privkeyA, String pubkeyB)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
    // Get the ECNamedCurveParameterSpec for the secp256k1 curve
    ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");

    // Create an ECDomainParameters from the ECNamedCurveParameterSpec
    ECDomainParameters domainParameters = new ECDomainParameters(
        ecSpec.getCurve(), ecSpec.getG(), ecSpec.getN(), ecSpec.getH(), ecSpec.getSeed());

    // Convert the private key string to a BigInteger
    BigInteger d = new BigInteger(privkeyA, 16);

    // Create a private key parameter
    ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(d, domainParameters);

    // Convert the public key string to an ECPoint
    ECPoint Q = domainParameters.getCurve().decodePoint(hexStringToByteArray(pubkeyB));

    // Create a public key parameter
    ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(Q, domainParameters);

    // Perform the point multiplication
    ECPoint pointQ = new FixedPointCombMultiplier()
        .multiply(publicKeyParameters.getQ(), privateKeyParameters.getD());

    // The result of the point multiplication is the shared secret
    byte[] sharedX = pointQ.normalize().getAffineXCoord().getEncoded();

    // Derive the conversation key using HKDF-extract
    return hkdf_extract(sharedX, Constants.SALT_PREFIX.getBytes(StandardCharsets.UTF_8));
  }

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  private static int calcPaddedLen(int unpaddedLen) {
    if (unpaddedLen <= 0)
      throw new IllegalArgumentException("expected positive integer");

    if (unpaddedLen <= 32)
      return 32;

    int nextPower = 1 << ((int) Math.floor(Math.log(unpaddedLen - 1) / Math.log(2)) + 1);
    int chunk = nextPower <= 256 ? 32 : nextPower / 8;
    return chunk * ((int) Math.floor((unpaddedLen - 1) / (double) chunk) + 1);
  }

  private static byte[] pad(String plaintext) throws Exception {
    byte[] unpadded = plaintext.getBytes(StandardCharsets.UTF_8);
    int unpaddedLen = unpadded.length;

    if (unpaddedLen < Constants.MIN_PLAINTEXT_SIZE || unpaddedLen > Constants.MAX_PLAINTEXT_SIZE)
      throw new Exception("Invalid plaintext length");

    ByteBuffer prefix = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) unpaddedLen);
    byte[] suffix = new byte[calcPaddedLen(unpaddedLen) - unpaddedLen];
    byte[] output = new byte[2 + unpaddedLen + suffix.length];
    System.arraycopy(prefix.array(), 0, output, 0, 2);
    System.arraycopy(unpadded, 0, output, 2, unpaddedLen);
    // suffix already zero-filled
    System.arraycopy(suffix, 0, output, 2 + unpaddedLen, suffix.length);

    return output;
  }

  public static int bytesToInt(byte byte1, byte byte2, boolean bigEndian) {
    if (bigEndian) {
      return ((byte1 & 0xFF) << 8) | (byte2 & 0xFF);
    } else {
      return ((byte2 & 0xFF) << 8) | (byte1 & 0xFF);
    }
  }

  public static String unpad(byte[] padded) {
    int unpaddedLen = bytesToInt(padded[0], padded[1], true);
    if (unpaddedLen < Constants.MIN_PLAINTEXT_SIZE || unpaddedLen > Constants.MAX_PLAINTEXT_SIZE) {
      throw new IllegalArgumentException("Invalid padding length: " + unpaddedLen);
    }

    if (padded.length != 2 + calcPaddedLen(unpaddedLen)) {
      throw new IllegalArgumentException("Padded size mismatch");
    }

    byte[] unpadded = Arrays.copyOfRange(padded, 2, 2 + unpaddedLen);

    return new String(unpadded, StandardCharsets.UTF_8);
  }

  public static byte[] hmacAad(byte[] key, byte[] message, byte[] aad) throws Exception {
    if (aad.length != Constants.HMAC_KEY_LENGTH) {
      throw new IllegalArgumentException("AAD associated data must be " + Constants.HMAC_KEY_LENGTH + "bytes, but it was " + aad.length + " bytes");
    }

    byte[] aadMessageConcat = new byte[aad.length + message.length];
    System.arraycopy(aad, 0, aadMessageConcat, 0, aad.length);
    System.arraycopy(message, 0, aadMessageConcat, aad.length, message.length);

    return hkdf_extract(aadMessageConcat, key);
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
    return new byte[][] {nonce, ciphertext, mac};
  }

  private static byte[] hkdf_extract(byte[] IKM, byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac mac = Mac.getInstance(Constants.HMAC_ALGORITHM);
    mac.init(new SecretKeySpec(salt, Constants.HMAC_ALGORITHM));
    return mac.doFinal(IKM);
  }

  private static byte[] hkdf_expand(byte[] key, byte[] nonce, int outputLength)
      throws InvalidKeyException, NoSuchAlgorithmException {
    int hashLen = 32;
    if (key.length != hashLen) {
      throw new IllegalArgumentException("Key must be of length " + hashLen);
    }
    if (nonce.length != hashLen) {
      throw new IllegalArgumentException("Nonce must be of length " + hashLen);
    }

    int n = (outputLength % hashLen == 0) ? (outputLength / hashLen) : (outputLength / hashLen + 1);
    byte[] hashRound = new byte[0];
    ByteBuffer generatedBytes = ByteBuffer.allocate(Math.multiplyExact(n, hashLen));

    Mac mac = Mac.getInstance(Constants.HMAC_ALGORITHM);
    mac.init(new SecretKeySpec(key, Constants.HMAC_ALGORITHM));

    for (int roundNum = 1; roundNum <= n; roundNum++) {
      mac.reset();
      ByteBuffer t = ByteBuffer.allocate(hashRound.length + nonce.length + 1);
      t.put(hashRound);
      t.put(nonce);
      t.put((byte) roundNum);
      hashRound = mac.doFinal(t.array());
      generatedBytes.put(hashRound);
    }

    byte[] result = new byte[outputLength];
    generatedBytes.rewind();
    generatedBytes.get(result, 0, outputLength);
    return result;
  }

  private static byte[][] getMessageKeys(byte[] conversationKey, byte[] nonce) throws Exception {
    if (conversationKey.length != Constants.CONVERSATION_KEY_LENGTH) {
      throw new Exception("Invalid conversation_key length");
    }
    if (nonce.length != Constants.NONCE_LENGTH) {
      throw new Exception("Invalid nonce length");
    }

    byte[] expanded = hkdf_expand(conversationKey, nonce, Constants.HKDF_OUTPUT_LENGTH);
    byte[] chachaKey = Arrays.copyOfRange(expanded, 0, Constants.CHACHA_KEY_LENGTH);
    byte[] chachaNonce = Arrays.copyOfRange(
      expanded, Constants.CHACHA_KEY_LENGTH, Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH);
    byte[] hmacKey = Arrays.copyOfRange(
      expanded, Constants.CHACHA_KEY_LENGTH + Constants.CHACHA_NONCE_LENGTH, Constants.HKDF_OUTPUT_LENGTH);

    return new byte[][] { chachaKey, chachaNonce, hmacKey };
  }

  private static class Constants {
    public static final int MIN_PLAINTEXT_SIZE = 1;
    public static final int MAX_PLAINTEXT_SIZE = 65535;
    public static final String SALT_PREFIX = "nip44-v2";
    private static final String ENCRYPTION_ALGORITHM = "ChaCha20";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int CONVERSATION_KEY_LENGTH = 32;
    private static final int NONCE_LENGTH = 32;
    private static final int HMAC_KEY_LENGTH = 32;
    private static final int CHACHA_KEY_LENGTH = 32;
    private static final int CHACHA_NONCE_LENGTH = 12;
    private static final int HKDF_OUTPUT_LENGTH = 76;
    private static final int VERSION = 2;
  }
}
