package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.Function;
import java.util.function.Predicate;

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
  public <T> Function<String, T> createContainedInstance() {
    return eventId -> {
      GenericEvent event = new GenericEvent();
      event.setId(eventId);
      return (T) event;
    };
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
