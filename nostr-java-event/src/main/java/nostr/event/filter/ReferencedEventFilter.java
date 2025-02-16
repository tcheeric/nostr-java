package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
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
  public ArrayNode toArrayNode() {
    return mapper.createArrayNode().add(referencedEvent.getId());
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
