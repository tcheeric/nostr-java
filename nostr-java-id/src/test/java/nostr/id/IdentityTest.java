package nostr.id;

import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.ISignable;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.DelegationTag;
import nostr.util.NostrUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author squirrel
 */
public class IdentityTest {

    public IdentityTest() {
    }

    @Test
    public void testSignEvent() {
        System.out.println("testSignEvent");
        Identity identity = Identity.generateRandomIdentity();
        PublicKey publicKey = identity.getPublicKey();
        GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
        identity.sign(instance);
        Assertions.assertNotNull(instance.getSignature());
    }

    @Test
    public void testSignDelegationTag() {
        System.out.println("testSignDelegationTag");
        Identity identity = Identity.generateRandomIdentity();
        PublicKey publicKey = identity.getPublicKey();
        DelegationTag delegationTag = new DelegationTag(publicKey, null);
        identity.sign(delegationTag);
        Assertions.assertNotNull(delegationTag.getSignature());
    }

    @Test
    public void testGenerateRandomIdentityProducesUniqueKeys() {
        Identity id1 = Identity.generateRandomIdentity();
        Identity id2 = Identity.generateRandomIdentity();
        Assertions.assertNotEquals(id1.getPrivateKey(), id2.getPrivateKey());
    }

    @Test
    public void testGetPublicKeyDerivation() {
        String privHex = "0000000000000000000000000000000000000000000000000000000000000001";
        Identity identity = Identity.create(privHex);
        PublicKey expected = new PublicKey("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        Assertions.assertEquals(expected, identity.getPublicKey());
    }

    @Test
    public void testSignProducesValidSignature() throws Exception {
        String privHex = "0000000000000000000000000000000000000000000000000000000000000001";
        Identity identity = Identity.create(privHex);
        final byte[] message = "hello".getBytes(StandardCharsets.UTF_8);

        ISignable signable = new ISignable() {
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
        boolean verified = Schnorr.verify(msgHash, identity.getPublicKey().getRawData(), signable.getSignature().getRawData());
        Assertions.assertTrue(verified);
    }

    @Test
    public void testSignWithNullConsumer() {
        Identity identity = Identity.generateRandomIdentity();
        ISignable signable = new ISignable() {
            @Override
            public Signature getSignature() {
                return null;
            }

            @Override
            public void setSignature(Signature signature) {
            }

            @Override
            public Consumer<Signature> getSignatureConsumer() {
                return null;
            }

            @Override
            public Supplier<ByteBuffer> getByteArraySupplier() {
                return () -> ByteBuffer.wrap("payload".getBytes(StandardCharsets.UTF_8));
            }
        };
        Signature signature = Assertions.assertDoesNotThrow(() -> identity.sign(signable));
        Assertions.assertNotNull(signature);
        Assertions.assertNull(signable.getSignature());
    }
}
