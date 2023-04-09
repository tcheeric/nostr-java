
package nostr.event.marshaller;

import nostr.base.INostrList;
import nostr.base.Relay;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
public abstract class BaseListMarhsaller extends BaseElementMarshaller {

    public BaseListMarhsaller(INostrList list, Relay relay) {
        super(list, relay, false);
    }

    public BaseListMarhsaller(INostrList tagList, Relay relay, boolean escape) {
        super(tagList, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        return toJson();

    }
}
