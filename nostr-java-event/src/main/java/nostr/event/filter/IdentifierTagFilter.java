package nostr.event.filter;

import nostr.event.impl.GenericEvent;
import nostr.event.tag.IdentifierTag;

import java.util.function.Function;
import java.util.function.Predicate;

public class IdentifierTagFilter<T extends IdentifierTag> implements Filterable {
  public final static String filterKey = "#d";
  private final T identifierTag;

  public IdentifierTagFilter(T identifierTag) {
    this.identifierTag = identifierTag;
  }
  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(IdentifierTag.class, genericEvent).stream().anyMatch(genericEventIdentifiterTag ->
            genericEventIdentifiterTag.getId().equals(this.identifierTag.getId()));
  }
  @Override
  public T getFilterCriterion() {
    return identifierTag;
  }

  @Override
  public <T> Function<String, T> createContainedInstance() {
    return identifier -> {
      IdentifierTag identiferTag = new IdentifierTag(identifier);
      return (T) identiferTag;
    };
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
