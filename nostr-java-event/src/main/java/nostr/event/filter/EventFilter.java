package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

@EqualsAndHashCode
public class EventFilter<T extends GenericEvent> implements Filterable {
  public final static String filterKey = "ids";
  private final T event;

  public EventFilter(T event) {
    this.event = event;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        this.event.getId().equals(genericEvent.getId());
  }

  @Override
  public T getFilterCriterion() {
    return event;
  }

  @Override
  public ArrayNode toArrayNode() {
    return mapper.createArrayNode().add(event.getId());
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
