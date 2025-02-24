package nostr.event.filter;

import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;

import java.util.function.Predicate;

@EqualsAndHashCode
public class ReferencedEventFilter<T extends EventTag> implements Filterable {
  public final static String filterKey = "#e";
  private final T referencedEventTag;

  public ReferencedEventFilter(T referencedEventTag) {
    this.referencedEventTag = referencedEventTag;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(EventTag.class, genericEvent).stream()
            .anyMatch(eventTag ->
                eventTag.getIdEvent().equals(referencedEventTag.getIdEvent()));
  }

  @Override
  public T getFilterCriterion() {
    return referencedEventTag;
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public String getFilterableValue() {
    return referencedEventTag.getIdEvent();
  }
}
