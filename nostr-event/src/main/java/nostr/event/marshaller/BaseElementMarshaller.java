package nostr.event.marshaller;

import nostr.base.IMarshaller;
import nostr.base.IElement;
import nostr.base.IEvent;
import nostr.base.INostrList;
import nostr.base.ITag;
import nostr.base.Relay;
import nostr.event.BaseMessage;
import nostr.event.list.TagList;
import nostr.event.marshaller.impl.EventMarshaller;
import nostr.event.marshaller.impl.MessageMarshaller;
import nostr.event.marshaller.impl.TagListMarshaller;
import nostr.event.marshaller.impl.TagMarshaller;
import java.lang.reflect.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.NipUtil;
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
public abstract class BaseElementMarshaller implements IMarshaller {

    private final IElement element;
    private final Relay relay;
    private boolean escape;

    public BaseElementMarshaller(IElement element, Relay relay) {
        this(element, relay, false);
    }

    protected boolean nipFieldSupport(Field field) {
        
        if (relay == null) {
            return true;
        }

        return NipUtil.checkSupport(relay, field);
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
