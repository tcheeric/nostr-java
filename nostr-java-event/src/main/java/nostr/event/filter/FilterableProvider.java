package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.stream.StreamSupport;

public class FilterableProvider {
  public static List<Filterable> getFilterable(String type, JsonNode node) {
    return switch (type) {
      case ReferencedPublicKeyFilter.filterKey ->
          Filters.getFilterable(node, referencedPubKey -> new ReferencedPublicKeyFilter<PublicKey>(new PublicKey(referencedPubKey.asText())));
      case ReferencedEventFilter.filterKey ->
          Filters.getFilterable(node, referencedEvent -> new ReferencedEventFilter<GenericEvent>(new GenericEvent(referencedEvent.asText())));
      case PublicKeyFilter.filterKey ->
          Filters.getFilterable(node, author -> new PublicKeyFilter<PublicKey>(new PublicKey(author.asText())));
      case EventFilter.filterKey -> Filters.getFilterable(node, event -> new EventFilter<GenericEvent>(new GenericEvent(event.asText())));
      case SinceFilter.filterKey -> Filters.getFilterable(node, since -> new SinceFilter(since.asLong()));
      case UntilFilter.filterKey -> Filters.getFilterable(node, until -> new UntilFilter(until.asLong()));
      case KindFilter.filterKey -> Filters.getFilterable(node, kindNode -> new KindFilter<Kind>(Kind.valueOf(kindNode.asInt())));
//            case AddressableTagFilter.filterKey -> new XYZ<>(getGenericTagQuery(node));
//            case IdentifierTagFilter.filterKey -> new XYZ<>(getGenericTagQuery(node));
      default -> Filters.getFilterable(node, kindNode ->
          new GenericTagQueryFilter<GenericTagQuery>(
              new GenericTagQuery(
                  type,
                  StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).toList())));
    };
  }
}
