package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class CalendarEventDeserializer extends StdDeserializer<CalendarEvent> {
    public CalendarEventDeserializer() {
        super(CalendarEvent.class);
    }

    //    TODO: below methods needs comprehensive tags assignment completion
    @Override
    public CalendarEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode calendarTimeBasedEventNode = jsonParser.getCodec().readTree(jsonParser);
        ArrayNode tags = (ArrayNode) calendarTimeBasedEventNode.get("tags");

        List<BaseTag> baseTags = StreamSupport.stream(
                        tags.spliterator(), false).toList().stream()
                .map(
                        JsonNode::elements)
                .map(element ->
                        IEvent.MAPPER_BLACKBIRD.convertValue(element, BaseTag.class)).toList();


        Map<String, String> generalMap = new HashMap<>();
        calendarTimeBasedEventNode.fields().forEachRemaining(generalTag ->
                generalMap.put(
                        generalTag.getKey(),
                        generalTag.getValue().asText()));


        CalendarEvent calendarEvent = new CalendarEvent(
                new PublicKey(generalMap.get("pubkey")),
                baseTags,
                generalMap.get("content")
        );
        calendarEvent.setId(generalMap.get("id"));
        calendarEvent.setCreatedAt(Long.valueOf(generalMap.get("created_at")));
        calendarEvent.setSignature(Signature.fromString(generalMap.get("sig")));

        return calendarEvent;
    }
}
