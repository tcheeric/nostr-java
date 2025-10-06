package nostr.event.filter;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.NonNull;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;

public interface Filterable {
  Predicate<GenericEvent> getPredicate();

  <T> T getFilterable();

  Object getFilterableValue();

  String getFilterKey();

  static <T extends BaseTag> List<T> getTypeSpecificTags(
      @NonNull Class<T> tagClass, @NonNull GenericEvent event) {
    return event.getTags().stream().filter(tagClass::isInstance).map(tagClass::cast).toList();
  }

  /**
   * Convenience: return the first tag of the specified type, if present.
   */
  static <T extends BaseTag> java.util.Optional<T> firstTagOfType(
      @NonNull Class<T> tagClass, @NonNull GenericEvent event) {
    return getTypeSpecificTags(tagClass, event).stream().findFirst();
  }

  /**
   * Convenience: return the first tag of the specified type and code, if present.
   */
  static <T extends BaseTag> java.util.Optional<T> firstTagOfTypeWithCode(
      @NonNull Class<T> tagClass, @NonNull String code, @NonNull GenericEvent event) {
    return getTypeSpecificTags(tagClass, event).stream()
        .filter(t -> code.equals(t.getCode()))
        .findFirst();
  }

  /**
   * Convenience: return the first tag of the specified type or throw with a clear message.
   *
   * Rationale: callers often need a single tag instance; this avoids repeated casts and stream code.
   */
  static <T extends BaseTag> T requireTagOfType(
      @NonNull Class<T> tagClass, @NonNull GenericEvent event, @NonNull String errorMessage) {
    return firstTagOfType(tagClass, event)
        .orElseThrow(() -> new java.util.NoSuchElementException(errorMessage));
  }

  /**
   * Convenience: return the first tag of the specified type and code or throw with a clear message.
   */
  static <T extends BaseTag> T requireTagOfTypeWithCode(
      @NonNull Class<T> tagClass,
      @NonNull String code,
      @NonNull GenericEvent event,
      @NonNull String errorMessage) {
    return firstTagOfTypeWithCode(tagClass, code, event)
        .orElseThrow(() -> new java.util.NoSuchElementException(errorMessage));
  }

  /**
   * Convenience overload: generic error if not found.
   */
  static <T extends BaseTag> T requireTagOfTypeWithCode(
      @NonNull Class<T> tagClass, @NonNull String code, @NonNull GenericEvent event) {
    return requireTagOfTypeWithCode(
        tagClass, code, event, "Missing required tag of type %s with code '%s'".formatted(tagClass.getSimpleName(), code));
  }

  default ObjectNode toObjectNode(ObjectNode objectNode) {
    ArrayNode arrayNode = MAPPER_BLACKBIRD.createArrayNode();

    Optional.ofNullable(objectNode.get(getFilterKey()))
        .ifPresent(jsonNode -> jsonNode.elements().forEachRemaining(arrayNode::add));

    addToArrayNode(arrayNode);

    return objectNode.set(getFilterKey(), arrayNode);
  }

  default void addToArrayNode(ArrayNode arrayNode) {
    arrayNode.addAll(MAPPER_BLACKBIRD.createArrayNode().add(getFilterableValue().toString()));
  }
}
