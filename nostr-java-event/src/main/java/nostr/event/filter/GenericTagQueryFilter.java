package nostr.event.filter;

import lombok.EqualsAndHashCode;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class GenericTagQueryFilter<T extends GenericTagQuery> extends AbstractFilterable<T> {
  public static final String HASH_PREFIX = "#";

  public GenericTagQueryFilter(T genericTagQuery) {
    super(genericTagQuery, genericTagQuery.getTagName());
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(GenericTag.class, genericEvent).stream()
            .filter(genericTag ->
                genericTag.getCode().equals(stripLeadingHashTag()))
            .anyMatch(genericTag ->
                genericTag
                    .getAttributes().stream().map(
                        ElementAttribute::getValue).toList()
                    .contains(
                        getFilterableValue()));
  }

  @Override
  public String getFilterKey() {
    return getGenericTagQuery().getTagName();
  }

  @Override
  public String getFilterableValue() {
    return getGenericTagQuery().getValue();
  }

  private T getGenericTagQuery() {
    return super.getFilterable();
  }

  private String stripLeadingHashTag() {
    return getFilterKey().startsWith(HASH_PREFIX) ?
        getFilterKey().substring(1) :
        getFilterKey();
  }
}
