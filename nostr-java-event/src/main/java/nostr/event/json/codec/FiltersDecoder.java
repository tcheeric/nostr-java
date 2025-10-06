package nostr.event.json.codec;

import static nostr.base.json.EventJsonMapper.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;
import nostr.base.IDecoder;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;

/**
 * @author eric
 */
@Data
public class FiltersDecoder implements IDecoder<Filters> {

  /**
   * Decodes a JSON string of filters into a {@link Filters} object.
   *
   * @param jsonFiltersList JSON representation of filters
   * @return decoded filters
   * @throws nostr.event.json.codec.EventEncodingException if decoding fails
   */
  @Override
  public Filters decode(@NonNull String jsonFiltersList) throws EventEncodingException {
    try {
      final List<Filterable> filterables = new ArrayList<>();

      Map<String, JsonNode> filtersMap =
          mapper().readValue(
              jsonFiltersList, new TypeReference<Map<String, JsonNode>>() {});

      for (Map.Entry<String, JsonNode> entry : filtersMap.entrySet()) {
        filterables.addAll(FilterableProvider.getFilterFunction(entry.getValue(), entry.getKey()));
      }

      return new Filters(filterables);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to decode filters", e);
    }
  }
}
