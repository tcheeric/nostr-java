package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.IdentifierTag;

import java.util.function.Predicate;

@EqualsAndHashCode
public class IdentifierTagFilter<T extends IdentifierTag> implements Filterable {
  public final static String filterKey = "#d";
  private final T identifierTag;

  public IdentifierTagFilter(T identifierTag) {
    this.identifierTag = identifierTag;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(IdentifierTag.class, genericEvent).stream().anyMatch(genericEventIdentifiterTag ->
            genericEventIdentifiterTag.getId().equals(this.identifierTag.getId()));
  }

  @Override
  public T getFilterCriterion() {
    return identifierTag;
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return processArrayNode(objectNode);
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public String getFilterableValue() {
    return identifierTag.getId();
  }
}
