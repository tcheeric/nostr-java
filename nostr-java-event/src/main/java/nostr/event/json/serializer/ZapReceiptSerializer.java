package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.event.impl.ZapReceiptEvent.ZapReceipt;

import java.io.IOException;

@NoArgsConstructor
public class ZapReceiptSerializer extends JsonSerializer<ZapReceipt> {

  @Override
  public void serialize(@NonNull ZapReceipt zapReceipt, @NonNull JsonGenerator jsonGenerator, @NonNull SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("bolt11", zapReceipt.getBolt11());
    jsonGenerator.writeStringField("descriptionSha256", zapReceipt.getDescriptionSha256());
    jsonGenerator.writeStringField("preimage", zapReceipt.getPreimage());
    jsonGenerator.writeEndObject();
  }
}
