package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.Signature;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenericEventValidateTest {

    private static final String HEX_64_A = "a".repeat(64);
    private static final String HEX_64_B = "b".repeat(64);
    private static final String SIG_HEX = "c".repeat(128);

    @Test
    public void testValidateMissingId() {
        GenericEvent event = new GenericEvent(new PublicKey(HEX_64_A), 1);
        event.setSignature(Signature.fromString(SIG_HEX));
        event.setCreatedAt(Instant.now().getEpochSecond());

        NullPointerException ex = assertThrows(NullPointerException.class, event::validate);
        assertEquals("Missing required `id` field.", ex.getMessage());
    }

    @Test
    public void testValidateMissingPubKey() {
        GenericEvent event = new GenericEvent();
        event.setId(HEX_64_A);
        event.setSignature(Signature.fromString(SIG_HEX));
        event.setCreatedAt(Instant.now().getEpochSecond());
        event.setKind(1);

        NullPointerException ex = assertThrows(NullPointerException.class, event::validate);
        assertEquals("Missing required `pubkey` field.", ex.getMessage());
    }

    @Test
    public void testValidateMissingSignature() {
        GenericEvent event = new GenericEvent(new PublicKey(HEX_64_A), 1);
        event.setId(HEX_64_B);
        event.setCreatedAt(Instant.now().getEpochSecond());

        NullPointerException ex = assertThrows(NullPointerException.class, event::validate);
        assertEquals("Missing required `sig` field.", ex.getMessage());
    }
}
