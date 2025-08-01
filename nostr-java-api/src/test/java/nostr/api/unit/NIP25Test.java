package nostr.api.unit;

import nostr.api.NIP01;
import nostr.api.NIP25;
import nostr.event.BaseTag;
import nostr.event.entities.Reaction;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NIP25Test {

    @Test
    public void testCreateReactionEvent() {
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);
        GenericEvent note = nip01.createTextNoteEvent("hi").getEvent();

        NIP25 nip25 = new NIP25(sender);
        nip25.createReactionEvent(note, Reaction.LIKE, null);
        GenericEvent event = nip25.getEvent();

        assertEquals("+", event.getContent());
        assertTrue(event.getTags().stream().anyMatch(t -> t instanceof EventTag));
    }
}
