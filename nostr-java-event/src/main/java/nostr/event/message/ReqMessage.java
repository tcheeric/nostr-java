package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.BaseMessage;
import nostr.event.filter.Filters;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.json.codec.FiltersEncoder;

import java.time.temporal.ValueRange;
import java.util.List;

/**
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

  public ReqMessage(String subscriptionId, Filters... filtersList) {
    this(subscriptionId, List.of(filtersList));
  }

  public ReqMessage(String subscriptionId, List<Filters> filtersList) {
    super(Command.REQ.name());
    if (!ValueRange.of(1, 64).isValidIntValue(subscriptionId.length())) {
      throw new IllegalArgumentException(String.format("subscriptionId length must be between 1 and 64 characters but was [%d]", subscriptionId.length()));
    }
    this.filtersList = filtersList;
    this.subscriptionId = subscriptionId;
  }

  @Override
  public String encode() throws JsonProcessingException {
    getArrayNode()
        .add(getCommand())
        .add(getSubscriptionId());

//    filtersList.stream()
//        .map(FiltersEncoder::new)
//        .map(FiltersEncoder::encode)
//        .map(IEncoder.MAPPER::readTree)
//        .forEach(jsonNode ->
//            getArrayNode().add(jsonNode));

//    TODO: remove below once above confirmed working
    List<String> encodedFilterList = filtersList.stream().map(FiltersEncoder::new).map(FiltersEncoder::encode).toList();

    List<JsonNode> jsonNodesList = encodedFilterList.stream().map(
        encode -> {
          try {
            return IEncoder.MAPPER.readTree(encode);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        }
    ).toList();

    jsonNodesList.forEach(jsonNode -> {
      ArrayNode arrayNode = getArrayNode();
      arrayNode.add(jsonNode);
    });

    String s = IEncoder.MAPPER.writeValueAsString(getArrayNode());
    System.out.println(s);
    return s;
  }

  public static <T extends BaseMessage> T decode(@NonNull Object subscriptionId, @NonNull List<String> jsonFiltersList) {

    ReqMessage reqMessage = new ReqMessage(subscriptionId.toString(),
        jsonFiltersList.stream().map(
            ReqMessage::createFiltersFromJson).toList());

    return (T) reqMessage;
  }

  private static Filters createFiltersFromJson(String jsonFiltersList) {
    return new FiltersDecoder<>().decode(jsonFiltersList);
  }
}
