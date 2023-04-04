package nostr.event.marshaller.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import lombok.extern.java.Log;
import nostr.base.GenericTagQuery;
import nostr.base.INostrList;
import nostr.base.Relay;
import nostr.event.list.GenericTagQueryList;
import nostr.event.marshaller.BaseListMarhsaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Log
public class GenericTagQueryListMarshaller extends BaseListMarhsaller {

    public GenericTagQueryListMarshaller(INostrList list, Relay relay) {
        this(list, relay, false);
    }

    public GenericTagQueryListMarshaller(INostrList list, Relay relay, boolean escape) {
        super(list, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        var gtql = (GenericTagQueryList) this.getElement();

        var result = new StringBuilder();
        final List<GenericTagQuery> list = gtql.getList();

        result.append(list.stream().map(q -> {
            try {
                return new GenericTagQueryMarshaller(q, getRelay(), isEscape()).marshall();
            } catch (NostrException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.joining(",")));

        return result.toString();
    }
}
