package nostr.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.GenericEvent;

import java.util.List;

@Getter
public abstract class NIP52Event extends GenericEvent {
  @JsonIgnore
  private CalendarContent calendarContent;

  public NIP52Event(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(pubKey, kind, baseTags, content);
    this.calendarContent = calendarContent;
  }
}
