package nostr.id;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.BaseConfiguration;
import nostr.base.Bech32Prefix;
import nostr.base.ISignable;
import nostr.base.ITag;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.DelegationTag;
import nostr.event.tag.PubKeyTag;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode
@ToString
public class Identity {

    @ToString.Exclude
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Identity(String profileFile) throws IOException, NostrException {
        this.privateKey = new IdentityConfiguration(profileFile).getPrivateKey();
        this.publicKey = new IdentityConfiguration(profileFile).getPublicKey();
    }

    public Identity(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public Identity(PrivateKey privateKey) throws Exception {
        this.privateKey = privateKey;
        this.publicKey = generatePublicKey(privateKey);
    }

    public void encryptDirectMessage(@NonNull DirectMessageEvent dmEvent) throws NostrException {
        ITag pkTag = (ITag) dmEvent.getTags().getList().get(0);
        if (pkTag instanceof PubKeyTag pubKeyTag) {
            try {
                var rcptPublicKey = pubKeyTag.getPublicKey();
                var encryptedContent = encryptMessage(privateKey.getRawData(), rcptPublicKey.getRawData(), dmEvent.getContent());
                dmEvent.setContent(encryptedContent);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new NostrException(ex);
            }
        }
    }

    public String decryptDirectMessage(@NonNull String encContent, PublicKey senderPublicKey) throws NostrException {
        try {
            var sharedSecret = getSharedSecretKeySpec(this.privateKey.getRawData(), senderPublicKey.getRawData());
            return Identity.decryptMessage(sharedSecret, encContent);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }
    }

    public Signature sign(@NonNull ISignable signable) throws NostrException {
        if (signable instanceof GenericEvent genericEvent) {
            try {
                return signEvent(genericEvent);
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new NostrException(ex);
            } catch (Exception ex) {
                log.log(Level.SEVERE, null, ex);
                throw new NostrException(ex);
            }
        } else if (signable instanceof DelegationTag delegationTag) {
            try {
                return signDelegationTag(delegationTag);
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new NostrException(ex);
            } catch (Exception ex) {
                log.log(Level.SEVERE, null, ex);
                throw new NostrException(ex);
            }
        }
        throw new NostrException();
    }

    private Signature signEvent(@NonNull GenericEvent event) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, Exception {
        event.update();
        log.log(Level.FINER, "Serialized event: {0}", new String(event.get_serializedEvent()));
        final var signedHashedSerializedEvent = Schnorr.sign(NostrUtil.sha256(event.get_serializedEvent()), privateKey.getRawData(), generateAuxRand());
        final Signature signature = Signature.builder().rawData(signedHashedSerializedEvent).pubKey(this.getPublicKey()).build();
        event.setSignature(signature);
        return signature;
    }

    private Signature signDelegationTag(@NonNull DelegationTag delegationTag) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, Exception {
        final var signedHashedToken = Schnorr.sign(NostrUtil.sha256(delegationTag.getToken().getBytes(StandardCharsets.UTF_8)), privateKey.getRawData(), generateAuxRand());
        final Signature signature = Signature.builder().rawData(signedHashedToken).pubKey(this.getPublicKey()).build();
        delegationTag.setSignature(signature);
        return signature;
    }

    private byte[] generateAuxRand() {
        return NostrUtil.createRandomByteArray(32);
    }

    /**
     *
     * @param senderPrivateKey
     * @param rcptPublicKey
     * @param message
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private static String encryptMessage(byte[] senderPrivateKey, byte[] rcptPublicKey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NostrException {

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

    private static SecretKeySpec getSharedSecretKeySpec(byte[] privateKey, byte[] publicKey) throws NostrException {
        final String secKeyHex = NostrUtil.bytesToHex(privateKey);
        final String pubKeyHex = NostrUtil.bytesToHex(publicKey);

        var sharedPoint = getSharedSecret(secKeyHex, pubKeyHex);
        var sharedX = Arrays.copyOfRange(sharedPoint, 1, 33);

        return new SecretKeySpec(sharedX, "AES");
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

    private static byte[] getSharedSecret(String privateKeyHex, String publicKeyHex) throws NostrException {

        SecP256K1Curve curve = new SecP256K1Curve();
        ECPoint pubKeyPt = curve.decodePoint(NostrUtil.hexToBytes("02" + publicKeyHex));
        BigInteger tweakVal = new BigInteger(1, NostrUtil.hexToBytes(privateKeyHex));
        return pubKeyPt.multiply(tweakVal).getEncoded(true);
    }

    private static PublicKey generatePublicKey(PrivateKey privateKey) throws Exception {
        var rawDate = Schnorr.genPubKey(privateKey.getRawData());
        return new PublicKey(rawDate);
    }

    @Log
    static class IdentityConfiguration extends BaseConfiguration {

        IdentityConfiguration(String propertyFile) throws IOException {
            super(propertyFile);
        }

        PrivateKey getPrivateKey() throws IOException, NostrException {
            String privKey = getProperty("privateKey");
            log.log(Level.FINE, "Reading the private key...");

            String hex = privKey.startsWith(Bech32Prefix.NSEC.getCode()) ? Bech32.fromBech32(privKey) : privKey;
            return new PrivateKey(hex);
        }

        PublicKey getPublicKey() throws NostrException, IOException {
            String pubKey = getProperty("publickKey");
            if (pubKey == null || "".equals(pubKey.trim())) {
                log.log(Level.FINE, "Generating new public key");
                try {
                    return Identity.generatePublicKey(getPrivateKey());
                } catch (Exception ex) {
                    log.log(Level.SEVERE, null, ex);
                    throw new NostrException(ex);
                }
            } else {
                String hex = pubKey.startsWith(Bech32Prefix.NPUB.getCode()) ? Bech32.fromBech32(pubKey) : pubKey;
                return new PublicKey(hex);
            }
        }
    }

}
