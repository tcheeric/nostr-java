package nostr.test.event;

import nostr.event.Marker;
import nostr.event.tag.EventTag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTagTest {
  @Test
  void getSupportedFields() {
    String eventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
    String recommendedRelayUrl = "ws://localhost:5555";

    EventTag eventTag = new EventTag(eventId);
    eventTag.setMarker(Marker.REPLY);
    eventTag.setRecommendedRelayUrl(recommendedRelayUrl);

    assertDoesNotThrow(() -> {
      List<Field> fields = eventTag.getSupportedFields();
      assertTrue(fields.stream().anyMatch(field -> field.getName().equals("idEvent")));
      assertTrue(fields.stream().anyMatch(field -> field.getName().equals("recommendedRelayUrl")));
      assertTrue(fields.stream().anyMatch(field -> field.getName().equals("marker")));

      assertTrue(fields.stream().map(field -> getFieldValue(field, eventTag)).anyMatch(fieldValue -> fieldValue.equals(eventId)));
      assertTrue(fields.stream().map(field -> getFieldValue(field, eventTag)).anyMatch(fieldValue -> fieldValue.equalsIgnoreCase(Marker.REPLY.getValue())));
      assertTrue(fields.stream().map(field -> getFieldValue(field, eventTag)).anyMatch(fieldValue -> fieldValue.equals(recommendedRelayUrl)));

      assertFalse(fields.stream().anyMatch(field -> field.getName().equals("idEventXXX")));
      assertFalse(fields.stream().map(field -> getFieldValue(field, eventTag)).anyMatch(fieldValue -> fieldValue.equals(eventId + "x")));
    });
  }

  private String getFieldValue(Field field, EventTag eventTag) {
    final String[] returnVal = new String[1];
    assertDoesNotThrow(() -> {
      returnVal[0] = eventTag.getFieldValue(field);
    });
    return returnVal[0];
  }
}