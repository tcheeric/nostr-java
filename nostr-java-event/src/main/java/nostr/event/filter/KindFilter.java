package nostr.event.filter;

import nostr.event.Kind;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

public class KindFilter<T extends Kind> implements Filterable {
  public final static String filterKey = "kinds";
  private final T kind;

  public KindFilter(T kind) {
    this.kind = kind;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getKind().equals(this.kind.getValue());
  }

  @Override
  public T getFilterCriterion() {
    return kind;
  }

  @Override
  public String toJson() {
    return kind.toString();
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
