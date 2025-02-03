package nostr.event.filter;

import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;

import java.util.function.Function;
import java.util.function.Predicate;

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
