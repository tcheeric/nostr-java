package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.id.Identity;

import java.util.List;

public class NIP52Impl {
  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class CalendarTimeBasedEventFactory extends EventFactory<CalendarTimeBasedEvent> {
    private final CalendarContent calendarContent;

    public CalendarTimeBasedEventFactory(@NonNull Identity sender, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
      super(sender, baseTags, content);
      this.calendarContent = calendarContent;
    }

    @Override
    public CalendarTimeBasedEvent create() {
      return new CalendarTimeBasedEvent(getSender(), getTags(), getContent(), calendarContent);
    }
  }
}
