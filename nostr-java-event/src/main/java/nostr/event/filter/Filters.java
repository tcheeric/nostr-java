package nostr.event.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode
public class Filters {
  @Getter
  private final Map<String, List<Filterable>> filtersMap;

  @Getter
  @Setter
  private Integer limit = 10;

  public Filters(Map<String, List<Filterable>> filtersMap) {
    this.filtersMap = filtersMap;
  }

//  TODO: unused
  public void addFilterable(@NonNull String key, @NonNull Filterable... filterable) {
    addFilterable(key, List.of(filterable));
  }

  public void addFilterable(@NonNull String key, @NonNull List<Filterable> filterable) {
    filtersMap.put(key, filterable);
  }

  public List<Filterable> getFilterableByType(@NonNull String type) {
    return filtersMap.get(type);
  }

  //  TODO: no tests currently call below...
//  public <T> List<T> getFilterCriterion(@NonNull String type) {
//    return Optional
//        .ofNullable(
//            getFilterableByType(type))
//        .stream().flatMap(filterables ->
//            filterables.stream().map(filterable ->
////  TODO: ...which leavesw below uncalled as well.  needs testing
//                (T) filterable.getFilterCriterion()))
//        .toList();
//  }

//  @SneakyThrows
//  public <T> void setFilterableListByType(
//      @NonNull String key,
//      @NonNull List<T> filterTypeList,
//      @NonNull Function<T, Filterable> filterableFunction) {
//
//    if (filterTypeList.isEmpty()) {
//      throw new IllegalArgumentException(
//          String.format("[%s] filter must contain at least one element", key));
//    }
//
//    addFilterable(
//        key,
//        filterTypeList.stream().map(filterableFunction).collect(Collectors.toList()));
}
