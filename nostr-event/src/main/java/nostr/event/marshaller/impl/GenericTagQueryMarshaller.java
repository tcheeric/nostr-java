package nostr.event.marshaller.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.Relay;
import nostr.event.impl.GenericTagQuery;
import nostr.event.marshaller.IMarshaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
@Builder
public class GenericTagQueryMarshaller implements IMarshaller {

    private final GenericTagQuery genericTagQuery;
    private final Relay relay;
    private final boolean escape;

    public GenericTagQueryMarshaller(GenericTagQuery genericTagQuery, Relay relay) {
        this(genericTagQuery, relay, false);
    }

    @Override
    public String marshall() throws NostrException {
        Character c = this.genericTagQuery.getTagName();
        var values = this.genericTagQuery.getValue();
        int i = 0;

        StringBuilder result = new StringBuilder("\"#").append(c.toString()).append("\":[");
        for (String s : values) {

            if (!escape) {
                result.append("\"");
            } else {
                result.append("\\\"");
            }
            result.append(s);
            if (!escape) {
                result.append("\"");
            } else {
                result.append("\\\"");
            }

            if (++i < values.size()) {
                result.append(",");
            }
        }
        result.append("]");
        
        return result.toString();
    }

}
