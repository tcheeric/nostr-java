package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.tag.ClassifiedListingTag;

import java.io.IOException;

public class ClassifiedListingTagSerializer extends JsonSerializer<ClassifiedListingTag> {
  private final PriceTagSerializer priceTagSerializer;

  public ClassifiedListingTagSerializer() {
    this.priceTagSerializer = new PriceTagSerializer();
  }

  @Override
  public void serialize(ClassifiedListingTag classifiedListingTag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartArray();

    jsonGenerator.writeString("title");
    jsonGenerator.writeString(classifiedListingTag.getTitle());
    jsonGenerator.writeEndArray();

    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("summary");
    jsonGenerator.writeString(classifiedListingTag.getSummary());
    jsonGenerator.writeEndArray();

    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("published_at");
    jsonGenerator.writeString(String.valueOf(classifiedListingTag.getPublishedAt()));
    jsonGenerator.writeEndArray();

    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("location");
    jsonGenerator.writeString(classifiedListingTag.getLocation());
    jsonGenerator.writeEndArray();

    priceTagSerializer.serialize(classifiedListingTag.getPriceTag(), jsonGenerator, serializerProvider);
  }
}
