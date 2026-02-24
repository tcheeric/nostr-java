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
import nostr.event.filter.EventFilter;
import nostr.event.json.EventJsonMapper;
import nostr.event.json.codec.EventEncodingException;
import nostr.event.json.codec.FiltersEncoder;

import java.time.temporal.ValueRange;
import java.util.List;
import java.util.stream.IntStream;

import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

/**
 * @author squirrel
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends BaseMessage {
  public static final int FILTERS_START_INDEX = 2;

  @JsonProperty private final String subscriptionId;

  @JsonProperty private final List<EventFilter> filtersList;

  public ReqMessage(@NonNull String subscriptionId, @NonNull EventFilter... filtersList) {
    this(subscriptionId, List.of(filtersList));
  }

  public ReqMessage(@NonNull String subscriptionId, @NonNull List<EventFilter> filtersList) {
    super(Command.REQ.name());
    validateSubscriptionId(subscriptionId);
    this.subscriptionId = subscriptionId;
    this.filtersList = filtersList;
  }

  @Override
  public String encode() throws EventEncodingException {
    var encoderArrayNode = JsonNodeFactory.instance.arrayNode();
    encoderArrayNode.add(getCommand()).add(getSubscriptionId());

    filtersList.stream()
        .map(FiltersEncoder::new)
        .map(FiltersEncoder::encode)
        .map(ReqMessage::createJsonNode)
        .forEach(encoderArrayNode::add);

    try {
      return EventJsonMapper.getMapper().writeValueAsString(encoderArrayNode);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode req message", e);
    }
  }

  public static <T extends BaseMessage> T decode(
      @NonNull Object subscriptionId, @NonNull String jsonString) throws EventEncodingException {
    validateSubscriptionId(subscriptionId.toString());
    @SuppressWarnings("unchecked")
    T result =
        (T)
            new ReqMessage(
                subscriptionId.toString(),
                getJsonFiltersList(jsonString).stream()
                    .map(EventFilter::fromJson)
                    .toList());
    return result;
  }

  private static JsonNode createJsonNode(String jsonNode) throws EventEncodingException {
    try {
      return EventJsonMapper.getMapper().readTree(jsonNode);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException(
          String.format("Malformed encoding ReqMessage json: [%s]", jsonNode), e);
    }
  }

  private static void validateSubscriptionId(String subscriptionId) {
    if (!ValueRange.of(1, 64).isValidIntValue(subscriptionId.length())) {
      throw new IllegalArgumentException(
          String.format(
              "SubscriptionId length must be between 1 and 64 characters but was [%d]",
              subscriptionId.length()));
    }
  }

  private static List<String> getJsonFiltersList(String jsonString) throws EventEncodingException {
    try {
      JsonNode root = I_DECODER_MAPPER_BLACKBIRD.readTree(jsonString);
      return IntStream.range(FILTERS_START_INDEX, root.size())
          .mapToObj(idx -> root.get(idx).toString())
          .toList();
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Invalid ReqMessage filters json", e);
    }
  }
}
