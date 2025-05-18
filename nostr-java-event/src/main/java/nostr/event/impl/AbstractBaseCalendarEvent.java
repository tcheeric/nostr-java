package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.JsonContent;
import nostr.event.NIP52Event;

import java.util.List;

@NoArgsConstructor
public abstract class AbstractBaseCalendarEvent<T extends JsonContent> extends NIP52Event {

    public AbstractBaseCalendarEvent(@NonNull PublicKey sender, @NonNull Kind kind, @NonNull List<BaseTag> baseTags, @NonNull String content) {
        super(sender, kind, baseTags, content);
    }

    protected abstract T getCalendarContent();

}
