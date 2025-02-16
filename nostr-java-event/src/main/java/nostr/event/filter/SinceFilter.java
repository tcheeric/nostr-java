package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

@EqualsAndHashCode
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
  public ArrayNode toArrayNode() {
    return mapper.createArrayNode().add(since);
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
