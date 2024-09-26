package nostr.event.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP52Event;
import nostr.event.tag.IdentifierTag;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@EqualsAndHashCode(callSuper = false)
@Event(name = "CalendarTimeBasedEvent", nip = 52)
@NoArgsConstructor
public class CalendarTimeBasedEvent extends NIP52Event {
  public CalendarTimeBasedEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(sender, Kind.CALENDAR_TIME_BASED_EVENT, baseTags, content, calendarContent);
  }

  public static class CalendarTimeBasedEventDeserializer extends StdDeserializer<CalendarTimeBasedEvent> {
    public CalendarTimeBasedEventDeserializer() {
      super(CalendarTimeBasedEvent.class);
    }

    @Override
    public CalendarTimeBasedEvent deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
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

      Map<String, String> generalMap = new HashMap<>();
      calendarTimeBasedEventNode.fields().forEachRemaining(generalTag ->
          generalMap.put(
              generalTag.getKey(),
              generalTag.getValue().asText()));

      CalendarContent calendarContent = CalendarContent.builder(
              new IdentifierTag(generalMap.get("d")),
              getTagValueFromString(genericTags, "title"),
              Long.valueOf(getTagValueFromString(genericTags, "start")))
          .build();

      CalendarTimeBasedEvent calendarTimeBasedEvent = new CalendarTimeBasedEvent(
          new PublicKey(generalMap.get("pubkey")),
          baseTags,
          generalMap.get("content"),
          calendarContent
      );
      calendarTimeBasedEvent.setId(generalMap.get("id"));
      calendarTimeBasedEvent.setCreatedAt(Long.valueOf(generalMap.get("created_at")));

      return calendarTimeBasedEvent;
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
