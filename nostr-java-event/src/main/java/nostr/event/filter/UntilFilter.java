package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

public class UntilFilter implements Filterable {
  public final static String filterKey = "until";
  private final Long until;

  public UntilFilter(Long until) {
    this.until = until;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        this.until < genericEvent.getCreatedAt();
  }

  @Override
  public Long getFilterCriterion() {
    return until;
  }

  @Override
  public String toJson() {
    return until.toString();
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
