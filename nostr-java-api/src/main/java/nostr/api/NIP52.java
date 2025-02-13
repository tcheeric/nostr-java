package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP52Impl.CalendarRsvpEventFactory;
import nostr.api.factory.impl.NIP52Impl.CalendarTimeBasedEventFactory;
import nostr.event.BaseTag;
import nostr.event.NIP52Event;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarRsvpContent;
import nostr.id.Identity;

import java.util.List;

public class NIP52<T extends NIP52Event> extends EventNostr<T> {
  public NIP52(@NonNull Identity sender) {
    setSender(sender);
  }

  public NIP52<T> createCalendarTimeBasedEvent(@NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    setEvent((T) new CalendarTimeBasedEventFactory(getSender(), baseTags, content, calendarContent).create());
    return this;
  }

  public NIP52<T> createCalendarRsvpEvent(@NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarRsvpContent calendarRsvpContent) {
    setEvent((T) new CalendarRsvpEventFactory(getSender(), baseTags, content, calendarRsvpContent).create());
    return this;
  }
}
