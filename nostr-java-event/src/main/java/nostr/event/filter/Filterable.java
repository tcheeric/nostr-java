package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.BiPredicate;
import java.util.function.Function;

public interface Filterable {
  <T> BiPredicate<T, GenericEvent> getBiPredicate();
  <T> T getFilterCriterion();
  <T> Function<String, T> createContainedInstance();
}
