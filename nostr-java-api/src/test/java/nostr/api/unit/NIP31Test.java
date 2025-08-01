package nostr.api.unit;

import nostr.api.NIP31;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NIP31Test {

    @Test
    public void testCreateAltTag() {
        BaseTag tag = NIP31.createAltTag("desc");
        assertEquals("alt", tag.getCode());
        assertEquals("desc", ((GenericTag) tag).getAttributes().get(0).getValue());
    }
}
