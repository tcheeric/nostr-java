package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

import java.util.HashSet;
import java.util.function.Predicate;

@EqualsAndHashCode
public class GenericTagQueryFilter<T extends GenericTagQuery> implements Filterable {
  private final T genericTagQuery;

  public GenericTagQueryFilter(T genericTagQuery) {
    this.genericTagQuery = genericTagQuery;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(GenericTag.class, genericEvent).stream()
            .filter(genericTag ->
                genericTag.getCode().equals(this.genericTagQuery.getTagName()))
            .anyMatch(genericTag ->
                new HashSet<>(genericTag
                    .getAttributes().stream().map(
                        ElementAttribute::getValue).toList())
                    .contains(
                        this.genericTagQuery.getValue()));
  }

  @Override
  public T getFilterCriterion() {
    return genericTagQuery;
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return processArrayNodeString(objectNode);
  }

  @Override
  public String getFilterKey() {
    return genericTagQuery.getTagName();
  }

  @Override
  public String getFilterableValue() {
    return genericTagQuery.getValue();
  }
}
