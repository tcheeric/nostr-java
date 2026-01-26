package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.json.EventJsonMapper;
import nostr.event.BaseTag;
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
  public ClassifiedListingEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    JsonNode classifiedListingEventNode = jsonParser.getCodec().readTree(jsonParser);
    ArrayNode tags = (ArrayNode) classifiedListingEventNode.get("tags");

    List<BaseTag> baseTags =
        StreamSupport.stream(tags.spliterator(), false).toList().stream()
            .map(JsonNode::elements)
            .map(element -> EventJsonMapper.mapper().convertValue(element, BaseTag.class))
            .toList();
    Map<String, String> generalMap = new HashMap<>();
    var fieldNames = classifiedListingEventNode.fieldNames();
    while (fieldNames.hasNext()) {
      String key = fieldNames.next();
      generalMap.put(key, classifiedListingEventNode.get(key).asText());
    }

    String kindValue = generalMap.get("kind");
    String createdAtValue = generalMap.get("created_at");

    try {
      if (kindValue == null) {
        throw new IOException("Missing required field 'kind' in ClassifiedListingEvent");
      }
      if (createdAtValue == null) {
        throw new IOException("Missing required field 'created_at' in ClassifiedListingEvent");
      }

      int kindInt = Integer.parseInt(kindValue);
      long createdAt = Long.parseLong(createdAtValue);

      ClassifiedListingEvent classifiedListingEvent =
          new ClassifiedListingEvent(
              new PublicKey(generalMap.get("pubkey")),
              Kind.valueOfStrict(kindInt),
              baseTags,
              generalMap.get("content"));
      classifiedListingEvent.setId(generalMap.get("id"));
      classifiedListingEvent.setCreatedAt(createdAt);
      classifiedListingEvent.setSignature(Signature.fromString(generalMap.get("sig")));

      return classifiedListingEvent;
    } catch (NumberFormatException ex) {
      throw new IOException(
          String.format(
              "Failed to parse numeric field in ClassifiedListingEvent - kind='%s', created_at='%s': %s",
              kindValue, createdAtValue, ex.getMessage()),
          ex);
    }
  }
}
