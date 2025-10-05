package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

@EqualsAndHashCode(callSuper = true)
public class EventFilter<T extends GenericEvent> extends AbstractFilterable<T> {
  public static final String FILTER_KEY = "ids";

  public EventFilter(T event) {
    super(event, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) -> genericEvent.getId().equals(getFilterableValue());
  }

  @Override
  public String getFilterableValue() {
    return getEvent().getId();
  }

  private T getEvent() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn =
      node -> new EventFilter<>(new GenericEvent(node.asText()));
}
