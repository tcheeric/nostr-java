package nostr.event.unit;

import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseTagTest {

    BaseTag genericTag = GenericTag.create("id", "value");

    @Test
    void testToString() {
        String result = "GenericTag(code=id, attributes=[ElementAttribute(name=param0, value=value)])";
        assertEquals(result, genericTag.toString());
    }

}
