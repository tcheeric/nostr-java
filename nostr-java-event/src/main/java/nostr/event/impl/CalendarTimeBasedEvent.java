package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nostr.base.annotation.Event;
import nostr.event.NIP52Event;

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

  protected CalendarTimeBasedEvent(CalendarTimeBasedEventBuilder<?, ?> b) {
    super(b);
    appendTags();
  }
}
