package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

@EqualsAndHashCode
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
  public ArrayNode toArrayNode() {
    return mapper.createArrayNode().add(until);
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
