package nostr.api.unit;

import nostr.api.NIP01;
import nostr.api.NIP09;
import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NIP09Test {

    @Test
    public void testCreateDeletionEvent() {
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);
        GenericEvent note = nip01.createTextNoteEvent("del me").getEvent();

        NIP09 nip09 = new NIP09(sender);
        nip09.createDeletionEvent(List.of(note));
        GenericEvent event = nip09.getEvent();

        assertEquals(Constants.Kind.EVENT_DELETION, event.getKind());
        assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals("e")));
    }
}
