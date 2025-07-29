package nostr.event.json.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 * @author eric
 */
@Data
public class FiltersDecoder implements FDecoder<Filters> {

  @SneakyThrows
  public Filters decode(@NonNull String jsonFiltersList) {
    final List<Filterable> filterables = new ArrayList<>();

    Map<String, JsonNode> filtersMap = MAPPER_AFTERBURNER.readValue(
        jsonFiltersList,
        new TypeReference<Map<String, JsonNode>>() {});

    for (Map.Entry<String, JsonNode> entry : filtersMap.entrySet()) {
      filterables.addAll(
          FilterableProvider.getFilterFunction(
              entry.getValue(),
              entry.getKey()));
    }

    return new Filters(filterables);
  }
}
