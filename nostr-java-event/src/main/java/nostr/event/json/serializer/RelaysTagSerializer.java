package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.SneakyThrows;
import nostr.event.tag.RelaysTag;

import java.io.IOException;

public class RelaysTagSerializer extends JsonSerializer<RelaysTag> {

  @Override
  public void serialize(RelaysTag relaysTag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartArray();
    relaysTag.getRelayUris().forEach(json -> writeString(jsonGenerator, json));
    jsonGenerator.writeEndArray();
  }

  @SneakyThrows
  private void writeString(JsonGenerator jsonGenerator, String json) {
    jsonGenerator.writeString(json);
  }
}
