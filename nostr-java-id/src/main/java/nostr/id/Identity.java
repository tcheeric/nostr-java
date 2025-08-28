package nostr.id;

import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import lombok.Data;
import lombok.NonNull;
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
 *
 * <p>Instances of this class can derive the associated public key and sign arbitrary {@link
 * ISignable} objects.
 *
 * @author squirrel
 */
@Slf4j
@Data
public class Identity {

  @ToString.Exclude private final PrivateKey privateKey;

  @ToString.Exclude private PublicKey cachedPublicKey;

  private Identity(@NonNull PrivateKey privateKey) {
    this.privateKey = privateKey;
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

  /**
   * Creates a new identity from a hex-encoded private key.
   *
   * @param privateKey the private key represented as a hex string
   * @return a new identity using the provided key
   * @throws IllegalArgumentException if the key cannot be parsed
   * @throws NullPointerException if {@code privateKey} is {@code null}
   */
  public static Identity create(@NonNull String privateKey) {
    return new Identity(new PrivateKey(privateKey));
  }

  /**
   * Generates a strong pseudo-random identity.
   *
   * @return a new identity backed by a cryptographically secure random private key
   */
  public static Identity generateRandomIdentity() {
    return new Identity(PrivateKey.generateRandomPrivKey());
  }

  /**
   * Derives the {@link PublicKey} associated with this identity's private key.
   *
   * @return the derived public key
   * @throws IllegalStateException if public key generation fails
   */
  public PublicKey getPublicKey() {
    if (cachedPublicKey == null) {
      try {
        cachedPublicKey = new PublicKey(Schnorr.genPubKey(this.getPrivateKey().getRawData()));
      } catch (Exception ex) {
        log.error("Failed to derive public key", ex);
        throw new IllegalStateException("Failed to derive public key", ex);
      }
    }
    return cachedPublicKey;
  }

  /**
   * Signs the supplied {@link ISignable} using this identity's private key. The resulting {@link
   * Signature} is returned and also provided to the signable's signature consumer.
   *
   * @param signable the entity to sign
   * @return the generated signature
   * @throws IllegalStateException if the SHA-256 algorithm is unavailable
   * @throws SigningException if the signature cannot be created
   */
  public Signature sign(@NonNull ISignable signable) {
    try {
      final Signature signature = new Signature();
      signature.setRawData(
          Schnorr.sign(
              NostrUtil.sha256(signable.getByteArraySupplier().get().array()),
              this.getPrivateKey().getRawData(),
              generateAuxRand()));
      signature.setPubKey(getPublicKey());
      Consumer<Signature> consumer = signable.getSignatureConsumer();
      if (consumer != null) {
        consumer.accept(signature);
      }
      return signature;
    } catch (NoSuchAlgorithmException ex) {
      log.error("SHA-256 algorithm not available for signing", ex);
      throw new IllegalStateException("SHA-256 algorithm not available", ex);
    } catch (Exception ex) {
      log.error("Signing failed", ex);
      throw new SigningException("Failed to sign with provided key", ex);
    }
  }

  private byte[] generateAuxRand() {
    return NostrUtil.createRandomByteArray(32);
  }
}
