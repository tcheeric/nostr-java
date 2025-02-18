package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.ArrayNodeCollector;
import nostr.event.json.codec.Collectors;
import nostr.event.json.codec.ObjectNodeCollector;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Filterable {
  ObjectMapper mapper = new ObjectMapper();

  Predicate<GenericEvent> getPredicate();
  <T> T getFilterCriterion();
  ObjectNode toObjectNode(ObjectNode objectNode);
  String getFilterableValue();
  String getFilterKey();

  default <T extends BaseTag> List<T> getTypeSpecificTags(Class<T> tagClass, GenericEvent event) {
    return event.getTags().stream()
        .filter(tagClass::isInstance)
        .map(tagClass::cast)
        .toList();
  }

  default ObjectNode processArrayNode(ObjectNode objectNode) {
    Optional<JsonNode> jsonNode1 = Optional.ofNullable(objectNode.get(getFilterKey()));

    ArrayNodeCollector arrayNodeCollector = Collectors.toArrayNode();

    ArrayNode arrayNode = arrayNodeCollector.supplier().get();
    jsonNode1.ifPresent(jsonNode -> {
      jsonNode.elements().forEachRemaining(jsonNode2 ->
          arrayNodeCollector.accumulator().accept(arrayNode, jsonNode2));
    });

    ArrayNode arrayNode1 = mapper.createArrayNode();
    ArrayNode add2 = arrayNode1.add(getFilterableValue());

    arrayNodeCollector.combiner().apply(arrayNode, add2);

    objectNode.set(getFilterKey(), arrayNode);

    return objectNode;
  }

  default ObjectNode processObjectNode(String key, ObjectNode objectNode) {
    Optional<JsonNode> jsonNode1 = Optional.ofNullable(objectNode.get(getFilterKey()));

    ObjectNodeCollector objectNodeCollector = Collectors.toObjectNode();

    ObjectNode objectNode1 = objectNodeCollector.supplier().get();
    jsonNode1.ifPresent(jsonNode -> {
      jsonNode.elements().forEachRemaining(jsonNode2 ->
          objectNodeCollector.accumulator().accept(objectNode1, jsonNode2));
    });

    ObjectNode objectNode2 = mapper.createObjectNode();

//    TODO: hack string to int, needs fix
    ObjectNode add2 = objectNode2.put(key, Integer.valueOf(getFilterableValue()));

    objectNodeCollector.combiner().apply(objectNode1, add2);

    return add2;
  }
}
