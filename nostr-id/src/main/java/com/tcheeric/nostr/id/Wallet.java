
package com.tcheeric.nostr.id;

import com.tcheeric.nostr.base.BaseConfiguration;
import com.tcheeric.nostr.base.ISignable;
import com.tcheeric.nostr.base.ITag;
import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.NostrUtil;
import com.tcheeric.nostr.base.PrivateKey;
import com.tcheeric.nostr.base.Profile;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.base.Signature;
import com.tcheeric.nostr.event.impl.DirectMessageEvent;
import com.tcheeric.nostr.event.tag.DelegationTag;
import com.tcheeric.nostr.event.impl.GenericEvent;
import com.tcheeric.nostr.event.tag.PubKeyTag;
import com.tcheeric.schnorr.Point;
import com.tcheeric.schnorr.Schnorr;
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

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode
@ToString
public class Wallet {

    @ToString.Exclude
    private final PrivateKey privateKey;

    private final Profile profile;

    public Wallet() throws IOException, NostrException {
        this("/profile.properties");
    }

    public Wallet(String profileFile) throws IOException, NostrException {
        this.privateKey = new ProfileConfiguration(profileFile).getPrivateKey();
        this.profile = new ProfileConfiguration(profileFile).getProfile();
    }

    public Wallet(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.profile = Profile.builder().publicKey(publicKey).build();
    }

    public void encryptDirectMessage(@NonNull DirectMessageEvent dmEvent) throws NostrException {
        ITag pkTag = (ITag) dmEvent.getTags().getList().get(0);
        if (pkTag instanceof PubKeyTag) {
            try {
                byte[] publicKey = ((PubKeyTag) pkTag).getPublicKey().getRawData();
                var encryptedContent = encryptMessage(privateKey.getRawData(), publicKey, dmEvent.getContent());
                dmEvent.setContent(encryptedContent);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new NostrException(ex);
            }
        }
    }

    public Signature sign(@NonNull ISignable signable) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, Exception {
        if (signable instanceof GenericEvent) {
            return signEvent((GenericEvent) signable);
        } else if (signable instanceof DelegationTag) {
            return signDelegationTag((DelegationTag) signable);
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

        var sharedPoint = getSharedSecret(NostrUtil.bytesToHex(senderPrivateKey), "02" + NostrUtil.bytesToHex(rcptPublicKey));        
        var sharedX = Arrays.copyOfRange(sharedPoint, 1, 33);

        SecretKeySpec sharedSecretKey = new SecretKeySpec(sharedX, "AES");

        var iv = NostrUtil.createRandomByteArray(16);
        var ivParamSpec = new IvParameterSpec(iv);
        
        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey, ivParamSpec);
        var encryptedMessage1 = Base64Encoder.encode(cipher.update(msg));
        var encryptedMessage2 = Base64Encoder.encode(cipher.doFinal());

        var result = Arrays.copyOf(encryptedMessage1, encryptedMessage1.length + encryptedMessage2.length);
        System.arraycopy(encryptedMessage2, 0, result, encryptedMessage1.length, encryptedMessage2.length);

        var iv64 = Base64Encoder.encode(ivParamSpec.getIV());

        return new String(result) + "?iv=" + new String(iv64);
    }

    private static byte[] getSharedSecret(String privateKeyHex, String publicKeyHex) throws NostrException {

        Point pubKeyPt = Point.fromHex(publicKeyHex);

        BigInteger privKey = new BigInteger(NostrUtil.hexToBytes(privateKeyHex));

        return Point.mul(pubKeyPt, privKey).toBytes();
    }

    @Log
    static class ProfileConfiguration extends BaseConfiguration {

        ProfileConfiguration(String profileFile) throws IOException {
            super(profileFile);
        }

        Profile getProfile() throws NostrException, IOException {
            log.log(Level.FINE, "Getting profile details from configuration file");
            return Profile.builder().about(getAbout()).email(getEmail()).name(getName()).picture(getPicture()).publicKey(getPublicKey()).build();
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

        String getEmail() {
            return getProperty("email");
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
