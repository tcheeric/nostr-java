package nostr.test.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Spliterator;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class ComparatorWithoutOrder implements Comparator<Iterable<? extends JsonNode>> {

  private boolean ignoreElementOrderInArrays = true;

  public static boolean isEquivalentJson(JsonNode target, JsonNode other) {
    return new ComparatorWithoutOrder().compare(target, other) == 0;
  }

  @Override
  public int compare(Iterable<? extends JsonNode> o1, Iterable<? extends JsonNode> o2) {
    if (o1 == null || o2 == null) {
      return -1;
    }
    if (o1 == o2) {
      return 0;
    }
    if (o1 instanceof JsonNode && o2 instanceof JsonNode) {
      return compareJsonNodes((JsonNode) o1, (JsonNode) o2);
    }
    return -1;
  }

  private int compareJsonNodes(JsonNode o1, JsonNode o2) {
    if (o1 == null || o2 == null) {
      return -1;
    }
    if (o1 == o2) {
      return 0;
    }
    if (!o1.getNodeType().equals(o2.getNodeType())) {
      return -1;
    }
    switch (o1.getNodeType()) {
      case NULL:
        return o2.isNull() ? 0 : -1;
      case BOOLEAN:
        return o1.asBoolean() == o2.asBoolean() ? 0 : -1;
      case STRING:
        return o1.asText().equals(o2.asText()) ? 0 : -1;
      case NUMBER:
        double double1 = o1.asDouble();
        double double2 = o2.asDouble();
        return Math.abs(double1 - double2) / Math.max(double1, double2) < 0.999 ? 0 : -1;
      case OBJECT:
        // ignores fields with null value that are missing at other JSON
        var missingNotNullFields = Sets
            .symmetricDifference(Sets.newHashSet(o1.fieldNames()), Sets.newHashSet(o2.fieldNames()))
            .stream()
            .filter(missingField -> isNotNull(o1, missingField) || isNotNull(o2, missingField))
            .toList();
        if (!missingNotNullFields.isEmpty()) {
          return -1;
        }
        Integer reduce1 = stream(spliteratorUnknownSize(o1.fieldNames(), Spliterator.ORDERED), false)
            .map(key -> compareJsonNodes(o1.get(key), o2.get(key)))
            .reduce(0, (a, b) -> a == -1 || b == -1 ? -1 : 0);
        return reduce1;
      case ARRAY:
        if (o1.size() != o2.size()) {
          return -1;
        }
        if (o1.isEmpty()) {
          return 0;
        }
        var o1Iterator = o1.elements();
        var o2Iterator = o2.elements();
        var o2Elements = Sets.newHashSet(o2.elements());
        Integer reduce = stream(spliteratorUnknownSize(o1Iterator, Spliterator.ORDERED), false)
            .map(o1Next -> ignoreElementOrderInArrays ?
                lookForMatchingElement(o1Next, o2Elements) : compareJsonNodes(o1Next, o2Iterator.next()))
            .reduce(0, (a, b) -> a == -1 || b == -1 ? -1 : 0);
        return reduce;
      case MISSING:
      case BINARY:
      case POJO:
      default:
        return -1;
    }
  }

  private int lookForMatchingElement(JsonNode elementToLookFor, Collection<JsonNode> collectionOfElements) {
    // Note: O(n^2) complexity
    return collectionOfElements.stream()
        .filter(o2Element -> compareJsonNodes(elementToLookFor, o2Element) == 0)
        .findFirst()
        .map(o2Element -> 0)
        .orElse(-1);
  }

  private static boolean isNotNull(JsonNode jsonObject, String fieldName) {
    return Optional.ofNullable(jsonObject.get(fieldName))
        .map(JsonNode::getNodeType)
        .filter(nodeType -> nodeType != JsonNodeType.NULL)
        .isPresent();
  }
}
