
package nostr.event.marshaller;

import nostr.base.IKey;
import nostr.base.INostrList;
import nostr.base.Relay;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
@Log
public abstract class BaseListMarhsaller implements IMarshaller {

    private final INostrList list;
    private final Relay relay;
    private boolean escape;

    public BaseListMarhsaller(INostrList list, Relay relay) {
        this(list, relay, false);
    }

    @Override
    public String marshall() throws NostrException {
        var result = new StringBuffer("[");

        if (list != null && !list.getList().isEmpty()) {
            int size = list.size(), i = 0;

            for (Object elt : list.getList()) {
                if (elt == null) {
                    continue;
                }
                if (elt instanceof GenericEvent) {
                    if (!escape) {
                        result.append("\"");
                    } else {
                        result.append("\\\"");
                    }
                    result.append(((GenericEvent) elt).getId());
                    if (!escape) {
                        result.append("\"");
                    } else {
                        result.append("\\\"");
                    }
                } else if (elt instanceof IKey) {
                    if (!escape) {
                        result.append("\"");
                    } else {
                        result.append("\\\"");
                    }
                    result.append(elt.toString());
                    if (!escape) {
                        result.append("\"");
                    } else {
                        result.append("\\\"");
                    }
                } else if (elt instanceof Kind) {
                    result.append(((Kind) elt).getValue());
                } else {
                    result.append(elt);
                }
                if (++i < size) {
                    result.append(",");
                }
            }
        }
        result.append("]");

        return result.toString();

    }
}
