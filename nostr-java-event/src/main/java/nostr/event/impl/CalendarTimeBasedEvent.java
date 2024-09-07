package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP52Event;

import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    defaultImpl = CalendarTimeBasedEvent.class)
@Jacksonized
@JsonTypeName("CalendarTimeBasedEvent")
@SuperBuilder
@JsonPOJOBuilder(withPrefix = "")

@EqualsAndHashCode(callSuper = false)
@Event(name = "CalendarTimeBasedEvent", nip = 52)
public class CalendarTimeBasedEvent extends NIP52Event {

  public CalendarTimeBasedEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(sender, Kind.CALENDAR_TIME_BASED_EVENT, baseTags, content, calendarContent);
  }
}
