package nostr.event.filter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.annotation.Key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FiltersCore {
  @Key
  private final Map<String, List<Filterable>> filtersMap;

  //  TODO: make limit configurable
  @Setter
  private Integer limit = 10;

  public FiltersCore() {
    filtersMap = new HashMap<>();
  }

  public FiltersCore(Map<String, List<Filterable>> filtersMap) {
    this.filtersMap = filtersMap;
  }

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

