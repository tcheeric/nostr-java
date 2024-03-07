package nostr.id;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.DelegationTag;
import nostr.util.AbstractBaseConfiguration;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * @author squirrel
 */
@Data
@Log
public class Identity implements IIdentity {

    private static Identity INSTANCE;

    @ToString.Exclude
    private final PrivateKey privateKey;

    private Identity() throws IOException, NostrException {
        this.privateKey = new IdentityConfiguration("").getPrivateKey();
    }

    public Identity(@NonNull PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public static Identity getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new Identity();
            } catch (IOException | NostrException ex) {
                throw new RuntimeException(ex);
            }
        }

        return INSTANCE;
    }

    public static Identity getInstance(@NonNull PrivateKey privateKey) {
        if (INSTANCE == null) {
            INSTANCE = new Identity(privateKey);
        }

        return INSTANCE;
    }

    public PublicKey getPublicKey() {
        try {
            return generatePublicKey(this.privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return A strong pseudo random Identity
     */
    public static Identity generateRandomIdentity() {
        return new Identity(PrivateKey.generateRandomPrivKey());
    }

    public Signature sign(@NonNull ISignable signable) {
        if (signable instanceof GenericEvent genericEvent) {
            try {
                return signEvent(genericEvent);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else if (signable instanceof DelegationTag delegationTag) {
            try {
                return signDelegationTag(delegationTag);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        throw new RuntimeException();
    }

    private Signature signEvent(@NonNull GenericEvent event) throws Exception {
        event.update();
        log.log(Level.FINER, "Serialized event: {0}", new String(event.get_serializedEvent()));
        final var signedHashedSerializedEvent = Schnorr.sign(NostrUtil.sha256(event.get_serializedEvent()), this.privateKey.getRawData(), generateAuxRand());
        final Signature signature = new Signature();
        signature.setRawData(signedHashedSerializedEvent);
        signature.setPubKey(getPublicKey());
        event.setSignature(signature);
        return signature;
    }

    private Signature signDelegationTag(@NonNull DelegationTag delegationTag) throws Exception {
        final var signedHashedToken = Schnorr.sign(NostrUtil.sha256(delegationTag.getToken().getBytes(StandardCharsets.UTF_8)), this.privateKey.getRawData(), generateAuxRand());
        final Signature signature = new Signature();
        signature.setRawData(signedHashedToken);
        signature.setPubKey(getPublicKey());
        delegationTag.setSignature(signature);
        return signature;
    }

    private static PublicKey generatePublicKey(@NonNull PrivateKey privateKey) {
        try {
            return new PublicKey(Schnorr.genPubKey(privateKey.getRawData()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private byte[] generateAuxRand() {
        return NostrUtil.createRandomByteArray(32);
    }


    @Log
    static class IdentityConfiguration extends AbstractBaseConfiguration {

        IdentityConfiguration(@NonNull String name) throws IOException {
            super(name, CONFIG_TYPE_IDENTITY);
        }

        PrivateKey getPrivateKey() throws NostrException {
            String privKey = getProperty("privateKey");

            if (privKey == null) {
                throw new RuntimeException("Missing private key. Aborting....");
            }
            String hex = privKey.startsWith(Bech32Prefix.NSEC.getCode()) ? Bech32.fromBech32(privKey) : privKey;
            return new PrivateKey(hex);
        }

        PublicKey getPublicKey() throws NostrException {
            String pubKey = getProperty("publicKey");
            if (pubKey == null || pubKey.trim().isEmpty()) {
                log.log(Level.FINE, "Generating new public key...");
                try {
                    return generatePublicKey(getPrivateKey());
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
