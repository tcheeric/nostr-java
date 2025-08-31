package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;

@EqualsAndHashCode(callSuper = true)
public class ReferencedEventFilter<T extends EventTag> extends AbstractFilterable<T> {
  public static final String FILTER_KEY = "#e";

  public ReferencedEventFilter(T referencedEventTag) {
    super(referencedEventTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        Filterable.getTypeSpecificTags(EventTag.class, genericEvent).stream()
            .anyMatch(eventTag -> eventTag.getIdEvent().equals(getFilterableValue()));
  }

  @Override
  public String getFilterableValue() {
    return getReferencedEventTag().getIdEvent();
  }

  private T getReferencedEventTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn =
      node -> new ReferencedEventFilter<>(new EventTag(node.asText()));
}
