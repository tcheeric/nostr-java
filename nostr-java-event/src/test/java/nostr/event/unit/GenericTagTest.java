package nostr.event.unit;

import nostr.event.BaseTag;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EmojiTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.ExpirationTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import nostr.event.tag.NonceTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.event.tag.RelaysTag;
import nostr.event.tag.SubjectTag;
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
        assertEquals("test-value", ((GenericTag)tag).getAttributes().get(0).getValue());
    }
}