package nostr.event.json.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.filter.Filterable;
import nostr.event.filter.FilterableProvider;
import nostr.event.filter.Filters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
@Data
public class FiltersDecoder<T extends Filters> implements FDecoder<T> {
  private final static ObjectMapper mapper = new ObjectMapper();

  @SneakyThrows
  public T decode(@NonNull String jsonFiltersList) {
    final Map<String, List<Filterable>> filterPluginsMap = new HashMap<>();

    mapper.readTree(jsonFiltersList).fields().forEachRemaining(field ->
        filterPluginsMap.put(
            field.getKey(),
            FilterableProvider.getFilterable(
                field.getKey(),
                field.getValue())));

    return (T) new Filters(filterPluginsMap);
  }
}
