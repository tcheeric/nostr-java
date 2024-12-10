package nostr.test.crypto;

import nostr.base.Signature;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import org.junit.jupiter.api.Test;

import static nostr.test.EntityFactory.Events.createTextNoteEvent;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author squirrel
 */
public class CryptoTest {

    @Test
    public void testBech32() {
        try {
            System.out.println("testBech32");

            final String hexPub = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
            final String npub = "npub126klq89p42wk78p4j5ur8wlxmxdqepdh8tez9e4axpd4run5nahsmff27j";

            assertEquals(npub, Bech32.toBech32(Bech32Prefix.NPUB, hexPub));
            assertEquals("56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f", Bech32.fromBech32(npub));
        } catch (NostrException ex) {
            fail(ex);
        }
    }

    @Test
    public void testVerifySignature() {
        System.out.println("testVerifySignature");

        Identity identity = Identity.generateRandomIdentity();
        final GenericEvent[] event = {createTextNoteEvent(identity.getPublicKey(), "Hello World")};
        event[0].update();
        assertDoesNotThrow(() -> {
            byte[] message = NostrUtil.sha256(event[0].get_serializedEvent());
            Signature signature = identity.sign(event[0]);
            boolean verification = Schnorr.verify(message, identity.getPublicKey().getRawData(), signature.getRawData());
            assertTrue(verification, "Schnorr must have a true verify result.");

            event[0] = createTextNoteEvent(identity.getPublicKey(), "Guten Tag");
            event[0].update();
            message = NostrUtil.sha256(event[0].get_serializedEvent());
            verification = Schnorr.verify(message, identity.getPublicKey().getRawData(), signature.getRawData());

            assertFalse(verification);
        });
    }

}
