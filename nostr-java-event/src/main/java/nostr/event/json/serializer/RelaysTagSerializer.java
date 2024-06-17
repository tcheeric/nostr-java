package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.tag.RelaysTag;

import java.io.IOException;

public class RelaysTagSerializer extends JsonSerializer<RelaysTag> {

  @Override
  public void serialize(@NonNull RelaysTag relaysTag, @NonNull JsonGenerator jsonGenerator, @NonNull SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("relays");
    relaysTag.getRelays().forEach(json -> writeString(jsonGenerator, json.getUri()));
    jsonGenerator.writeEndArray();
  }

  @SneakyThrows
  private static void writeString(JsonGenerator jsonGenerator, String json) {
    jsonGenerator.writeString(json);
  }
}
