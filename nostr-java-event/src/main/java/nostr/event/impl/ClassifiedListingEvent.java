package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
@JsonDeserialize(using = ClassifiedListingEventDeserializer.class)
public class ClassifiedListingEvent extends NIP99Event {
  @Getter
  @JsonIgnore
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
      JsonNode classifiedListingEventNode = jsonParser.getCodec().readTree(jsonParser);
      ArrayNode tags = (ArrayNode) classifiedListingEventNode.get("tags");

      List<BaseTag> baseTags = StreamSupport.stream(
              tags.spliterator(), false).toList().stream()
          .map(
              JsonNode::elements)
          .map(element ->
              new ObjectMapper().convertValue(element, BaseTag.class)).toList();

      List<GenericTag> genericTags = baseTags.stream()
          .filter(GenericTag.class::isInstance)
          .map(GenericTag.class::cast)
          .toList();

      PriceTag priceTag = getBaseTagCastFromString(baseTags, PriceTag.class);
      ClassifiedListing classifiedListing = ClassifiedListing.builder(
              getTagValueFromString(genericTags, "title"),
              getTagValueFromString(genericTags, "summary"),
              priceTag)
          .build();

      Map<String, String> generalMap = new HashMap<>();
      classifiedListingEventNode.fields().forEachRemaining(generalTag ->
          generalMap.put(
              generalTag.getKey(),
              generalTag.getValue().asText()));

      ClassifiedListingEvent classifiedListingEvent = new ClassifiedListingEvent(
          new PublicKey(generalMap.get("pubkey")),
          Kind.valueOf(Integer.parseInt(generalMap.get("kind"))),
          // TODO: baseTags below need already-added items from classifiedListing to be removed
          baseTags,
          generalMap.get("content"),
          classifiedListing
      );
      classifiedListingEvent.setId(generalMap.get("id"));
      classifiedListingEvent.setCreatedAt(Long.valueOf(generalMap.get("created_at")));

      return classifiedListingEvent;
    }

    private String getTagValueFromString(List<GenericTag> genericTags, String code) {
      return genericTags.stream()
          .filter(tag -> tag.getCode().equalsIgnoreCase(code))
          .findFirst().get().getAttributes().get(0).getValue().toString();
    }

    private <T extends BaseTag> T getBaseTagCastFromString(List<BaseTag> baseTags, Class<T> type) {
      return baseTags.stream().filter(type::isInstance).map(type::cast).findFirst().get();
    }
  }
}
