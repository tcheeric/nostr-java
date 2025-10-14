package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;

import java.io.IOException;

public class RelaysTagSerializer extends JsonSerializer<RelaysTag> {

  @Override
  public void serialize(
      @NonNull RelaysTag relaysTag,
      @NonNull JsonGenerator jsonGenerator,
      @NonNull SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("relays");
    for (var relay : relaysTag.getRelays()) {
      jsonGenerator.writeString(relay.getUri());
    }
    jsonGenerator.writeEndArray();
  }

  private static void writeString(JsonGenerator jsonGenerator, String json) throws IOException {
    jsonGenerator.writeString(json);
  }
}
