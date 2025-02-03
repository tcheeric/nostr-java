package nostr.event.filter;

import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.IdentifierTag;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Filterable {
  Predicate<GenericEvent> getPredicate();
  <T> T getFilterCriterion();
  <T> Function<String, T> createContainedInstance();
  String getFilterKey();

  default List<GenericTag> getGenericTags(GenericEvent genericEvent) {
    return genericEvent.getTags().stream()
        .filter(GenericTag.class::isInstance)
        .map(GenericTag.class::cast)
        .toList();
  }

  default List<IdentifierTag> getIdentifierTags(GenericEvent genericEvent) {
    return genericEvent.getTags().stream()
        .filter(IdentifierTag.class::isInstance)
        .map(IdentifierTag.class::cast)
        .toList();
  }
}
