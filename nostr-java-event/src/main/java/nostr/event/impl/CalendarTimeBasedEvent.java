package nostr.event.impl;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP52Event;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "CalendarTimeBasedEvent", nip = 52)
@NoArgsConstructor
public class CalendarTimeBasedEvent extends NIP52Event {
  public CalendarTimeBasedEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(sender, Kind.CALENDAR_TIME_BASED_EVENT, baseTags, content, calendarContent);
  }
}
