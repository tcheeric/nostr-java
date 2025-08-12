package nostr.api.unit;

import nostr.api.NIP01;
import nostr.api.NIP20;
import nostr.event.impl.GenericEvent;
import nostr.event.message.OkMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NIP20Test {

    @Test
    public void testCreateOkMessage() {
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);
        GenericEvent event = nip01.createTextNoteEvent("msg").getEvent();
        OkMessage ok = NIP20.createOkMessage(event, true, "ok");
        assertEquals(event.getId(), ok.getEventId());
        assertTrue(ok.getFlag());
        assertEquals("ok", ok.getMessage());
    }
}
