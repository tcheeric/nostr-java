package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.tag.IdentifierTag;

import java.io.IOException;

public class IdentifierTagSerializer extends JsonSerializer<IdentifierTag> {

  @Override
  public void serialize(IdentifierTag value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("d");
    jsonGenerator.writeString(value.getUuid());
    jsonGenerator.writeEndArray();
  }

}
