package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NonNull;
import nostr.event.tag.ZapRequest;

import java.io.IOException;

public class ZapRequestSerializer extends JsonSerializer<ZapRequest> {
  private final RelaysTagSerializer relaysTagSerializer;

  public ZapRequestSerializer() {
    this.relaysTagSerializer = new RelaysTagSerializer();
  }

  @Override
  public void serialize(@NonNull ZapRequest zapRequest, @NonNull JsonGenerator jsonGenerator, @NonNull SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartObject();
    relaysTagSerializer.serialize(zapRequest.getRelaysTag(), jsonGenerator, serializerProvider);
    jsonGenerator.writeNumberField("amount", zapRequest.getAmount());
    jsonGenerator.writeStringField("lnurl", zapRequest.getLnUrl());
    jsonGenerator.writeEndObject();
  }
}
