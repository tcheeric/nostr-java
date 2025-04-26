package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.filter.Filters;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.json.codec.FiltersEncoder;

import java.time.temporal.ValueRange;
import java.util.List;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;

/**
 * @author squirrel
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends GenericMessage {
    @JsonProperty
    private final String subscriptionId;

    @JsonProperty
    private final List<Filters> filtersList;

    public ReqMessage(@NonNull String subscriptionId, Filters... filtersList) {
        this(subscriptionId, List.of(filtersList));
    }

    public ReqMessage(@NonNull String subscriptionId, List<Filters> filtersList) {
        super(Command.REQ.name());
        validateSubscriptionId(subscriptionId);
        this.subscriptionId = subscriptionId;
        this.filtersList = filtersList;
    }

    @Override
    public String encode() throws JsonProcessingException {
        getArrayNode()
            .add(getCommand())
            .add(getSubscriptionId());

        filtersList.stream()
            .map(FiltersEncoder::new)
            .map(FiltersEncoder::encode)
            .map(ReqMessage::createJsonNode)
            .forEach(jsonNode ->
                getArrayNode().add(jsonNode));

        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(getArrayNode());
    }

    public static <T extends BaseMessage> T decode(@NonNull Object subscriptionId, @NonNull List<String> jsonFiltersList) {
        validateSubscriptionId(subscriptionId.toString());
        ReqMessage reqMessage = new ReqMessage(
            subscriptionId.toString(),
            jsonFiltersList.stream().map(filtersList ->
                new FiltersDecoder().decode(filtersList)).toList());
        return (T) reqMessage;
    }

    private static void validateSubscriptionId(String subscriptionId) {
        if (!ValueRange.of(1, 64).isValidIntValue(subscriptionId.length())) {
            throw new IllegalArgumentException(String.format("SubscriptionId length must be between 1 and 64 characters but was [%d]", subscriptionId.length()));
        }
    }

    private static JsonNode createJsonNode(String jsonNode) {
        try {
            return ENCODER_MAPPED_AFTERBURNER.readTree(jsonNode);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Malformed encoding ReqMessage json: [%s]", jsonNode), e);
        }
    }
}
