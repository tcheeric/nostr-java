package nostr.event.marshaller;

import nostr.base.IElement;
import nostr.base.IEvent;
import nostr.base.INostrList;
import nostr.base.ITag;
import nostr.base.Relay;
import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import nostr.event.BaseMessage;
import nostr.event.list.TagList;
import nostr.event.marshaller.impl.EventMarshaller;
import nostr.event.marshaller.impl.MessageMarshaller;
import nostr.event.marshaller.impl.TagListMarshaller;
import nostr.event.marshaller.impl.TagMarshaller;
import java.lang.reflect.Field;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.event.impl.Filters;
import nostr.event.marshaller.impl.FiltersMarshaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
@Log
public abstract class BaseMarshaller implements IMarshaller {

    private final IElement element;
    private final Relay relay;
    private boolean escape;

    public BaseMarshaller(IElement element, Relay relay) {
        this.element = element;
        this.relay = relay;
        this.escape = false;
    }

    protected boolean nipFieldSupport(Field field) {

        if (field.getAnnotation(Key.class) == null) {
            log.log(Level.FINE, "@Key annotation not found, skipping field {0}", field.getName());
            return false;
        }

        if (relay == null) {
            return true;
        }

        var snips = relay.getSupportedNips();
        var n = field.getAnnotation(NIPSupport.class);
        var nip = n != null ? n.value() : 1;

        return snips.contains(nip);
    }

    @Builder
    @AllArgsConstructor
    @Data
    public static class Factory {

        private final IElement element;

        public IMarshaller create(Relay relay, boolean escape) throws NostrException {
            if (element instanceof IEvent) {
                return new EventMarshaller((IEvent) element, relay, escape);
            } else if (element instanceof ITag) {
                return new TagMarshaller((ITag) element, relay, escape);
            } else if (element instanceof BaseMessage) {
                return new MessageMarshaller((BaseMessage) element, relay, escape);
            } else if (element instanceof TagList) {
                return new TagListMarshaller((TagList) element, relay, escape);
            } else if (element instanceof INostrList) {
                return new BaseListMarhsaller((INostrList) element, relay, escape) {
                };
            } else if (element instanceof Filters) {
                return new FiltersMarshaller((Filters) element, relay, escape);
            } else {
                throw new NostrException("Invalid Element type");
            }
        }
    }
}
