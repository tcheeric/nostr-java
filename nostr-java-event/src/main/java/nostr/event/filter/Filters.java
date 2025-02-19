package nostr.event.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode
public class Filters {
  public static final int DEFAULT_FILTERS_LIMIT = 10;
  @Getter
  private final Map<String, List<Filterable>> filtersMap;

  @Getter
  @Setter
  private Integer limit = DEFAULT_FILTERS_LIMIT;

  public Filters(@NonNull Map<String, List<Filterable>> filtersMap) {
    validateFiltersMap(filtersMap);
    this.filtersMap = filtersMap;
  }

  public List<Filterable> getFilterableByType(@NonNull String type) {
    return filtersMap.get(type);
  }

  private static void validateFiltersMap(Map<String, List<Filterable>> filtersMap) throws IllegalArgumentException {
    filtersMap.values().forEach(filterables -> {
      if (filterables.isEmpty()) {
        throw new IllegalArgumentException("Filters cannot be empty.");
      }
    });

    filtersMap.forEach((key, value) -> {
      if (key.isEmpty())
        throw new IllegalArgumentException(String.format("Filter key for filterable [%s] is not defined", value.getFirst().getFilterKey()));
    });
  }
}
