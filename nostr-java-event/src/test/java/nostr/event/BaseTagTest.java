package nostr.event;

import nostr.event.impl.GenericTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseTagTest {

    BaseTag genericTag = GenericTag.create("id", 1, "value");

    @Test
    void getNip() {
        assertEquals(1, genericTag.getNip());
    }

    @Test
    void testHashCode() {
        assertEquals(112174237, genericTag.hashCode());
    }

    @Test
    void testToString() {
        String result = "GenericTag(code=id, nip=1, attributes=[ElementAttribute(name=param0, value=value, nip=null)])";
        assertEquals(result, genericTag.toString());
    }

}
