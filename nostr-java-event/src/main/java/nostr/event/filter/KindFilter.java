package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import nostr.base.Kind;
import nostr.event.impl.GenericEvent;

import java.util.function.Function;
import java.util.function.Predicate;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

@EqualsAndHashCode(callSuper = true)
public class KindFilter<T extends Kind> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "kinds";

  public KindFilter(T kind) {
    super(kind, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getKind().equals(getFilterableValue());
  }

  @Override
  public void addToArrayNode(ArrayNode arrayNode) {
    arrayNode.addAll(
        MAPPER_BLACKBIRD.createArrayNode().add(
            getFilterableValue()));
  }

  @Override
  public Integer getFilterableValue() {
    return getKind().getValue();
  }

  private T getKind() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn = node -> new KindFilter<>(Kind.valueOf(node.asInt()));
}
