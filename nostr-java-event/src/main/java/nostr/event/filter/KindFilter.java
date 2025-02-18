package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;
import java.util.function.Supplier;

@EqualsAndHashCode
public class KindFilter<T extends Kind> implements Filterable {
  public final static String filterKey = "kinds";
  private final T kind;

  public KindFilter(T kind) {
    this.kind = kind;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getKind().equals(this.kind.getValue());
  }

  @Override
  public T getFilterCriterion() {
    return kind;
  }

  @Override
  public ObjectNode toObjectNode(ObjectNode objectNode) {
    return processArrayNodeIntRxR(objectNode, () -> Integer.valueOf(getFilterableValue().toString()));
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public Integer getFilterableValue() {
    return kind.getValue();
  }
}
