package nostr.api.unit;

import nostr.api.NIP08;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NIP08Test {

    @Test
    public void testCreateMentionsEvent() {
        Identity sender = Identity.generateRandomIdentity();
        Identity recipient = Identity.generateRandomIdentity();
        BaseTag pTag = new PubKeyTag(recipient.getPublicKey());

        NIP08 nip08 = new NIP08(sender);
        nip08.createMentionsEvent(Constants.Kind.SHORT_TEXT_NOTE, List.of(pTag), "hi");
        GenericEvent event = nip08.getEvent();

        assertEquals(Constants.Kind.SHORT_TEXT_NOTE, event.getKind());
        assertTrue(event.getTags().contains(pTag));
        assertEquals("hi", event.getContent());
    }
}
