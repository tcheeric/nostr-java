package nostr.event.json.codec;

import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;

import java.util.ArrayList;
import java.util.List;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 * @author eric
 */
@Data
public class FiltersDecoder implements FDecoder<Filters> {

  @SneakyThrows
  public Filters decode(@NonNull String jsonFiltersList) {
    final List<Filterable> filterables = new ArrayList<>();

    MAPPER_AFTERBURNER.readTree(jsonFiltersList).fields().forEachRemaining(node ->
        filterables.addAll(
            FilterableProvider.getFilterFunction(
                node.getValue(),
                node.getKey())));

    return new Filters(filterables);
  }
}
