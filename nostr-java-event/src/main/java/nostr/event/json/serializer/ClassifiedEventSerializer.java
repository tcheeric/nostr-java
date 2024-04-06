package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.impl.ClassifiedListingEvent.ClassifiedListing;

import java.io.IOException;

public class ClassifiedEventSerializer extends JsonSerializer<ClassifiedListing> {
  PriceTagSerializer priceTagSerializer;

  public ClassifiedEventSerializer() {
    this.priceTagSerializer = new PriceTagSerializer();
  }

  @Override
  public void serialize(ClassifiedListing classifiedListing, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("id", classifiedListing.getId());
    jsonGenerator.writeStringField("title", classifiedListing.getTitle());
    jsonGenerator.writeStringField("summary", classifiedListing.getSummary());
    jsonGenerator.writeNumberField("publishedAt", classifiedListing.getPublishedAt());
    jsonGenerator.writeStringField("location", classifiedListing.getLocation());
    jsonGenerator.writeFieldName("price");
    priceTagSerializer.serialize(classifiedListing.getPriceTags(), jsonGenerator, serializerProvider);
    jsonGenerator.writeEndObject();
  }
}
