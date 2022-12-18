
package com.tcheeric.nostr.event.marshaller;

import com.tcheeric.nostr.base.IElement;
import com.tcheeric.nostr.base.IEvent;
import com.tcheeric.nostr.base.INostrList;
import com.tcheeric.nostr.base.ITag;
import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.Relay;
import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import com.tcheeric.nostr.event.BaseMessage;
import com.tcheeric.nostr.event.list.BaseList;
import com.tcheeric.nostr.event.list.TagList;
import com.tcheeric.nostr.event.marshaller.impl.EventMarshaller;
import com.tcheeric.nostr.event.marshaller.impl.MessageMarshaller;
import com.tcheeric.nostr.event.marshaller.impl.TagListMarshaller;
import com.tcheeric.nostr.event.marshaller.impl.TagMarshaller;
import java.lang.reflect.Field;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;

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
            } else if (element instanceof BaseList) {
                return new BaseListMarhsaller((INostrList) element, relay, escape) {
                };
            } else {
                throw new NostrException("Invalid Element type");
            }
        }
    }
}
