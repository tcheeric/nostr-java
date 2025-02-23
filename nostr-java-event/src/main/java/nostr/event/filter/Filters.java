package nostr.event.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;

@EqualsAndHashCode
public class Filters {
  public static final int DEFAULT_FILTERS_LIMIT = 10;
  @Getter
  private final Map<String, List<Filterable>> filtersMap;

  @Getter
  @Setter
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
    return filtersMap.get(type);
  }

  private static void validateFiltersMap(Map<String, List<Filterable>> filtersMap) throws IllegalArgumentException {
    if (filtersMap.isEmpty()) {
      throw new IllegalArgumentException("Filters cannot be empty.");
    }

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
