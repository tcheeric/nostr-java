package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.Relay;
import nostr.event.BaseEvent;
import nostr.event.list.FiltersList;
import nostr.util.NostrException;

@EqualsAndHashCode(callSuper = true)
@Data
public class FiltersListEncoder extends BaseEventEncoder {

    private final FiltersList filtersList;
    private boolean arrayFlag;

    public FiltersListEncoder(FiltersList filtersList, Relay relay) {
        super(null, relay);
        this.filtersList = filtersList;
        this.arrayFlag = false;
    }

    public FiltersListEncoder(FiltersList filtersList) {
        super(null);
        this.filtersList = filtersList;
        this.arrayFlag = false;
    }

    @Override
    public BaseEvent getEvent() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected String toJson() throws NostrException {
        if (arrayFlag) {
            return toJsonArray();
        } else {
            return toJsonCommaSeparated();
        }
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
