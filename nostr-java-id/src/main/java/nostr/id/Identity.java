package nostr.id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.schnorr.Schnorr;
import nostr.util.NostrUtil;

/**
 * @author squirrel
 */
@EqualsAndHashCode
@Data
@Slf4j
public class Identity {

    @ToString.Exclude
    private final PrivateKey privateKey;

    private Identity(@NonNull PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public static Identity create(@NonNull PrivateKey privateKey) {
        return new Identity(privateKey);
    }

    public static Identity create(@NonNull String privateKey) {
        return new Identity(new PrivateKey(privateKey));
    }

    /**
     * @return A strong pseudo random identity
     */
    public static Identity generateRandomIdentity() {
        return new Identity(PrivateKey.generateRandomPrivKey());
    }

    public PublicKey getPublicKey() {
        try {
            return new PublicKey(Schnorr.genPubKey(this.getPrivateKey().getRawData()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // TODO: refine exception handling strategy
    public Signature sign(@NonNull ISignable signable) {
        final Signature signature = new Signature();
        ByteBuffer buffer = signable.getByteArraySupplier().get();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        try {
            signature.setRawData(
                    Schnorr.sign(
                            NostrUtil.sha256(data),
                            this.getPrivateKey().getRawData(),
                            generateAuxRand()));
            signature.setPubKey(getPublicKey());
            signable.getSignatureConsumer().accept(signature);
            return signature;
        } catch (NoSuchAlgorithmException ex) {
            log.error("SHA-256 algorithm not available for signing", ex);
            throw new RuntimeException("SHA-256 algorithm not available", ex);
        } catch (Exception ex) {
            InvalidKeyException ike = new InvalidKeyException("Failed to sign with provided key");
            ike.initCause(ex);
            log.error("Signing failed", ike);
            throw new RuntimeException("Signing failed", ike);
        }
    }

    private byte[] generateAuxRand() {
        return NostrUtil.createRandomByteArray(32);
    }
}
