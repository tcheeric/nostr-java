package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.impl.ClassifiedListingEvent.ClassifiedListing;

import java.io.IOException;

public class ClassifiedListingSerializer extends JsonSerializer<ClassifiedListing> {
  private final PriceTagSerializer priceTagSerializer;

  public ClassifiedListingSerializer() {
    this.priceTagSerializer = new PriceTagSerializer();
  }

  @Override
  public void serialize(ClassifiedListing classifiedListing, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartArray();

    jsonGenerator.writeString("tags");
    jsonGenerator.writeStartArray();

    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("title");
    jsonGenerator.writeString(classifiedListing.getTitle());
    jsonGenerator.writeEndArray();

    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("summary");
    jsonGenerator.writeString(classifiedListing.getSummary());
    jsonGenerator.writeEndArray();

    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("published_at");
    jsonGenerator.writeString(String.valueOf(classifiedListing.getPublishedAt()));
    jsonGenerator.writeEndArray();

    jsonGenerator.writeStartArray();
    jsonGenerator.writeString("location");
    jsonGenerator.writeString(classifiedListing.getLocation());
    jsonGenerator.writeEndArray();

    priceTagSerializer.serialize(classifiedListing.getPriceTag(), jsonGenerator, serializerProvider);

    jsonGenerator.writeEndArray();
    jsonGenerator.writeEndArray();
  }
}
