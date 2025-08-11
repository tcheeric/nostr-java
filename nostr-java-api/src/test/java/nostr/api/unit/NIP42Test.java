package nostr.api.unit;

import nostr.api.NIP42;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NIP42Test {

    @Test
    public void testCreateTags() {
        Relay relay = new Relay("wss://relay");
        BaseTag rTag = NIP42.createRelayTag(relay);
        assertEquals("relay", rTag.getCode());
        assertEquals(relay.getUri(), ((GenericTag) rTag).getAttributes().get(0).value());

        BaseTag cTag = NIP42.createChallengeTag("abc");
        assertEquals("challenge", cTag.getCode());
        assertEquals("abc", ((GenericTag) cTag).getAttributes().get(0).value());
    }
}
