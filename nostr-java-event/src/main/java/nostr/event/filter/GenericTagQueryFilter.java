package nostr.event.filter;

import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;

public class GenericTagQueryFilter<T extends GenericTagQuery> implements Filterable {
  public final static String filterKey = "undefined";
  private final T genericTagQuery;

  public GenericTagQueryFilter(T genericTagQuery) {
    this.genericTagQuery = genericTagQuery;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(GenericTag.class, genericEvent).stream()
            .filter(genericTag ->
                genericTag.getCode().equals(this.genericTagQuery.getTagName()))
            .anyMatch(genericTag ->
                new HashSet<>(genericTag
                    .getAttributes().stream().map(
                        ElementAttribute::getValue).toList())
                    .containsAll(
                        this.genericTagQuery.getValue()));
  }

  @Override
  public T getFilterCriterion() {
    return genericTagQuery;
  }

  @Override
  public <T> Function<String, T> createContainedInstance() {
    return tagName -> {
      GenericTagQuery tagQuery = new GenericTagQuery();
      tagQuery.setTagName(tagName);
      return (T) tagQuery;
    };
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
