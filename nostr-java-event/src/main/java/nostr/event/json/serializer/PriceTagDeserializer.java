package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import nostr.event.tag.PriceTag;

import java.io.IOException;

public class PriceTagDeserializer extends JsonDeserializer<PriceTag> {
  @Override
  public PriceTag deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
    return null;
  }
}
