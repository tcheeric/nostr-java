package nostr.event.marshaller.impl;

import java.util.logging.Level;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.IEvent;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.event.marshaller.BaseElementMarshaller;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
@SuppressWarnings("Lombok")
@Data
@EqualsAndHashCode(callSuper = false)
@Log
public class EventMarshaller extends BaseElementMarshaller {

    private boolean escape;

    public EventMarshaller(IEvent event, Relay relay) {
        this(event, relay, false);
    }

    public EventMarshaller(IEvent event, Relay relay, boolean escape) {
        super(event, relay);
        this.escape = escape;
    }

    @Override
    public String marshall() throws UnsupportedNIPException {
        Relay relay = getRelay();

        if (!nipEventSupport()) {
            throw new UnsupportedNIPException("NIP is not supported by relay: \"" + relay.getName() + "\"  - List of supported NIP(s): " + relay.printSupportedNips());
        }

        try {
			return toJson();
		} catch (NostrException e) {
			log.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
		}
    }

    private boolean nipEventSupport() {
        Relay relay = getRelay();

        if (relay == null) {
            return true;
        }

        IEvent event = (IEvent) getElement();
        return NipUtil.checkSupport(relay, event);
    }
}
