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
import nostr.base.GenericTagQuery;
import nostr.base.IEncoder;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.Kind;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.FiltersCore;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.PublicKeyFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.FiltersEncoderRxR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * @author squirrel
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessageRxR extends BaseMessage {
  private final static ObjectMapper mapper = new ObjectMapper();

  @JsonProperty
  private final String subscriptionId;

  @JsonProperty
  private final FiltersCore filtersCore;

  public ReqMessageRxR(String subscriptionId, Map<String, List<Filterable>> filterPlugins) {
    super(Command.REQ.name());
    this.filtersCore = new FiltersCore(filterPlugins);
    this.subscriptionId = subscriptionId;
  }

  @Override
  public String encode() throws JsonProcessingException {
    getArrayNode()
        .add(getCommand())
        .add(getSubscriptionId());

    try {
      FiltersEncoderRxR filtersEncoder = new FiltersEncoderRxR(filtersCore);
      var filterNode = IEncoder.MAPPER.readTree(filtersEncoder.encode());
      getArrayNode().add(filterNode);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return IEncoder.MAPPER.writeValueAsString(getArrayNode());
  }

  @SneakyThrows
  public static <T extends BaseMessage> T decode(@NonNull Object subscriptionId, @NonNull String jsonString) {
    final Map<String, List<Filterable>> filterPluginsMap = new HashMap<>();
    mapper.readTree(jsonString).fields().forEachRemaining(field ->
        filterPluginsMap.put(
            field.getKey(),
            getFilterable(
                field.getKey(),
                field.getValue())));

//    TODO: variant 1 stream generating map, almost working
//    Iterator<JsonNode> elements = mapper.readTree(jsonString).elements();
//    List<Map<String, List<Filterable>>> list = Stream.of(elements).map(jsonNodeIterator ->
//        {
//          JsonNode next = jsonNodeIterator.next();
//          Iterator<Entry<String, JsonNode>> fields1 = next.fields();
//          return fields1;
//        }).map(entryIterator ->
//            Map.of(
//                entryIterator.next().getKey(),
//                getFilterable(
//                    entryIterator.next().getKey(),
//                    entryIterator.next().getValue())))
//        .toList();

//    TODO: variant 2 stream generating map, left as potentially helpful for variant 1
//    JsonNode jsonNode = mapper.readTree(jsonString);
//    Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
//    List<Map<String, List<Filterable>>> kindOnly = Stream.of(fields)
//        .map(entryIterator ->
//            Map.of(entryIterator.next().getKey(),
//                getFilterable(
//                    entryIterator.next().getKey(),
//                    entryIterator.next().getValue()))).toList();

    return (T) new ReqMessageRxR(subscriptionId.toString(), filterPluginsMap);
  }

//  TODO: below functionals can/should be refactored into their associated filter classes
  static List<Filterable> getFilterable(String type, JsonNode node) {
    return switch (type) {
      case ReferencedPublicKeyFilter.filterKey ->
          getFilterable(node, referencedPubKey -> new ReferencedPublicKeyFilter<>(new PublicKey(referencedPubKey.asText())));
      case ReferencedEventFilter.filterKey ->
          getFilterable(node, referencedEvent -> new ReferencedEventFilter<>(new GenericEvent(referencedEvent.asText())));
      case PublicKeyFilter.filterKey ->
          getFilterable(node, author -> new PublicKeyFilter<>(new PublicKey(author.asText())));
      case EventFilter.filterKey -> getFilterable(node, event -> new EventFilter<>(new GenericEvent(event.asText())));
      case SinceFilter.filterKey -> getFilterable(node, since -> new SinceFilter(since.asLong()));
      case UntilFilter.filterKey -> getFilterable(node, until -> new UntilFilter(until.asLong()));
      case KindFilter.filterKey -> getFilterable(node, kindNode -> new KindFilter<>(Kind.valueOf(kindNode.asInt())));
//            case AddressableTagFilter.filterKey -> new XYZ<>(getGenericTagQuery(node));
//            case IdentifierTagFilter.filterKey -> new XYZ<>(getGenericTagQuery(node));
      default -> getFilterable(node, kindNode ->
          new GenericTagQueryFilter<>(
              new GenericTagQuery(
                  type,
                  StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).toList())));
    };
  }

  private static List<Filterable> getFilterable(JsonNode jsonNode, Function<JsonNode, Filterable> filterFunction) {
    return StreamSupport.stream(jsonNode.spliterator(), false).map(filterFunction).toList();
  }
}
