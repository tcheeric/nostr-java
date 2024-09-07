package nostr.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import nostr.base.PublicKey;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

import java.util.List;
import java.util.Optional;

@JsonSubTypes({
    @JsonSubTypes.Type(
        value = CalendarTimeBasedEvent.class),
})
@SuperBuilder
@JsonPOJOBuilder(withPrefix = "")

@EqualsAndHashCode(callSuper = false)
public abstract class NIP52Event extends GenericEvent {
  //  @JsonProperty
  private CalendarContent calendarContent;

  public NIP52Event(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(pubKey, kind.getValue(), baseTags, content);
    this.calendarContent = calendarContent;
    appendTags();
  }

  public void appendTags() {
    addStandardTag(calendarContent.getIdentifierTag());
    addGenericTag("id", calendarContent.getId());
    addGenericTag("title", calendarContent.getTitle());
    addGenericTag("start", calendarContent.getStart());
    addGenericTag("end", calendarContent.getEnd());
    addGenericTag("start_tzid", calendarContent.getStartTzid());
    addGenericTag("end_tzid", calendarContent.getEndTzid());
    addGenericTag("summary", calendarContent.getSummary());
    addGenericTag("image", calendarContent.getImage());
    addGenericTag("location", calendarContent.getLocation());
    addStandardTag(calendarContent.getGeohashTag());
    addStandardTag(calendarContent.getParticipantPubKeys());
    addStringListTag("l", calendarContent.getLabels());
    addStandardTag(calendarContent.getHashtagTags());
    addStandardTag(calendarContent.getReferenceTags());
  }

  // TODO: possibly refactor below into parent/hierarchy, take a look at Product/Classifieds first.
  private <T extends BaseTag> void addStandardTag(List<T> tag) {
    Optional.ofNullable(tag).ifPresent(tagList -> tagList.forEach(this::addStandardTag));
  }

  private void addStandardTag(BaseTag tag) {
    Optional.ofNullable(tag).ifPresent(this::addTag);
  }

  private void addGenericTag(String key, Object value) {
    Optional.ofNullable(value).ifPresent(s -> addTag(GenericTag.create(key, 52, s.toString())));
  }

  private void addStringListTag(String label, List<String> tag) {
    Optional.ofNullable(tag).ifPresent(tagList -> addGenericTag(label, tagList));
  }

  //  @JsonValue
//  public String json() throws JsonProcessingException {
//    JsonNode calendarNode = new ObjectMapper().valueToTree(calendarContent);
//    ArrayNode tags = new ObjectMapper().valueToTree(getTags());
//    calendarNode.fields().forEachRemaining(cal -> {
//      ArrayNode newArray = new ObjectMapper().createArrayNode();
//      newArray.add(cal.getKey());
//      newArray.add(cal.getValue());
//      tags.add(newArray);
//    });
//    return new ObjectMapper().writeValueAsString(tags);
//  }

//  @SneakyThrows
//  @Override
//  public String toString() {
//    ArrayNode root = new ObjectMapper().valueToTree(this);
//    JsonNode rootNode = new ObjectMapper().valueToTree(this);
//
//    JsonNode calendarNode = new ObjectMapper().valueToTree(calendarContent);
//    ArrayNode tags = new ObjectMapper().valueToTree(getTags());
//    calendarNode.fields().forEachRemaining(cal -> {
//      ArrayNode newArray = new ObjectMapper().createArrayNode();
//      newArray.add(cal.getKey());
//      newArray.add(cal.getValue());
//      tags.add(newArray);
//    });
//    return new ObjectMapper().writeValueAsString(tags);
//  }
}
