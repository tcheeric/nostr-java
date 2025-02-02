package nostr.event.filter;

import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiltersCore {
  Map<String, List<Filterable>> filtersMap = new HashMap<>();

  public void addFilter(@NonNull String key, @NonNull Filterable filterable) {
    addFilterList(key, List.of(filterable));
  }

  public void addFilterList(@NonNull String key, @NonNull List<Filterable> filterable) {
    filtersMap.put(key, filterable);
  }

  public List<Filterable> getFilterableByType(String type) {
    return filtersMap.get(type);
  }
}
