package nostr.event.marshaller.impl;

import nostr.base.Relay;
import nostr.event.impl.Filters;

/**
 *
 * @author squirrel
 */
public class FiltersMarshaller extends EventMarshaller {

    public FiltersMarshaller(Filters filters, Relay relay) {
        this(filters, relay, false);
    }

    public FiltersMarshaller(Filters filters, Relay relay, boolean escape) {
        super(filters, relay, escape);
    }
}
