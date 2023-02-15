package nostr.id;

import nostr.base.BaseConfiguration;
import nostr.base.ISignable;
import nostr.base.ITag;
import nostr.util.NostrUtil;
import nostr.base.PrivateKey;
import nostr.base.Profile;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.tag.DelegationTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.crypto.schnorr.Schnorr;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.util.NostrException;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

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

    private final Profile profile;

    public Identity() throws IOException, NostrException {
        this("/profile.properties");
    }

    public Identity(String profileFile) throws IOException, NostrException {
        this.privateKey = new ProfileConfiguration(profileFile).getPrivateKey();
        this.profile = new ProfileConfiguration(profileFile).getProfile();
    }

    public Identity(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.profile = Profile.builder().publicKey(publicKey).build();
    }

    public void encryptDirectMessage(@NonNull DirectMessageEvent dmEvent) throws NostrException {
        ITag pkTag = (ITag) dmEvent.getTags().getList().get(0);
        if (pkTag instanceof PubKeyTag pubKeyTag) {
            try {
                var publicKey = pubKeyTag.getPublicKey().getRawData();
                var encryptedContent = encryptMessage(privateKey.getRawData(), publicKey, dmEvent.getContent());
                dmEvent.setContent(encryptedContent);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new NostrException(ex);
            }
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
        final Signature signature = Signature.builder().rawData(signedHashedSerializedEvent).pubKey(this.profile.getPublicKey()).build();
        event.setSignature(signature);
        return signature;
    }

    private Signature signDelegationTag(@NonNull DelegationTag delegationTag) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, Exception {
        final var signedHashedToken = Schnorr.sign(NostrUtil.sha256(delegationTag.getToken().getBytes(StandardCharsets.UTF_8)), privateKey.getRawData(), generateAuxRand());
        final Signature signature = Signature.builder().rawData(signedHashedToken).pubKey(this.profile.getPublicKey()).build();
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

        final var Base64Encoder = Base64.getEncoder();
        final var msg = message.getBytes(StandardCharsets.UTF_8);

        final String secKeyHex = NostrUtil.bytesToHex(senderPrivateKey);
        final String pubKeyHex = NostrUtil.bytesToHex(rcptPublicKey);

        var sharedPoint = getSharedSecret(secKeyHex, pubKeyHex);
        var sharedX = Arrays.copyOfRange(sharedPoint, 1, 33);

        var iv = NostrUtil.createRandomByteArray(16);
        var ivParamSpec = new IvParameterSpec(iv);

        var sharedSecretKey = new SecretKeySpec(sharedX, "AES");
        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey, ivParamSpec);

        var encryptedMessage = cipher.doFinal(msg);
        var encryptedMessage64 = Base64Encoder.encode(encryptedMessage);

        var iv64 = Base64Encoder.encode(ivParamSpec.getIV());

        return new String(encryptedMessage64) + "?iv=" + new String(iv64);
    }

    private static byte[] getSharedSecret(String privateKeyHex, String publicKeyHex) throws NostrException {

        SecP256K1Curve curve = new SecP256K1Curve();
        ECPoint pubKeyPt = curve.decodePoint(NostrUtil.hexToBytes("02" + publicKeyHex));
        BigInteger tweakVal = new BigInteger(1, NostrUtil.hexToBytes(privateKeyHex));
        return pubKeyPt.multiply(tweakVal).getEncoded(true);
    }

    @Log
    static class ProfileConfiguration extends BaseConfiguration {

        ProfileConfiguration(String profileFile) throws IOException {
            super(profileFile);
        }

        Profile getProfile() throws NostrException, IOException {
            log.log(Level.FINE, "Getting the profile details from the configuration file...");
            return Profile.builder().about(getAbout()).nip05(getNip05()).name(getName()).picture(getPicture()).publicKey(getPublicKey()).build();
        }

        String getName() {
            return getProperty("name");
        }

        String getAbout() {
            return getProperty("about");
        }

        URL getPicture() {
            try {
                final String pic = getProperty("picture");
                if (pic != null) {
                    return new URL(pic);
                }
            } catch (MalformedURLException ex) {
                log.log(Level.SEVERE, null, ex);
                return null;
            }

            return null;
        }

        String getNip05() {
            return getProperty("nip05");
        }

        PrivateKey getPrivateKey() throws IOException {
            String privKey = getProperty("privateKey");
            log.log(Level.FINE, "Reading the private key...");

            if (privKey.startsWith("file://")) {
                return new PrivateKey(Files.readAllBytes(Paths.get(privKey)));
            } else {
                return new PrivateKey(NostrUtil.hexToBytes(privKey));
            }
        }

        PublicKey getPublicKey() throws NostrException, IOException {
            String pubKey = getProperty("publickKey");
            if (pubKey == null || "".equals(pubKey.trim())) {
                log.log(Level.FINE, "Generating new public key");
                try {
                    return new PublicKey(Schnorr.genPubKey(getPrivateKey().getRawData()));
                } catch (Exception ex) {
                    log.log(Level.SEVERE, null, ex);
                    throw new NostrException(ex);
                }
            } else if (pubKey.startsWith("file://")) {
                return new PublicKey(Files.readAllBytes(Paths.get(pubKey)));
            } else {
                return new PublicKey(NostrUtil.hexToBytes(pubKey));
            }
        }
    }

}
