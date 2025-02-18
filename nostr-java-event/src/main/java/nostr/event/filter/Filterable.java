package nostr.event.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
    return processArrayNode(objectNode, this::getFilterableValue);
  }

  default ObjectNode processArrayNode(@NonNull ObjectNode objectNode, Supplier<Object> objectSupplier) {
    ArrayNode arrayNode = mapper.createArrayNode();

    Optional.ofNullable(objectNode.get(getFilterKey()))
        .ifPresent(jsonNode ->
            jsonNode.elements().forEachRemaining(arrayNode::add));

    arrayNode.addAll(
        mapper.createArrayNode().add(
            objectSupplier.get().toString()));

    return objectNode.set(getFilterKey(), arrayNode);
  }

  default ObjectNode processArrayNodeIntRxR(@NonNull ObjectNode objectNode, Supplier<Integer> integerSupplier) {
    ArrayNode arrayNode = mapper.createArrayNode();

    Optional.ofNullable(objectNode.get(getFilterKey()))
        .ifPresent(jsonNode ->
            jsonNode.elements().forEachRemaining(arrayNode::add));

    arrayNode.addAll(
        mapper.createArrayNode().add(
            integerSupplier.get()));

    return objectNode.set(getFilterKey(), arrayNode);
  }
}
