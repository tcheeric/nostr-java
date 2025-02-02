package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class EventFilter<T extends GenericEvent> implements Filterable {
  private final T event;

  public EventFilter(T event) {
    this.event = event;
  }
  @Override
  public BiPredicate<T, GenericEvent> getBiPredicate() {
    return (event, genericEvent) -> event.getId().equals(genericEvent.getId());
  }
  @Override
  public T getFilterCriterion() {
    return event;
  }

  @Override
  public <T> Function<String, T> createContainedInstance() {
    return eventId -> {
      GenericEvent event = new GenericEvent();
      event.setId(eventId);
      return (T) event;
    };
  }
}
