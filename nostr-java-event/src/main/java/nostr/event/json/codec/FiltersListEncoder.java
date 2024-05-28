package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import nostr.base.IEncoder;
import nostr.event.impl.Filters;
import nostr.event.list.FiltersList;
import nostr.util.NostrException;

@Data
public class FiltersListEncoder<T extends Filters> implements IEncoder {

    private final FiltersList<T> filtersList;

    public FiltersListEncoder(FiltersList<T> filtersList) {
        this.filtersList = filtersList;
    }

    @Override
    public String encode() {
        try {
            return toJson();
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String toJson() throws NostrException {
        return toJsonCommaSeparated();
    }

    private String toJsonArray() throws NostrException {
        try {
            StringBuilder sb = new StringBuilder();
            for (Object filter : getFiltersList().getList()) {
                if (!sb.isEmpty()) {
                    sb.append(",");
                }
                sb.append(MAPPER.writeValueAsString(filter));
            }
            return sb.toString();
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new NostrException(e);
        }
    }

    private String toJsonCommaSeparated() throws NostrException {
        JsonNode node = MAPPER.valueToTree(getFiltersList().getList());
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }

    }
}
