
package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import nostr.event.Kind;

import java.io.IOException;

public class KindDeserializer extends JsonDeserializer<Kind> {

  @Override
  public Kind deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    Integer node = jsonParser.readValueAs(Integer.class);
    return Kind.valueOf(node);
  }
}
