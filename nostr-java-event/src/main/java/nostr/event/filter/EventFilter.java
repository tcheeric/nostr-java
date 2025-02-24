package nostr.event.filter;

import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class EventFilter<T extends GenericEvent> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "ids";

  public EventFilter(T event) {
    super(event, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getId().equals(getFilterableValue());
  }

  @Override
  public String getFilterableValue() {
    return getEvent().getId();
  }

  private T getEvent() {
    return super.getFilterable();
  }
}
