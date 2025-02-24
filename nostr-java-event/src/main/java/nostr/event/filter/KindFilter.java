package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

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
        mapper.createArrayNode().add(
            getFilterableValue()));
  }

  @Override
  public Integer getFilterableValue() {
    return getKind().getValue();
  }

  private T getKind() {
    return super.getFilterable();
  }
}
