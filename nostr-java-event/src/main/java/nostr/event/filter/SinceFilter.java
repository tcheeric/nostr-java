package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class SinceFilter<T extends Long> implements Filterable {
  private final T since;

  public SinceFilter(T since) {
    this.since = since;
  }
  @Override
  public BiPredicate<T, GenericEvent> getBiPredicate() {
    return (since, genericEvent) -> (Long) since > genericEvent.getCreatedAt();
  }

  @Override
  public T getFilterCriterion() {
    return since;
  }

  @Override
  public <T> Function<String, T> createContainedInstance() {
    return longValue -> (T) Long.valueOf(longValue);
  }
}
