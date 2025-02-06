package nostr.event.filter;

import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Filterable {
  Predicate<GenericEvent> getPredicate();
  <T> T getFilterCriterion();
  String toJson();
  String getFilterKey();

  default <T extends BaseTag> List<T> getTypeSpecificTags(Class<T> tagClass, GenericEvent genericEvent) {
    return genericEvent.getTags().stream()
        .filter(tagClass::isInstance)
        .map(tagClass::cast)
        .toList();
  }
}
