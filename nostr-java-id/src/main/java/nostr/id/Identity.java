package nostr.id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.schnorr.Schnorr;
import nostr.util.NostrUtil;

/**
 * Represents a Nostr identity backed by a private key.
 * <p>
 * Instances of this class can derive the associated public key and
 * sign arbitrary {@link ISignable} objects.
 * </p>
 *
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

    @Deprecated(forRemoval = true)
    public static Identity getInstance(@NonNull PrivateKey privateKey) {
        return new Identity(privateKey);
    }

    /**
     * Creates a new identity from an existing {@link PrivateKey}.
     *
     * @param privateKey the private key that will back the identity
     * @return a new identity using the provided key
     * @throws NullPointerException if {@code privateKey} is {@code null}
     */
    public static Identity create(@NonNull PrivateKey privateKey) {
        return new Identity(privateKey);
    }

    @Deprecated(forRemoval = true)
    public static Identity getInstance(@NonNull String privateKey) {
        return new Identity(new PrivateKey(privateKey));
    }

    /**
     * Creates a new identity from a hex-encoded private key.
     *
     * @param privateKey the private key represented as a hex string
     * @return a new identity using the provided key
     * @throws IllegalArgumentException if the key cannot be parsed
     * @throws NullPointerException     if {@code privateKey} is {@code null}
     */
    public static Identity create(@NonNull String privateKey) {
        return new Identity(new PrivateKey(privateKey));
    }

    /**
     * Generates a strong pseudo-random identity.
     *
     * @return a new identity backed by a cryptographically secure random
     * private key
     */
    public static Identity generateRandomIdentity() {
        return new Identity(PrivateKey.generateRandomPrivKey());
    }

    /**
     * Derives the {@link PublicKey} associated with this identity's private key.
     *
     * @return the derived public key
     * @throws RuntimeException if public key generation fails
     */
    public PublicKey getPublicKey() {
        try {
            return new PublicKey(Schnorr.genPubKey(this.getPrivateKey().getRawData()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    //    TODO: exceptions refactor
    /**
     * Signs the supplied {@link ISignable} using this identity's private key.
     * The resulting {@link Signature} is returned and also provided to the
     * signable's signature consumer.
     *
     * @param signable the entity to sign
     * @return the generated signature
     * @throws Exception if the signature cannot be created
     */
    @SneakyThrows
    public Signature sign(@NonNull ISignable signable) {
        final Signature signature = new Signature();
        signature.setRawData(
                Schnorr.sign(
                        NostrUtil.sha256(signable.getByteArraySupplier().get().array()),
                        this.getPrivateKey().getRawData(),
                        generateAuxRand()));
        signature.setPubKey(getPublicKey());
        signable.getSignatureConsumer().accept(signature);
        return signature;
    }

    private byte[] generateAuxRand() {
        return NostrUtil.createRandomByteArray(32);
    }
}
