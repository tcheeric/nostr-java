package nostr.event.marshaller.impl;

import nostr.base.INostrList;
import nostr.base.NostrException;
import nostr.base.Relay;
import nostr.event.list.GenericTagQueryList;
import nostr.event.marshaller.BaseListMarhsaller;

/**
 *
 * @author squirrel
 */
public class GenericTagQueryListMarshaller extends BaseListMarhsaller {

    public GenericTagQueryListMarshaller(INostrList list, Relay relay) {
        this(list, relay, false);
    }

    public GenericTagQueryListMarshaller(INostrList list, Relay relay, boolean escape) {
        super(list, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        var gtql = (GenericTagQueryList) this.getList();
        int i = 0;
        var result = new StringBuilder();
        for (var q : gtql.getList()) {
            result.append(new GenericTagQueryMarshaller(q, getRelay(), isEscape()).marshall());

            if (++i < gtql.size()) {
                result.append(",");
            }
        }
        return result.toString();
    }
}
