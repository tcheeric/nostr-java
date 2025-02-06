package nostr.event.filter;

import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

public class SinceFilter implements Filterable {
  public final static String filterKey = "since";
  private final Long since;

  public SinceFilter(Long since) {
    this.since = since;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        this.since >= genericEvent.getCreatedAt();
  }

  @Override
  public Long getFilterCriterion() {
    return since;
  }

  @Override
  public String toJson() {
    return since.toString();
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
