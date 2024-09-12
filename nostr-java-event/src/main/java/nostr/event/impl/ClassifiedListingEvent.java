package nostr.event.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;
import nostr.event.impl.ClassifiedListingEvent.ClassifiedListingEventDeserializer;
import nostr.event.tag.PriceTag;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
@NoArgsConstructor
@JsonDeserialize(using = ClassifiedListingEventDeserializer.class)
public class ClassifiedListingEvent extends NIP99Event {
  private ClassifiedListing classifiedListing;

  public ClassifiedListingEvent(@NonNull PublicKey sender, @NonNull Kind kind, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    super(sender, kind, baseTags, content);
    this.classifiedListing = classifiedListing;
    mapCustomTags();
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, ClassifiedListing.builder(title, summary, priceTag).build());
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull String title, @NonNull String summary, @NonNull BigDecimal number, @NonNull String currency, @NonNull String frequency) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, ClassifiedListing.builder(title, summary, new PriceTag(number, currency, frequency)).build());
  }

  @Override
  protected void validate() {
    var n = getKind();
    if (30402 <= n && n <= 30403)
      return;

    throw new AssertionError(String.format("Invalid kind value [%s]. Classified Listing must be either 30402 or 30403", n), null);
  }

  private void mapCustomTags() {
    addGenericTag("title", getNip(), classifiedListing.getTitle());
    addGenericTag("summary", getNip(), classifiedListing.getSummary());
    addGenericTag("published_at", getNip(), classifiedListing.getPublishedAt());
    addGenericTag("location", getNip(), classifiedListing.getLocation());
    addStandardTag(classifiedListing.getPriceTag());
  }

  public static class ClassifiedListingEventDeserializer extends StdDeserializer<ClassifiedListingEvent> {
    public ClassifiedListingEventDeserializer() {
      super(ClassifiedListingEvent.class);
    }

    @Override
    public ClassifiedListingEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode calendarNode = jsonParser.getCodec().readTree(jsonParser);
      ArrayNode tags = (ArrayNode) calendarNode.get("tags");
      calendarNode.fields().forEachRemaining(cal -> {
        ArrayNode newArray = new ObjectMapper().createArrayNode();
        newArray.add(cal.getKey());
        newArray.add(cal.getValue());
        tags.add(newArray);
      });
      return null;
    }
  }
}
