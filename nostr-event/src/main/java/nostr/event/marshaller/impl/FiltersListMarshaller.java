package nostr.event.marshaller.impl;

import java.util.List;
import nostr.base.Relay;
import nostr.event.impl.Filters;
import nostr.event.list.FiltersList;
import nostr.event.marshaller.BaseListMarhsaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
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

        @SuppressWarnings("rawtypes")
        final List list = filtersList.getList();
        if (!list.isEmpty()) {
            result.append("[");
            int size = filtersList.size(), i = 0;

            for (Object t : list) {
                if (t == null) {
                    continue;
                }
                Filters filters = (Filters) t;
                result.append(new FiltersMarshaller(filters, relay, isEscape()).marshall());
                if (++i < size) {
                    result.append(",");
                }
            }
            result.append("]");
        }

        return result.toString();
    }
}
