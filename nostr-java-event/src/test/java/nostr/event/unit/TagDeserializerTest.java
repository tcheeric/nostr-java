package nostr.event.unit;

import nostr.event.BaseTag;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.UrlTag;
import nostr.event.tag.GenericTag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;
import static org.junit.jupiter.api.Assertions.*;

class TagDeserializerTest {

    @Test
    void testAddressTagDeserialization() throws Exception {
        String pubKey = "bbbd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
        String json = "[\"a\",\"1:" + pubKey + ":test\",\"ws://localhost:8080\"]";
        BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
        assertInstanceOf(AddressTag.class, tag);
        AddressTag aTag = (AddressTag) tag;
        assertEquals(1, aTag.getKind());
        assertEquals(pubKey, aTag.getPublicKey().toString());
        assertEquals("test", aTag.getIdentifierTag().getUuid());
        assertEquals("ws://localhost:8080", aTag.getRelay().getUri());
    }

    @Test
    void testEventTagDeserialization() throws Exception {
        String id = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346";
        String json = "[\"e\",\"" + id + "\",\"wss://relay.example.com\",\"root\"]";
        BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
        assertInstanceOf(EventTag.class, tag);
        EventTag eTag = (EventTag) tag;
        assertEquals(id, eTag.getIdEvent());
        assertEquals("wss://relay.example.com", eTag.getRecommendedRelayUrl());
        assertEquals("root", eTag.getMarker().getValue());
    }

    @Test
    void testPriceTagDeserialization() throws Exception {
        String json = "[\"price\",\"10.99\",\"USD\"]";
        BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
        assertInstanceOf(PriceTag.class, tag);
        PriceTag pTag = (PriceTag) tag;
        assertEquals(new BigDecimal("10.99"), pTag.getNumber());
        assertEquals("USD", pTag.getCurrency());
    }

    @Test
    void testUrlTagDeserialization() throws Exception {
        String json = "[\"u\",\"http://example.com\"]";
        BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
        assertInstanceOf(UrlTag.class, tag);
        UrlTag uTag = (UrlTag) tag;
        assertEquals("http://example.com", uTag.getUrl());
    }

    @Test
    void testGenericFallback() throws Exception {
        String json = "[\"unknown\",\"value\"]";
        BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
        assertInstanceOf(GenericTag.class, tag);
        GenericTag gTag = (GenericTag) tag;
        assertEquals("unknown", gTag.getCode());
        assertEquals("value", gTag.getAttributes().get(0).value());
    }
}
