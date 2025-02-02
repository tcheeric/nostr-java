package nostr.event.filter;

import nostr.event.Kind;
import nostr.event.impl.GenericEvent;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class KindFilter<T extends Kind> implements Filterable {
  private final T kind;

  public KindFilter(T kind) {
    this.kind = kind;
  }
  @Override
  public BiPredicate<T, GenericEvent> getBiPredicate() {
    return (publicKey, genericEvent) -> Kind.valueOf(genericEvent.getKind()).equals(kind);
  }
  @Override
  public T getFilterCriterion() {
    return kind;
  }

  @Override
  public <T> Function<String, T> createContainedInstance() {
    return pubkey -> (T) Kind.valueOf(pubkey);
  }
}
