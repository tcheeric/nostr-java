package nostr.event.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Filterable {
  ObjectMapper mapper = new ObjectMapper();

  Predicate<GenericEvent> getPredicate();
  <T> T getFilterCriterion();
  Object getFilterableValue();
  String getFilterKey();

  default <T extends BaseTag> List<T> getTypeSpecificTags(Class<T> tagClass, GenericEvent event) {
    return event.getTags().stream()
        .filter(tagClass::isInstance)
        .map(tagClass::cast)
        .toList();
  }

  default ObjectNode toObjectNode(ObjectNode objectNode) {
    ArrayNode arrayNode = mapper.createArrayNode();

    Optional.ofNullable(objectNode.get(getFilterKey()))
        .ifPresent(jsonNode ->
            jsonNode.elements().forEachRemaining(arrayNode::add));

    addToArrayNode(arrayNode);

    return objectNode.set(getFilterKey(), arrayNode);
  }

  default void addToArrayNode(ArrayNode arrayNode) {
    arrayNode.addAll(
        mapper.createArrayNode().add(
            getFilterableValue().toString()));
  }
}
