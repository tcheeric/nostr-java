package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.tag.PriceTag;

import java.io.IOException;

public class PriceTagSerializer extends JsonSerializer<PriceTag> {

  @Override
  public void serialize(PriceTag priceTag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("price");
    jsonGenerator.writeNumber(priceTag.getNumber());
    jsonGenerator.writeString(priceTag.getCurrency());
    jsonGenerator.writeString(priceTag.getFrequency());
    jsonGenerator.writeEndArray();
  }
}
