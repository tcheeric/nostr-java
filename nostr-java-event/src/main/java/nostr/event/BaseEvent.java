
package nostr.event;

import nostr.base.IEvent;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author squirrel
 */
public abstract class BaseEvent implements IEvent {

    public static class ProxyEvent extends GenericEvent {

        public ProxyEvent(String id)  {
            setId(id);
        }

    }
}
