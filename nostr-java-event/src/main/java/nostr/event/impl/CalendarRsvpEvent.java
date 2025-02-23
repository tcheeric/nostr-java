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
import nostr.base.Signature;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP52Event;
import nostr.event.impl.CalendarRsvpEvent.CalendarRsvpEventDeserializer;
import nostr.event.impl.CalendarTimeBasedEvent.CalendarTimeBasedEventDeserializer;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@EqualsAndHashCode(callSuper = false)
@Event(name = "CalendarRsvpEvent", nip = 52)
@JsonDeserialize(using = CalendarRsvpEventDeserializer.class)
public class CalendarRsvpEvent extends NIP52Event {
  @Getter
  @JsonIgnore
  private CalendarRsvpContent calendarRsvpContent;

  public CalendarRsvpEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarRsvpContent calendarRsvpContent) {
    super(sender, Kind.CALENDAR_RSVP_EVENT, baseTags, content);
    this.calendarRsvpContent = calendarRsvpContent;
    mapCustomTags();
  }

  private void mapCustomTags() {
    addStandardTag(calendarRsvpContent.getIdentifierTag());
    addStandardTag(calendarRsvpContent.getAddressTag());
    addGenericTag("status", getNip(), calendarRsvpContent.getStatus());
    addStandardTag(calendarRsvpContent.getParticipantPubKeys());
  }

  public static class CalendarRsvpEventDeserializer extends StdDeserializer<CalendarRsvpEvent> {
    public CalendarRsvpEventDeserializer() {
      super(CalendarRsvpEvent.class);
    }

//    TODO: below methods needs comprehensive tags assignment completion
    @Override
    public CalendarRsvpEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode calendarTimeBasedEventNode = jsonParser.getCodec().readTree(jsonParser);
      ArrayNode tags = (ArrayNode) calendarTimeBasedEventNode.get("tags");

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

      IdentifierTag identifierTag = getBaseTagCastFromString(baseTags, IdentifierTag.class).getFirst();

      AddressTag addressTag = getBaseTagCastFromString(baseTags, AddressTag.class).getFirst();

      CalendarRsvpContent calendarRsvpContent = CalendarRsvpContent.builder(
              identifierTag,
              addressTag,
              getTagValueFromString(genericTags, "status"))
          .build();

      calendarRsvpContent.setParticipantPubKeys(getBaseTagCastFromString(baseTags, PubKeyTag.class));

      Map<String, String> generalMap = new HashMap<>();
      calendarTimeBasedEventNode.fields().forEachRemaining(generalTag ->
          generalMap.put(
              generalTag.getKey(),
              generalTag.getValue().asText()));


      CalendarRsvpEvent calendarTimeBasedEvent = new CalendarRsvpEvent(
          new PublicKey(generalMap.get("pubkey")),
          baseTags,
          generalMap.get("content"),
          calendarRsvpContent
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
