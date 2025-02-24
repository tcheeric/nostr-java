package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class SinceFilter extends AbstractFilterable<Long> {
  public final static String FILTER_KEY = "since";

  public SinceFilter(Long since) {
    super(since, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getCreatedAt() > getSince();
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return mapper.createObjectNode().put(FILTER_KEY, getSince());
  }

  @Override
  public String getFilterableValue() {
    return getSince().toString();
  }

  private Long getSince() {
    return super.getFilterable();
  }
}
