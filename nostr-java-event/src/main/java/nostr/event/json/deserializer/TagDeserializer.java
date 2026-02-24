package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.BaseTag;
import nostr.event.json.codec.GenericTagDecoder;

import java.io.IOException;

public class TagDeserializer<T extends BaseTag> extends JsonDeserializer<T> {

  @Override
  public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    if (!node.isArray() || node.isEmpty() || node.get(0) == null) {
      throw new IOException("Malformed JSON: Expected a non-empty array.");
    }

    BaseTag tag = new GenericTagDecoder<>().decode(node.toString());

    @SuppressWarnings("unchecked")
    T typed = (T) tag;
    return typed;
  }
}
