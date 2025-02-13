package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Filters {
  @Getter
  private final Map<String, List<Filterable>> filtersMap;

  //  TODO: make limit configurable
  @Setter
  private Integer limit = 10;

  public Filters(Map<String, List<Filterable>> filtersMap) {
    this.filtersMap = filtersMap;
  }

  public void addFilterable(@NonNull String key, @NonNull Filterable... filterable) {
    addFilterable(key, List.of(filterable));
  }

  public void addFilterable(@NonNull String key, @NonNull List<Filterable> filterable) {
    filtersMap.put(key, filterable);
  }

  public List<Filterable> getFilterableByType(@NonNull String type) {
    return filtersMap.get(type);
  }

  public <T> List<T> getFilterCriterion(@NonNull String type) {
    return Optional
        .ofNullable(
            getFilterableByType(type))
        .stream().flatMap(filterables ->
            filterables.stream().map(filterable ->
                (T) filterable.getFilterCriterion()))
        .toList();
  }

  @SneakyThrows
  public <T> void setFilterableListByType(
      @NonNull String key,
      @NonNull List<T> filterTypeList,
      @NonNull Function<T, Filterable> filterableFunction) {

    if (filterTypeList.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("[%s] filter must contain at least one element", key));
    }

    addFilterable(
        key,
        filterTypeList.stream().map(filterableFunction).collect(Collectors.toList()));
//        .orElseThrow(() ->
//            new IllegalArgumentException(
//                String.format("[%s] filter must contain at least one element")))
  }

  public static List<Filterable> getFilterable(JsonNode jsonNode, Function<JsonNode, Filterable> filterFunction) {
    return StreamSupport.stream(jsonNode.spliterator(), false).map(filterFunction).toList();
  }

  public Optional<Long> getSinceOrUntil(String type) {
    return Optional
        .ofNullable(
            getFilterableByType(type))
        .map(filterables -> filterables
            .getFirst().getFilterCriterion());
  }
}
