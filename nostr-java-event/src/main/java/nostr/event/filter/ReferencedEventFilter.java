package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;

import java.util.function.Predicate;

@EqualsAndHashCode
public class ReferencedEventFilter<T extends GenericEvent> implements Filterable {
  public final static String filterKey = "#e";
  private final T referencedEvent;

  public ReferencedEventFilter(T referencedEvent) {
    this.referencedEvent = referencedEvent;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(EventTag.class, genericEvent).stream()
            .anyMatch(eventTag ->
                eventTag.getIdEvent().equals(referencedEvent.getId()));
  }

  @Override
  public T getFilterCriterion() {
    return referencedEvent;
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return processArrayNodeString(objectNode);
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public String getFilterableValue() {
    return referencedEvent.getId();
  }
}
