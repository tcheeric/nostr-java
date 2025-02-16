package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.BaseMessage;
import nostr.event.filter.Filterable;
import nostr.event.filter.FilterableProvider;
import nostr.event.filter.Filters;
import nostr.event.json.codec.FiltersEncoder;

import java.util.HashMap;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author squirrel
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends BaseMessage {
  private final static ObjectMapper mapper = new ObjectMapper();

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

  @SneakyThrows
  @Override
  public String encode() throws JsonProcessingException {
    getArrayNode()
        .add(getCommand())
        .add(getSubscriptionId());

//    filtersList.stream()
//        .map(FiltersEncoderRxR::new)
//        .map(FiltersEncoderRxR::encode)
//        .map(IEncoder.MAPPER::readTree)
//        .forEach(jsonNode ->
//            getArrayNode().add(jsonNode));


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

    jsonNodesList.forEach(jsonNode -> getArrayNode().add(jsonNode));

    return IEncoder.MAPPER.writeValueAsString(getArrayNode());
  }

  public static <T extends BaseMessage> T decode(@NonNull Object subscriptionId, @NonNull List<String> jsonFiltersList) {
    return (T) new ReqMessage(subscriptionId.toString(),
        jsonFiltersList.stream().map(
            ReqMessage::createFiltersFromJson).toList());
  }

  @SneakyThrows
  private static Filters createFiltersFromJson(String jsonFiltersList) {
    final Map<String, List<Filterable>> filterPluginsMap = new HashMap<>();

    mapper.readTree(jsonFiltersList).fields().forEachRemaining(field ->
        filterPluginsMap.put(
            field.getKey(),
            FilterableProvider.getFilterable(
                field.getKey(),
                field.getValue())));

    return new Filters(filterPluginsMap);
  }
}
