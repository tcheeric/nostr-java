package nostr.event.filter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class FiltersCore {
  private final Map<String, List<Filterable>> filtersMap = new HashMap<>();

//  TODO: make limit configurable
  @Setter
  private Integer limit = 10;

  public void addFilterable(@NonNull String key, @NonNull Filterable filterable) {
    addFilterable(key, List.of(filterable));
  }

  public void addFilterable(@NonNull String key, @NonNull List<Filterable> filterable) {
    filtersMap.put(key, filterable);
  }

  public List<Filterable> getFilterableByType(@NonNull String type) {
    return filtersMap.get(type);
  }
}
