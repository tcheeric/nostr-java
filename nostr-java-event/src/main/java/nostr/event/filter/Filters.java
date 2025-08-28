package nostr.event.filter;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.IElement;

@Getter
@EqualsAndHashCode
@ToString
public class Filters implements IElement {
  public static final int DEFAULT_FILTERS_LIMIT = 10;
  private static final String FILTERS_EMPTY_ERROR = "Filters cannot be empty.";
  private static final String FILTER_KEY_ERROR = "Filter key for filterable [%s] is not defined";
  private static final String POSITIVE_LIMIT_ERROR = "Limit must be positive.";
  private final Map<String, List<Filterable>> filtersMap;

  private Integer limit = DEFAULT_FILTERS_LIMIT;

  public Filters(@NonNull Filterable... filterablesByDefaultType) {
    this(List.of(filterablesByDefaultType));
  }

  public Filters(@NonNull List<Filterable> filterablesByDefaultType) {
    this(filterablesByDefaultType.stream().collect(groupingBy(Filterable::getFilterKey)));
  }

  private Filters(@NonNull Map<String, List<Filterable>> filterablesByCustomType) {
    validateFiltersMap(filterablesByCustomType);
    this.filtersMap = filterablesByCustomType;
  }

  public List<Filterable> getFilterByType(@NonNull String type) {
    return filtersMap.getOrDefault(type, List.of());
  }

  public void setLimit(@NonNull Integer limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException(POSITIVE_LIMIT_ERROR);
    }
    this.limit = limit;
  }

  private static void validateFiltersMap(Map<String, List<Filterable>> filtersMap)
      throws IllegalArgumentException {
    if (filtersMap.isEmpty()) {
      throw new IllegalArgumentException(FILTERS_EMPTY_ERROR);
    }

    filtersMap
        .values()
        .forEach(
            filterables -> {
              if (filterables.isEmpty()) {
                throw new IllegalArgumentException(FILTERS_EMPTY_ERROR);
              }
            });

    filtersMap.forEach(
        (key, value) -> {
          String filterKey = Objects.requireNonNullElse(key, "");
          if (filterKey.isEmpty()) {
            throw new IllegalArgumentException(
                String.format(FILTER_KEY_ERROR, value.getFirst().getFilterKey()));
          }
        });
  }
}
