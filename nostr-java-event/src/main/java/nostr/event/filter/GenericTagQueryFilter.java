package nostr.event.filter;

import lombok.NonNull;
import nostr.base.GenericTagQuery;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class GenericTagQueryFilter<T extends GenericTagQuery> implements Filterable {
  private final T genericTagQuery;

  public GenericTagQueryFilter(@NonNull T genericTagQuery) {
    this.genericTagQuery = genericTagQuery;
  }
  @Override
  public BiPredicate<T, GenericTagQuery> getBiPredicate() {
    return (genericTagQueryA, genericTagQueryB) -> genericTagQueryA.getTagName().equals(genericTagQueryB.getTagName());
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
}
