package nostr.event.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode
public abstract class AbstractFilterable<T> implements Filterable {
  private final T filterable;
  private final String filterKey;

  protected AbstractFilterable(@NonNull T filterable, @NonNull String filterKey) {
    this.filterable = filterable;
    this.filterKey = filterKey;
  }
}
