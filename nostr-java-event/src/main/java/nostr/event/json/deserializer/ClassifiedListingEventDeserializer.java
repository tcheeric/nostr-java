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
import nostr.base.Kind;
import nostr.event.impl.ClassifiedListingEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class ClassifiedListingEventDeserializer extends StdDeserializer<ClassifiedListingEvent> {
    public ClassifiedListingEventDeserializer() {
        super(ClassifiedListingEvent.class);
    }

    @Override
    public ClassifiedListingEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode classifiedListingEventNode = jsonParser.getCodec().readTree(jsonParser);
        ArrayNode tags = (ArrayNode) classifiedListingEventNode.get("tags");

        List<BaseTag> baseTags = StreamSupport.stream(tags.spliterator(), false).toList()
                .stream()
                .map(JsonNode::elements)
                .map(element -> IEvent.MAPPER_AFTERBURNER.convertValue(element, BaseTag.class)).toList();
        Map<String, String> generalMap = new HashMap<>();
        classifiedListingEventNode.fields().forEachRemaining(generalTag ->
                generalMap.put(
                        generalTag.getKey(),
                        generalTag.getValue().asText()));


        ClassifiedListingEvent classifiedListingEvent = new ClassifiedListingEvent(
                new PublicKey(generalMap.get("pubkey")),
                Kind.valueOf(Integer.parseInt(generalMap.get("kind"))),
                baseTags,
                generalMap.get("content")
        );
        classifiedListingEvent.setId(generalMap.get("id"));
        classifiedListingEvent.setCreatedAt(Long.valueOf(generalMap.get("created_at")));
        classifiedListingEvent.setSignature(Signature.fromString(generalMap.get("sig")));

        return classifiedListingEvent;
    }
}
