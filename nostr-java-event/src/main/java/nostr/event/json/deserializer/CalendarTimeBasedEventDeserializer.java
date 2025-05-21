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
import nostr.event.impl.CalendarTimeBasedEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class CalendarTimeBasedEventDeserializer extends StdDeserializer<CalendarTimeBasedEvent> {
    public CalendarTimeBasedEventDeserializer() {
        super(CalendarTimeBasedEvent.class);
    }

    //    TODO: below methods needs comprehensive tags assignment completion
    @Override
    public CalendarTimeBasedEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode calendarTimeBasedEventNode = jsonParser.getCodec().readTree(jsonParser);
        ArrayNode tags = (ArrayNode) calendarTimeBasedEventNode.get("tags");

        List<BaseTag> baseTags = StreamSupport.stream(
                        tags.spliterator(), false).toList().stream()
                .map(
                        JsonNode::elements)
                .map(element ->
                        IEvent.MAPPER_AFTERBURNER.convertValue(element, BaseTag.class)).toList();


        Map<String, String> generalMap = new HashMap<>();
        calendarTimeBasedEventNode.fields().forEachRemaining(generalTag ->
                generalMap.put(
                        generalTag.getKey(),
                        generalTag.getValue().asText()));


        CalendarTimeBasedEvent calendarTimeBasedEvent = new CalendarTimeBasedEvent(
                new PublicKey(generalMap.get("pubkey")),
                baseTags,
                generalMap.get("content")
        );
        calendarTimeBasedEvent.setId(generalMap.get("id"));
        calendarTimeBasedEvent.setCreatedAt(Long.valueOf(generalMap.get("created_at")));
        calendarTimeBasedEvent.setSignature(Signature.fromString(generalMap.get("sig")));

        return calendarTimeBasedEvent;
    }
}
