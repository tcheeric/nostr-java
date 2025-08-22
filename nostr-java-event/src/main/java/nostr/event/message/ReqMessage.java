package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.filter.Filters;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.json.codec.EventEncodingException;

import java.time.temporal.ValueRange;
import java.util.List;
import java.util.stream.IntStream;

import static nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD;
import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

/**
 * @author squirrel
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends BaseMessage {
    public static final int FILTERS_START_INDEX = 2;

    @JsonProperty
    private final String subscriptionId;

    @JsonProperty
    private final List<Filters> filtersList;

    public ReqMessage(@NonNull String subscriptionId, @NonNull Filters... filtersList) {
        this(subscriptionId, List.of(filtersList));
    }

    public ReqMessage(@NonNull String subscriptionId, @NonNull List<Filters> filtersList) {
        super(Command.REQ.name());
        validateSubscriptionId(subscriptionId);
        this.subscriptionId = subscriptionId;
        this.filtersList = filtersList;
    }

    @Override
    public String encode() throws EventEncodingException {
        var encoderArrayNode = JsonNodeFactory.instance.arrayNode();
        encoderArrayNode
          .add(getCommand())
          .add(getSubscriptionId());

        filtersList.stream()
          .map(FiltersEncoder::new)
          .map(FiltersEncoder::encode)
          .map(ReqMessage::createJsonNode)
          .forEach(encoderArrayNode::add);

        try {
            return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(encoderArrayNode);
        } catch (JsonProcessingException e) {
            throw new EventEncodingException("Failed to encode req message", e);
        }
    }

    public static <T extends BaseMessage> T decode(@NonNull Object subscriptionId, @NonNull String jsonString) throws EventEncodingException {
        validateSubscriptionId(subscriptionId.toString());
        return (T) new ReqMessage(
          subscriptionId.toString(),
          getJsonFiltersList(jsonString).stream().map(filtersList ->
                new FiltersDecoder().decode(filtersList)).toList());
    }

    private static JsonNode createJsonNode(String jsonNode) throws EventEncodingException {
        try {
            return ENCODER_MAPPER_BLACKBIRD.readTree(jsonNode);
        } catch (JsonProcessingException e) {
            throw new EventEncodingException(String.format("Malformed encoding ReqMessage json: [%s]", jsonNode), e);
        }
    }

    private static void validateSubscriptionId(String subscriptionId) {
        if (!ValueRange.of(1, 64).isValidIntValue(subscriptionId.length())) {
            throw new IllegalArgumentException(String.format("SubscriptionId length must be between 1 and 64 characters but was [%d]", subscriptionId.length()));
        }
    }

    private static List<String> getJsonFiltersList(String jsonString) throws EventEncodingException {
        try {
            return IntStream.range(FILTERS_START_INDEX, I_DECODER_MAPPER_BLACKBIRD.readTree(jsonString).size())
                     .mapToObj(idx -> readTree(jsonString, idx)).toList();
        } catch (JsonProcessingException e) {
            throw new EventEncodingException("Invalid ReqMessage filters json", e);
        }
    }

    private static String readTree(String jsonString, int idx) throws EventEncodingException {
        try {
            return I_DECODER_MAPPER_BLACKBIRD.readTree(jsonString).get(idx).toString();
        } catch (JsonProcessingException e) {
            throw new EventEncodingException("Failed to read json tree", e);
        }
    }
}
