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
import nostr.util.AbstractBaseConfiguration;
import nostr.util.NostrException;

import java.io.IOException;
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
        this.privateKey = new IdentityConfiguration().getPrivateKey();
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
            return IdentityHelper.generatePublicKey(this.privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Signature sign(@NonNull ISignable signable) {
        try {
            return new IdentityHelper(this).sign(signable);
        } catch (NostrException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return A strong pseudo random Identity
     */
    public static Identity generateRandomIdentity() {
        return new Identity(PrivateKey.generateRandomPrivKey());
    }

    @Log
    static class IdentityConfiguration extends AbstractBaseConfiguration {

        IdentityConfiguration() throws IOException {
            super();
            var configFile = appConfig.getIdentityProperties();
            configFile = configFile.startsWith("/") ? configFile : "/" + configFile;
            load(configFile);
        }

        PrivateKey getPrivateKey() throws NostrException {
            String privKey = getProperty("privateKey");
            log.log(Level.FINE, "Reading the private key...");

            if (privKey == null) {
                throw new RuntimeException("Missing private key. Aborting....");
            }
            String hex = privKey.startsWith(Bech32Prefix.NSEC.getCode()) ? Bech32.fromBech32(privKey) : privKey;
            return new PrivateKey(hex);
        }

        PublicKey getPublicKey() throws NostrException {
            String pubKey = getProperty("publicKey");
            if (pubKey == null || pubKey.trim().isEmpty()) {
                log.log(Level.FINE, "Generating new public key");
                try {
                    return IdentityHelper.generatePublicKey(getPrivateKey());
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
