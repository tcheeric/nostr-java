package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;
import nostr.event.json.codec.FiltersEncoder;

import java.time.temporal.ValueRange;
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

    public ReqMessage(@NonNull String subscriptionId, Filters filters) {
        this(subscriptionId, List.of(filters));
    }

    public ReqMessage(@NonNull String subscriptionId, List<Filters> incomingFiltersList) {
        super(Command.REQ.name());
        if (!ValueRange.of(1, 64).isValidIntValue(subscriptionId.length())) {
            throw new IllegalArgumentException(String.format("subscriptionId length must be between 1 and 64 characters but was [%d]", subscriptionId.length()));
        }
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

    public static <T extends BaseMessage> T decode(@NonNull Object[] msgArr, ObjectMapper mapper) {
        var len = msgArr.length - 2;
        var filtersArr = new Object[len];
        System.arraycopy(msgArr, 2, filtersArr, 0, len);
        var filtersList = mapper.convertValue(filtersArr, new TypeReference<List<Filters>>() {
        });
        return (T) new ReqMessage(msgArr[1].toString(), filtersList);
    }
}
