package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static nostr.base.json.EventJsonMapper.mapper;

@EqualsAndHashCode(callSuper = true)
public class UntilFilter extends AbstractFilterable<Long> {
  public static final String FILTER_KEY = "until";

  public UntilFilter(Long until) {
    super(until, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) -> genericEvent.getCreatedAt() < getUntil();
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return mapper().createObjectNode().put(FILTER_KEY, getUntil());
  }

  @Override
  public String getFilterableValue() {
    return getUntil().toString();
  }

  private Long getUntil() {
    return super.getFilterable();
  }

  public static Function<JsonNode, List<Filterable>> fxn =
      node -> List.of(new UntilFilter(node.asLong()));
}
