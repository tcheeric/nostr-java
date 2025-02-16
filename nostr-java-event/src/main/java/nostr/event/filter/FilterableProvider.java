package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;

import java.util.List;

public class FilterableProvider {
  public static List<Filterable> getFilterable(String type, JsonNode node) {
    return switch (type) {
      case ReferencedPublicKeyFilter.filterKey -> Filters.getFilterable(node, referencedPubKey -> new ReferencedPublicKeyFilter<>(new PublicKey(referencedPubKey.asText())));
      case ReferencedEventFilter.filterKey -> Filters.getFilterable(node, referencedEvent -> new ReferencedEventFilter<>(new GenericEvent(referencedEvent.asText())));
      case AuthorFilter.filterKey -> Filters.getFilterable(node, author -> new AuthorFilter<>(new PublicKey(author.asText())));
      case EventFilter.filterKey -> Filters.getFilterable(node, event -> new EventFilter<>(new GenericEvent(event.asText())));
      case SinceFilter.filterKey -> Filters.getFilterable(node, since -> new SinceFilter(since.asLong()));
      case UntilFilter.filterKey -> Filters.getFilterable(node, until -> new UntilFilter(until.asLong()));
      case KindFilter.filterKey -> Filters.getFilterable(node, kindNode ->  new KindFilter<>(Kind.valueOf(kindNode.asInt())));
//      TODO: complete & test below
//            case AddressableTagFilter.filterKey -> new XYZ<>(getGenericTagQuery(node));
//            case IdentifierTagFilter.filterKey -> new XYZ<>(getGenericTagQuery(node));
      default -> Filters.getFilterable(node, genericNode -> new GenericTagQueryFilter<>(new GenericTagQuery(type, genericNode.asText())));
    };
  }
}
