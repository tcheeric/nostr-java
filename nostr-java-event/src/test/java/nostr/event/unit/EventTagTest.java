package nostr.event.unit;

import nostr.base.Marker;
import nostr.event.BaseTag;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.tag.EventTag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static nostr.base.json.EventJsonMapper.mapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTagTest {

  @Test
  // Verifies that getSupportedFields returns expected fields and values.
  void getSupportedFields() {
    String eventId =
        UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).substring(0, 64);
    String recommendedRelayUrl = "ws://localhost:5555";

    EventTag eventTag = new EventTag(eventId);
    eventTag.setMarker(Marker.REPLY);
    eventTag.setRecommendedRelayUrl(recommendedRelayUrl);

    List<Field> fields = eventTag.getSupportedFields();
    anyFieldNameMatch(fields, field -> field.getName().equals("idEvent"));
    anyFieldNameMatch(fields, field -> field.getName().equals("recommendedRelayUrl"));
    anyFieldNameMatch(fields, field -> field.getName().equals("marker"));

    anyFieldValueMatch(fields, eventTag, fieldValue -> fieldValue.equals(eventId));
    anyFieldValueMatch(
        fields, eventTag, fieldValue -> fieldValue.equalsIgnoreCase(Marker.REPLY.getValue()));
    anyFieldValueMatch(fields, eventTag, fieldValue -> fieldValue.equals(recommendedRelayUrl));

    assertFalse(fields.stream().anyMatch(field -> field.getName().equals("idEventXXX")));
    assertFalse(
        fields.stream()
            .flatMap(field -> eventTag.getFieldValue(field).stream())
            .anyMatch(fieldValue -> fieldValue.equals(eventId + "x")));
  }

  @Test
  // Ensures that a newly created EventTag has a null marker and serializes without it.
  void serializeWithoutMarker() throws Exception {
    String eventId = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346";
    EventTag eventTag = new EventTag(eventId);

    assertNull(eventTag.getMarker());

    String json = new BaseTagEncoder(eventTag).encode();
    assertEquals("[\"e\",\"" + eventId + "\"]", json);

    BaseTag decoded = mapper().readValue(json, BaseTag.class);
    assertInstanceOf(EventTag.class, decoded);
    assertNull(((EventTag) decoded).getMarker());
  }

  @Test
  // Checks that an explicit marker is serialized and restored on deserialization.
  void serializeWithMarker() throws Exception {
    String eventId = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346";
    EventTag eventTag =
        EventTag.builder()
            .idEvent(eventId)
            .recommendedRelayUrl("wss://relay.example.com")
            .marker(Marker.ROOT)
            .build();

    String json = new BaseTagEncoder(eventTag).encode();
    assertEquals("[\"e\",\"" + eventId + "\",\"wss://relay.example.com\",\"ROOT\"]", json);

    BaseTag decoded = mapper().readValue(json, BaseTag.class);
    assertInstanceOf(EventTag.class, decoded);
    EventTag decodedEventTag = (EventTag) decoded;
    assertEquals(Marker.ROOT, decodedEventTag.getMarker());
    assertEquals("wss://relay.example.com", decodedEventTag.getRecommendedRelayUrl());
  }

  private static void anyFieldNameMatch(List<Field> fields, Predicate<Field> predicate) {
    assertTrue(fields.stream().anyMatch(predicate));
  }

  private static void anyFieldValueMatch(
      List<Field> fields, EventTag eventTag, Predicate<String> predicate) {
    assertTrue(
        fields.stream()
            .flatMap(field -> eventTag.getFieldValue(field).stream())
            .anyMatch(predicate));
  }
}
