package nostr.api.unit;

import nostr.api.NIP28;
import nostr.config.Constants;
import nostr.event.entities.ChannelProfile;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NIP28Test {

    @Test
    public void testCreateChannelCreateEvent() throws Exception {
        Identity sender = Identity.generateRandomIdentity();
        NIP28 nip28 = new NIP28(sender);
        ChannelProfile profile = new ChannelProfile("channel","about", new java.net.URL("https://example.com"));
        nip28.createChannelCreateEvent(profile);
        GenericEvent event = nip28.getEvent();

        assertEquals(Constants.Kind.CHANNEL_CREATION, event.getKind());
        assertTrue(event.getContent().contains("channel"));
    }
}
