package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class ReferencedEventFilter<T extends GenericEvent> implements Filterable {
  private final T referencedEvent;

  public ReferencedEventFilter(T referencedEvent) {
    this.referencedEvent = referencedEvent;
  }
  @Override
  public BiPredicate<T, GenericEvent> getBiPredicate() {
    return (referencedEvent, genericEvent) -> referencedEvent.getId().equals(genericEvent.getId());
  }
  @Override
  public T getFilterCriterion() {
    return referencedEvent;
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
