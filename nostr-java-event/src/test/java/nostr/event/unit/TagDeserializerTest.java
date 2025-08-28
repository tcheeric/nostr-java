package nostr.event.unit;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import nostr.event.BaseTag;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.UrlTag;
import org.junit.jupiter.api.Test;

class TagDeserializerTest {

  @Test
  // Parses an AddressTag from JSON and verifies its fields.
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
  // Parses an EventTag with relay and marker and checks values.
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
  // Parses an EventTag without relay or marker and ensures marker is null.
  void testEventTagDeserializationWithoutMarker() throws Exception {
    String id = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346";
    String json = "[\"e\",\"" + id + "\"]";
    BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
    assertInstanceOf(EventTag.class, tag);
    EventTag eTag = (EventTag) tag;
    assertEquals(id, eTag.getIdEvent());
    assertNull(eTag.getMarker());
    assertNull(eTag.getRecommendedRelayUrl());
  }

  @Test
  // Parses a PriceTag from JSON and validates number and currency.
  void testPriceTagDeserialization() throws Exception {
    String json = "[\"price\",\"10.99\",\"USD\"]";
    BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
    assertInstanceOf(PriceTag.class, tag);
    PriceTag pTag = (PriceTag) tag;
    assertEquals(new BigDecimal("10.99"), pTag.getNumber());
    assertEquals("USD", pTag.getCurrency());
  }

  @Test
  // Parses a UrlTag from JSON and checks the URL value.
  void testUrlTagDeserialization() throws Exception {
    String json = "[\"u\",\"http://example.com\"]";
    BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
    assertInstanceOf(UrlTag.class, tag);
    UrlTag uTag = (UrlTag) tag;
    assertEquals("http://example.com", uTag.getUrl());
  }

  @Test
  // Falls back to GenericTag for unknown tag codes.
  void testGenericFallback() throws Exception {
    String json = "[\"unknown\",\"value\"]";
    BaseTag tag = MAPPER_BLACKBIRD.readValue(json, BaseTag.class);
    assertInstanceOf(GenericTag.class, tag);
    GenericTag gTag = (GenericTag) tag;
    assertEquals("unknown", gTag.getCode());
    assertEquals("value", gTag.getAttributes().get(0).value());
  }
}
