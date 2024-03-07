package nostr.id;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.*;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.DelegationTag;
import nostr.event.tag.PubKeyTag;
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
import java.util.logging.Level;

@AllArgsConstructor
@Log
@Deprecated(forRemoval = true)
public class IdentityHelper {

    @NonNull
    private final IIdentity identity;

    public PublicKey getPublicKey() {
        try {
            return new PublicKey(Schnorr.genPubKey(identity.getPrivateKey().getRawData()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

/*
    public void encryptDirectMessage(@NonNull DirectMessageEvent dmEvent) throws NostrException {

        ITag pkTag = dmEvent.getTags().get(0);
        if (pkTag instanceof PubKeyTag pubKeyTag) {
            try {
                var rcptPublicKey = pubKeyTag.getPublicKey();
                var encryptedContent = IdentityHelper.encryptMessage(this.identity.getPrivateKey().getRawData(), rcptPublicKey.getRawData(), dmEvent.getContent());
                dmEvent.setContent(encryptedContent);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                     InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
                throw new NostrException(ex);
            }
        }
    }
*/

/*
    public String encrypt(@NonNull String message, @NonNull PublicKey recipient) throws InvalidAlgorithmParameterException, NostrException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return encryptMessage(this.identity.getPrivateKey().getRawData(), recipient.getRawData(), message);
    }

    public String decryptMessage(@NonNull String encContent, @NonNull PublicKey publicKey) throws NostrException {
        try {
            var sharedSecret = getSharedSecretKeySpec(this.identity.getPrivateKey().getRawData(), publicKey.getRawData());
            return decryptMessage(sharedSecret, encContent);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new NostrException(ex);
        }
    }
*/

    public Signature sign(@NonNull ISignable signable) throws NostrException {
        if (signable instanceof GenericEvent genericEvent) {
            try {
                return signEvent(genericEvent);
            } catch (Exception ex) {
                throw new NostrException(ex);
            }
        } else if (signable instanceof DelegationTag delegationTag) {
            try {
                return signDelegationTag(delegationTag);
            } catch (Exception ex) {
                throw new NostrException(ex);
            }
        }
        throw new NostrException();
    }

    private Signature signEvent(@NonNull GenericEvent event) throws Exception {
        event.update();
        log.log(Level.FINER, "Serialized event: {0}", new String(event.get_serializedEvent()));
        final var signedHashedSerializedEvent = Schnorr.sign(NostrUtil.sha256(event.get_serializedEvent()), this.identity.getPrivateKey().getRawData(), generateAuxRand());
        final Signature signature = new Signature();
        signature.setRawData(signedHashedSerializedEvent);
        signature.setPubKey(getPublicKey());
        event.setSignature(signature);
        return signature;
    }

    private Signature signDelegationTag(@NonNull DelegationTag delegationTag) throws Exception {
        final var signedHashedToken = Schnorr.sign(NostrUtil.sha256(delegationTag.getToken().getBytes(StandardCharsets.UTF_8)), this.identity.getPrivateKey().getRawData(), generateAuxRand());
        final Signature signature = new Signature();
        signature.setRawData(signedHashedToken);
        signature.setPubKey(getPublicKey());
        delegationTag.setSignature(signature);
        return signature;
    }

    private byte[] generateAuxRand() {
        return NostrUtil.createRandomByteArray(32);
    }

/*
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

    static PublicKey generatePublicKey(PrivateKey privateKey) throws Exception {
        var rawDate = Schnorr.genPubKey(privateKey.getRawData());
        return new PublicKey(rawDate);
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
*/
}
