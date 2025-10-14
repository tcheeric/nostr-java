package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import nostr.base.json.EventJsonMapper;
import nostr.event.impl.CalendarRsvpEvent;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrException;

import java.io.IOException;

public class CalendarRsvpEventDeserializer extends StdDeserializer<CalendarRsvpEvent> {
  public CalendarRsvpEventDeserializer() {
    super(CalendarRsvpEvent.class);
  }

  @Override
  public CalendarRsvpEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    JsonNode calendarEventNode = jsonParser.getCodec().readTree(jsonParser);
    GenericEvent genericEvent =
        EventJsonMapper.mapper().treeToValue(calendarEventNode, GenericEvent.class);

    try {
      return GenericEvent.convert(genericEvent, CalendarRsvpEvent.class);
    } catch (NostrException ex) {
      throw new IOException("Failed to convert generic event into CalendarRsvpEvent", ex);
    }
  }
}
