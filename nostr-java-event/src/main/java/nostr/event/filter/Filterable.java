package nostr.event.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.function.Predicate;

public interface Filterable {
  ObjectMapper mapper = new ObjectMapper();

  Predicate<GenericEvent> getPredicate();
  <T> T getFilterCriterion();
  ArrayNode toArrayNode();
  String getFilterKey();

  default <T extends BaseTag> List<T> getTypeSpecificTags(Class<T> tagClass, GenericEvent event) {
    return event.getTags().stream()
        .filter(tagClass::isInstance)
        .map(tagClass::cast)
        .toList();
  }
}
