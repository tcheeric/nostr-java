package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class UntilFilter extends AbstractFilterable<Long> {
  public final static String FILTER_KEY = "until";

  public UntilFilter(Long until) {
    super(until, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getCreatedAt() < getUntil();
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return mapper.createObjectNode().put(FILTER_KEY, getUntil());
  }

  @Override
  public String getFilterableValue() {
    return getUntil().toString();
  }

  private Long getUntil() {
    return super.getFilterable();
  }
}
