//package nostr.event.json.serializer;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonSerializer;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import lombok.NonNull;
//import nostr.event.impl.ClassifiedListing;
//
//import java.io.IOException;
//
//@EqualsAndHashCode(callSuper = true)
//@NoArgsConstructor
//@Data
//public class ClassifiedListingSerializer extends JsonSerializer<ClassifiedListing> {
//
//  @Override
//  public void serialize(@NonNull ClassifiedListing classifiedListing, @NonNull JsonGenerator jsonGenerator, @NonNull SerializerProvider serializerProvider) throws IOException {
//    jsonGenerator.writeStartObject();
//    jsonGenerator.writeStringField("title", classifiedListing.getTitle());
//    jsonGenerator.writeStringField("summary", classifiedListing.getSummary());
//    jsonGenerator.writeStringField("summaryXXX", "XXXXXXXXXXXXXXXXXXXX");
//    jsonGenerator.writeNumberField("published_at", classifiedListing.getPublishedAt());
//    jsonGenerator.writeStringField("location", classifiedListing.getLocation());
//    jsonGenerator.writeEndObject();
//  }
//}
