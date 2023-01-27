package nostr.event.marshaller.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.event.impl.Filters;
import nostr.base.list.FiltersList;
import nostr.event.marshaller.BaseListMarhsaller;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
@Log
public class FiltersListMarshaller extends BaseListMarhsaller {

    public FiltersListMarshaller(FiltersList list, Relay relay) {
        this(list, relay, false);
    }

    public FiltersListMarshaller(FiltersList list, Relay relay, boolean escape) {
        super(list, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {

        StringBuilder result = new StringBuilder();
        FiltersList filtersList = (FiltersList) getList();
        Relay relay = getRelay();

        final List<Filters> list = filtersList.getList();
        if (!list.isEmpty()) {
            result.append("[");

            result.append(list.stream().filter(f -> f != null).map(f -> {
                try {
                    return new FiltersMarshaller(f, relay, isEscape()).marshall();
                } catch (UnsupportedNIPException ex) {
                    log.log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }).collect(Collectors.joining(",")));

            result.append("]");
        }

        return result.toString();
    }    
}
