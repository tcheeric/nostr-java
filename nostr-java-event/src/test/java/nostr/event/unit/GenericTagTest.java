package nostr.event.unit;

import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class GenericTagTest {

    @Test
    public void testCreateGenericFallback() {
        String code = "unknown";
        List<String> params = List.of("test-value");
        BaseTag tag = BaseTag.create(code, params);

        assertInstanceOf(GenericTag.class, tag);
        assertEquals(code, tag.getCode());
        assertEquals("test-value", ((GenericTag)tag).getAttributes().get(0).value());
    }
}