package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.impl.ZapRequestEvent.ZapRequest;

import java.io.IOException;

public class ZapRequestSerializer extends JsonSerializer<ZapRequest> {
  RelaysTagSerializer relaysTagSerializer;

  public ZapRequestSerializer() {
    this.relaysTagSerializer = new RelaysTagSerializer();
  }

  @Override
  public void serialize(ZapRequest zapRequest, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartObject();
    relaysTagSerializer.serialize(zapRequest.getRelaysTag(), jsonGenerator, serializerProvider);
    jsonGenerator.writeNumberField("amount", zapRequest.getAmount());
    jsonGenerator.writeStringField("lnurl", zapRequest.getLnUrl());
    jsonGenerator.writeStringField("p", zapRequest.getRecipientPubKey());
    jsonGenerator.writeEndObject();
  }
}
