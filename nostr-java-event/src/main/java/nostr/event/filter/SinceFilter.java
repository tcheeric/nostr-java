package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
        this.since < genericEvent.getCreatedAt();
  }

  @Override
  public Long getFilterCriterion() {
    return since;
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return mapper.createObjectNode().put(filterKey, since);
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public String getFilterableValue() {
    return since.toString();
  }
}
