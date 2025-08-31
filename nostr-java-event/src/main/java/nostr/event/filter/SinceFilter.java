package nostr.event.filter;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

@EqualsAndHashCode(callSuper = true)
public class SinceFilter extends AbstractFilterable<Long> {
  public static final String FILTER_KEY = "since";

  public SinceFilter(Long since) {
    super(since, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) -> genericEvent.getCreatedAt() > getSince();
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return MAPPER_BLACKBIRD.createObjectNode().put(FILTER_KEY, getSince());
  }

  @Override
  public String getFilterableValue() {
    return getSince().toString();
  }

  private Long getSince() {
    return super.getFilterable();
  }

  public static Function<JsonNode, List<Filterable>> fxn =
      node -> List.of(new SinceFilter(node.asLong()));
}
