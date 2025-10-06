package nostr.event.json.deserializer;

import nostr.base.json.EventJsonMapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import nostr.base.json.EventJsonMapper;
import nostr.event.impl.CalendarDateBasedEvent;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrException;

public class CalendarDateBasedEventDeserializer extends StdDeserializer<CalendarDateBasedEvent> {
  public CalendarDateBasedEventDeserializer() {
    super(CalendarDateBasedEvent.class);
  }

  @Override
  public CalendarDateBasedEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    JsonNode calendarEventNode = jsonParser.getCodec().readTree(jsonParser);
    GenericEvent genericEvent =
        EventJsonMapper.mapper().treeToValue(calendarEventNode, GenericEvent.class);

    try {
      return GenericEvent.convert(genericEvent, CalendarDateBasedEvent.class);
    } catch (NostrException ex) {
      throw new IOException("Failed to convert generic event into CalendarDateBasedEvent", ex);
    }
  }
}
