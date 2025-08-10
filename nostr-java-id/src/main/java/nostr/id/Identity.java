package nostr.id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
 * @author squirrel
 */
@EqualsAndHashCode
@Data
@Slf4j
public class Identity {

    @ToString.Exclude
    private final PrivateKey privateKey;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private transient PublicKey cachedPublicKey;

    private Identity(@NonNull PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Deprecated(forRemoval = true)
    public static Identity getInstance(@NonNull PrivateKey privateKey) {
        return new Identity(privateKey);
    }

    public static Identity create(@NonNull PrivateKey privateKey) {
        return new Identity(privateKey);
    }

    @Deprecated(forRemoval = true)
    public static Identity getInstance(@NonNull String privateKey) {
        return new Identity(new PrivateKey(privateKey));
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
        if (cachedPublicKey == null) {
            try {
                cachedPublicKey = new PublicKey(Schnorr.genPubKey(this.getPrivateKey().getRawData()));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid private key", ex);
            }
        }
        return cachedPublicKey;
    }

//    TODO: exceptions refactor
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
