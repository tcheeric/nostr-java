package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;
import nostr.event.json.codec.FiltersEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends BaseMessage {

    @JsonProperty
    private final String subscriptionId;

    @JsonProperty
    private final List<Filters> filtersList;

    public ReqMessage(String subscriptionId, Filters filters) {
        this(subscriptionId, List.of(filters));
    }

    public ReqMessage(String subscriptionId, List<Filters> incomingFiltersList) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = new ArrayList<>();
        this.filtersList.addAll(incomingFiltersList);
    }

    @Override
    public String encode() throws JsonProcessingException {
        getArrayNode()
            .add(getCommand())
            .add(getSubscriptionId());
        List<Filters> localFiltersList = getFiltersList();
        for (Filters f : localFiltersList) {
            try {
                FiltersEncoder filtersEncoder = new FiltersEncoder(f);
                var filterNode = IEncoder.MAPPER.readTree(filtersEncoder.encode());
                getArrayNode().add(filterNode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return IEncoder.MAPPER.writeValueAsString(getArrayNode());
    }
}
