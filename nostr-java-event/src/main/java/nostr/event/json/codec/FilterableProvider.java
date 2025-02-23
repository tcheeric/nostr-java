package nostr.event.json.codec;

import com.fasterxml.jackson.databind.JsonNode;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.filter.AddressableTagFilter;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.IdentifierTag;

import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

class FilterableProvider {
  protected static List<Filterable> getFilterable(String type, JsonNode node) {
    return switch (type) {
      case ReferencedPublicKeyFilter.filterKey -> getFilterable(node, referencedPubKey -> new ReferencedPublicKeyFilter<>(new PublicKey(referencedPubKey.asText())));
      case ReferencedEventFilter.filterKey -> getFilterable(node, referencedEvent -> new ReferencedEventFilter<>(new GenericEvent(referencedEvent.asText())));
      case AddressableTagFilter.filterKey -> getFilterable(node, addressableTag -> new AddressableTagFilter<>(AddressableTagFilter.createAddressTag(addressableTag)));
      case IdentifierTagFilter.filterKey -> getFilterable(node, identifierTag -> new IdentifierTagFilter<>(new IdentifierTag(identifierTag.asText())));
      case AuthorFilter.filterKey -> getFilterable(node, author -> new AuthorFilter<>(new PublicKey(author.asText())));
      case EventFilter.filterKey -> getFilterable(node, event -> new EventFilter<>(new GenericEvent(event.asText())));
      case KindFilter.filterKey -> getFilterable(node, kindNode -> new KindFilter<>(Kind.valueOf(kindNode.asInt())));
      case SinceFilter.filterKey -> List.of(new SinceFilter(node.asLong()));
      case UntilFilter.filterKey -> List.of(new UntilFilter(node.asLong()));
      default ->
          getFilterable(node, genericNode -> new GenericTagQueryFilter<>(new GenericTagQuery(type, genericNode.asText())));
    };
  }

  private static List<Filterable> getFilterable(JsonNode jsonNode, Function<JsonNode, Filterable> filterFunction) {
    return StreamSupport.stream(jsonNode.spliterator(), false).map(filterFunction).toList();
  }
}
