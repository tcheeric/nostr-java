package nostr.id;

import nostr.base.ISignable;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.schnorr.Schnorr;
import nostr.crypto.schnorr.SchnorrException;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.DelegationTag;
import nostr.util.NostrUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author squirrel
 */
public class IdentityTest {

  public IdentityTest() {}

  @Test
  // Ensures signing a text note event attaches a signature
  public void testSignEvent() {
    System.out.println("testSignEvent");
    Identity identity = Identity.generateRandomIdentity();
    PublicKey publicKey = identity.getPublicKey();
    GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
    identity.sign(instance);
    Assertions.assertNotNull(instance.getSignature());
  }

  @Test
  // Ensures signing a delegation tag populates its signature
  public void testSignDelegationTag() {
    System.out.println("testSignDelegationTag");
    Identity identity = Identity.generateRandomIdentity();
    PublicKey publicKey = identity.getPublicKey();
    DelegationTag delegationTag = new DelegationTag(publicKey, null);
    identity.sign(delegationTag);
    Assertions.assertNotNull(delegationTag.getSignature());
  }

  @Test
  // Verifies that generating random identities yields unique private keys
  public void testGenerateRandomIdentityProducesUniqueKeys() {
    Identity id1 = Identity.generateRandomIdentity();
    Identity id2 = Identity.generateRandomIdentity();
    Assertions.assertNotEquals(id1.getPrivateKey(), id2.getPrivateKey());
  }

  @Test
  // Confirms that deriving the public key from a known private key matches expectations
  public void testGetPublicKeyDerivation() {
    String privHex = "0000000000000000000000000000000000000000000000000000000000000001";
    Identity identity = Identity.create(privHex);
    PublicKey expected =
        new PublicKey("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
    Assertions.assertEquals(expected, identity.getPublicKey());
  }

  @Test
  // Verifies that signing produces a Schnorr signature that validates successfully
  public void testSignProducesValidSignature()
      throws NoSuchAlgorithmException, SchnorrException {
    String privHex = "0000000000000000000000000000000000000000000000000000000000000001";
    Identity identity = Identity.create(privHex);
    final byte[] message = "hello".getBytes(StandardCharsets.UTF_8);

    ISignable signable =
        new ISignable() {
          private Signature signature;

          @Override
          public Signature getSignature() {
            return signature;
          }

          @Override
          public void setSignature(Signature signature) {
            this.signature = signature;
          }

          @Override
          public Consumer<Signature> getSignatureConsumer() {
            return this::setSignature;
          }

          @Override
          public Supplier<ByteBuffer> getByteArraySupplier() {
            return () -> ByteBuffer.wrap(message);
          }
        };

    identity.sign(signable);

    byte[] msgHash = NostrUtil.sha256(message);
    boolean verified =
        Schnorr.verify(
            msgHash, identity.getPublicKey().getRawData(), signable.getSignature().getRawData());
    Assertions.assertTrue(verified);
  }

  @Test
  // Confirms public key derivation is cached for subsequent calls
  public void testPublicKeyCaching() {
    Identity identity = Identity.generateRandomIdentity();
    PublicKey first = identity.getPublicKey();
    PublicKey second = identity.getPublicKey();
    Assertions.assertSame(first, second);
  }

  @Test
  // Ensures that invalid private keys trigger a derivation failure
  public void testGetPublicKeyFailure() {
    String invalidPriv = "0000000000000000000000000000000000000000000000000000000000000000";
    Identity identity = Identity.create(invalidPriv);
    Assertions.assertThrows(IllegalStateException.class, identity::getPublicKey);
  }

  @Test
  // Ensures that signing with an invalid private key throws SigningException
  public void testSignWithInvalidKeyFails() {
    String invalidPriv = "0000000000000000000000000000000000000000000000000000000000000000";
    Identity identity = Identity.create(invalidPriv);

    ISignable signable =
        new ISignable() {
          private Signature signature;

          @Override
          public Signature getSignature() {
            return signature;
          }

          @Override
          public void setSignature(Signature signature) {
            this.signature = signature;
          }

          @Override
          public Consumer<Signature> getSignatureConsumer() {
            return this::setSignature;
          }

          @Override
          public Supplier<ByteBuffer> getByteArraySupplier() {
            return () -> ByteBuffer.wrap("msg".getBytes(StandardCharsets.UTF_8));
          }
        };

    Assertions.assertThrows(SigningException.class, () -> identity.sign(signable));
  }
}
