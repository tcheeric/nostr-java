//package nostr.event.json.serializer;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonSerializer;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.NonNull;
//import nostr.event.tag.PriceTag;
//
//import java.io.IOException;
//
//@NoArgsConstructor
//@Data
//public class PriceTagSerializer extends JsonSerializer<PriceTag> {
//
//  @Override
//  public void serialize(@NonNull PriceTag priceTag, @NonNull JsonGenerator jsonGenerator, @NonNull SerializerProvider serializerProvider) throws IOException {
//    jsonGenerator.writeStartArray();
//    jsonGenerator.writeString("price");
//    jsonGenerator.writeNumber(priceTag.getNumber());
//    jsonGenerator.writeString(priceTag.getCurrency());
//    jsonGenerator.writeString(priceTag.getFrequency());
//    jsonGenerator.writeEndArray();
//  }
//}
