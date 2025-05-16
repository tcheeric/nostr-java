package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP52Event;
import nostr.event.impl.CalendarTimeBasedEvent.CalendarTimeBasedEventDeserializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@EqualsAndHashCode(callSuper = false)
@Event(name = "CalendarTimeBasedEvent", nip = 52)
@JsonDeserialize(using = CalendarTimeBasedEventDeserializer.class)
public class CalendarTimeBasedEvent extends NIP52Event {
  @Getter
  @JsonIgnore
  private CalendarContent calendarContent;

  public CalendarTimeBasedEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(sender, Kind.CALENDAR_TIME_BASED_EVENT, baseTags, content);
    this.calendarContent = calendarContent;
    mapCustomTags();
  }

  private void mapCustomTags() {
    addStandardTag(calendarContent.getIdentifierTag());
    addGenericTag("title", getNip(), calendarContent.getTitle());
    addGenericTag("start", getNip(), calendarContent.getStart());
    addGenericTag("end", getNip(), calendarContent.getEnd());
    addGenericTag("start_tzid", getNip(), calendarContent.getStartTzid());
    addGenericTag("end_tzid", getNip(), calendarContent.getEndTzid());
    addGenericTag("summary", getNip(), calendarContent.getSummary());
    addGenericTag("image", getNip(), calendarContent.getImage());
    addGenericTag("location", getNip(), calendarContent.getLocation());
    addStandardTag(calendarContent.getGeohashTag());
    addStandardTag(calendarContent.getParticipantPubKeys());
    addStringListTag("l", getNip(), calendarContent.getLabels());
    addStandardTag(calendarContent.getHashtagTags());
    addStandardTag(calendarContent.getReferenceTags());
  }

  public static class CalendarTimeBasedEventDeserializer extends StdDeserializer<CalendarTimeBasedEvent> {
    public CalendarTimeBasedEventDeserializer() {
      super(CalendarTimeBasedEvent.class);
    }

//    TODO: below methods needs comprehensive tags assignment completion
    @Override
    public CalendarTimeBasedEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode calendarTimeBasedEventNode = jsonParser.getCodec().readTree(jsonParser);
      ArrayNode tags = (ArrayNode) calendarTimeBasedEventNode.get("tags");

      List<BaseTag> baseTags = StreamSupport.stream(
              tags.spliterator(), false).toList().stream()
          .map(
              JsonNode::elements)
          .map(element ->
              MAPPER_AFTERBURNER.convertValue(element, BaseTag.class)).toList();

      List<GenericTag> genericTags = baseTags.stream()
          .filter(GenericTag.class::isInstance)
          .map(GenericTag.class::cast)
          .toList();

      IdentifierTag identifierTag = getBaseTagCastFromString(baseTags, IdentifierTag.class).getFirst();
      CalendarContent calendarContent = CalendarContent.builder(
              identifierTag,
              getTagValueFromString(genericTags, "title"),
              Long.valueOf(getTagValueFromString(genericTags, "start")))
          .build();

      calendarContent.setParticipantPubKeys(getBaseTagCastFromString(baseTags, PubKeyTag.class));

      Map<String, String> generalMap = new HashMap<>();
      calendarTimeBasedEventNode.fields().forEachRemaining(generalTag ->
          generalMap.put(
              generalTag.getKey(),
              generalTag.getValue().asText()));


      CalendarTimeBasedEvent calendarTimeBasedEvent = new CalendarTimeBasedEvent(
          new PublicKey(generalMap.get("pubkey")),
          baseTags,
          generalMap.get("content"),
          calendarContent
      );
      calendarTimeBasedEvent.setId(generalMap.get("id"));
      calendarTimeBasedEvent.setCreatedAt(Long.valueOf(generalMap.get("created_at")));
      calendarTimeBasedEvent.setSignature(Signature.fromString(generalMap.get("sig")));

      return calendarTimeBasedEvent;
    }

    private String getTagValueFromString(List<GenericTag> genericTags, String code) {
      return genericTags.stream()
          .filter(tag -> tag.getCode().equalsIgnoreCase(code))
          .findFirst().get().getAttributes().get(0).getValue().toString();
    }

    private <T extends BaseTag> List<T> getBaseTagCastFromString(List<BaseTag> baseTags, Class<T> type) {
      return baseTags.stream().filter(type::isInstance).map(type::cast).toList();
    }
  }
}
