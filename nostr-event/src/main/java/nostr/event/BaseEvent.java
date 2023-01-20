
package nostr.event;

import nostr.base.IEvent;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author squirrel
 */
@Log
public abstract class BaseEvent implements IEvent {

    public static class ProxyEvent extends GenericEvent {

        public ProxyEvent(String id)  {
            super(new PublicKey(new byte[]{}), Kind.UNDEFINED);
            setId(id);
        }

    }
}
