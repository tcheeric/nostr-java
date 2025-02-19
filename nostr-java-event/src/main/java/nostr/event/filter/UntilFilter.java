package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
        this.until >= genericEvent.getCreatedAt();
  }

  @Override
  public Long getFilterCriterion() {
    return until;
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return mapper.createObjectNode().put(filterKey, until);
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public String getFilterableValue() {
    return until.toString();
  }
}
