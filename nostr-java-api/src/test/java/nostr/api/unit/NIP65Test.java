package nostr.api.unit;

import nostr.api.NIP65;
import nostr.base.Marker;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NIP65Test {

    @Test
    public void testCreateRelayListMetadataEvent() {
        Identity sender = Identity.generateRandomIdentity();
        NIP65 nip65 = new NIP65(sender);
        Relay relay = new Relay("wss://relay");
        nip65.createRelayListMetadataEvent(List.of(relay), Marker.READ);
        GenericEvent event = nip65.getEvent();
        assertEquals("r", event.getTags().get(0).getCode());
    }
}
