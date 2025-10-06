package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import nostr.base.json.EventJsonMapper;
import nostr.event.impl.CalendarEvent;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrException;

public class CalendarEventDeserializer extends StdDeserializer<CalendarEvent> {
  public CalendarEventDeserializer() {
    super(CalendarEvent.class);
  }

  @Override
  public CalendarEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    JsonNode calendarEventNode = jsonParser.getCodec().readTree(jsonParser);
    GenericEvent genericEvent =
        EventJsonMapper.mapper().treeToValue(calendarEventNode, GenericEvent.class);

    try {
      return GenericEvent.convert(genericEvent, CalendarEvent.class);
    } catch (NostrException ex) {
      throw new IOException("Failed to convert generic event into CalendarEvent", ex);
    }
  }
}
