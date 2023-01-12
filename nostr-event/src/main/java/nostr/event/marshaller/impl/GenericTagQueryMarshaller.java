package nostr.event.marshaller.impl;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.GenericTagQuery;
import nostr.base.Relay;
import nostr.base.IMarshaller;
import nostr.base.NipUtil;
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

        if (this.relay != null) {
            if (!NipUtil.checkSupport(relay, this.genericTagQuery)) {
                return "";
            }
        }

        Character c = this.genericTagQuery.getTagName();
        var values = this.genericTagQuery.getValue();
        int i = 0;

        StringBuilder result = new StringBuilder("\"#").append(c.toString()).append("\":[");

        result.append(values.stream().map(s -> {
            final StringBuilder sb = new StringBuilder();
            if (!escape) {
                sb.append("\"");
            } else {
                sb.append("\\\"");
            }
            sb.append(s);
            if (!escape) {
                sb.append("\"");
            } else {
                sb.append("\\\"");
            }
            return sb.toString();
        }).collect(Collectors.joining(",")));

        result.append("]");

        return result.toString();
    }

}
