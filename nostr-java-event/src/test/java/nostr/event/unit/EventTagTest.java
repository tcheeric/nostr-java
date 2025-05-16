package nostr.event.unit;

import nostr.event.Marker;
import nostr.event.tag.EventTag;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTagTest {

    @Test
    void getSupportedFields() {
        String eventId = UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).substring(0, 64);
        String recommendedRelayUrl = "ws://localhost:5555";

        EventTag eventTag = new EventTag(eventId);
        eventTag.setMarker(Marker.REPLY);
        eventTag.setRecommendedRelayUrl(recommendedRelayUrl);

        List<Field> fields = eventTag.getSupportedFields();
        anyFieldNameMatch(fields, field -> field.getName().equals("idEvent"));
        anyFieldNameMatch(fields, field -> field.getName().equals("recommendedRelayUrl"));
        anyFieldNameMatch(fields, field -> field.getName().equals("marker"));

        anyFieldValueMatch(fields, eventTag, fieldValue -> fieldValue.equals(eventId));
        anyFieldValueMatch(fields, eventTag, fieldValue -> fieldValue.equalsIgnoreCase(Marker.REPLY.getValue()));
        anyFieldValueMatch(fields, eventTag, fieldValue -> fieldValue.equals(recommendedRelayUrl));

        assertFalse(fields.stream().anyMatch(field -> field.getName().equals("idEventXXX")));
        assertFalse(fields.stream().flatMap(field -> eventTag.getFieldValue(field).stream()).anyMatch(fieldValue -> fieldValue.equals(eventId + "x")));
    }

    private static void anyFieldNameMatch(List<Field> fields, Predicate<Field> predicate) {
        assertTrue(fields.stream().anyMatch(predicate));
    }

    private static void anyFieldValueMatch(List<Field> fields, EventTag eventTag, Predicate<String> predicate) {
        assertTrue(fields.stream().flatMap(field -> eventTag.getFieldValue(field).stream()).anyMatch(predicate));
    }
}
