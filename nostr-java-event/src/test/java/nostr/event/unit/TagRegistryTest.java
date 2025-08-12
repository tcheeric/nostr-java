package nostr.event.unit;

import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.TagRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests for dynamic tag registration.
 */
class TagRegistryTest {

    @Tag(code = "x")
    static class CustomTag extends BaseTag {
        @Key
        private String value;

        static CustomTag updateFields(GenericTag genericTag) {
            CustomTag tag = new CustomTag();
            tag.value = genericTag.getAttributes().get(0).value().toString();
            return tag;
        }
    }

    @Test
    void registerCustomTag() {
        TagRegistry.register("x", CustomTag::updateFields);
        BaseTag created = BaseTag.create("x", "hello");
        assertInstanceOf(CustomTag.class, created);
        assertEquals("hello", ((CustomTag) created).value);
    }
}

