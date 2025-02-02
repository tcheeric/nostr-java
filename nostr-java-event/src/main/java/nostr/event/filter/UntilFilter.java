package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class UntilFilter<T extends Long> implements Filterable {
  private final T until;

  public UntilFilter(T until) {
    this.until = until;
  }
  @Override
  public BiPredicate<T, GenericEvent> getBiPredicate() {
    return (since, genericEvent) -> (Long) since < genericEvent.getCreatedAt();
  }
  @Override
  public T getFilterCriterion() {
    return until;
  }

  @Override
  public <T> Function<String, T> createContainedInstance() {
    return longValue -> (T) Long.valueOf(longValue);
  }
}
