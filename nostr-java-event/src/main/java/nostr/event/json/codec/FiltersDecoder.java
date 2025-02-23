package nostr.event.json.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
@Data
public class FiltersDecoder<T extends Filters> implements FDecoder<T> {
  private final static ObjectMapper mapper = new ObjectMapper();

  @SneakyThrows
  public T decode(@NonNull String jsonFiltersList) {
    final List<Filterable> filterables = new ArrayList<>();

    mapper.readTree(jsonFiltersList).fields().forEachRemaining(field ->
        filterables.addAll(
            FilterableProvider.getFilterable(
                field.getKey(),
                field.getValue())));

    return (T) new Filters(filterables);
  }
}
