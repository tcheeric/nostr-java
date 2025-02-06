package nostr.event.json.codec;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import nostr.base.FEncoder;
import nostr.event.filter.Filterable;
import nostr.event.filter.FiltersCore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guilhermegps
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FiltersEncoderRxR implements FEncoder<FiltersCore> {
  private final FiltersCore filtersCore;

  public FiltersEncoderRxR(FiltersCore filtersCore) {
    this.filtersCore = filtersCore;
  }

  @SneakyThrows
  @Override
  public String encode() {
    Map<String, List<String>> result = new HashMap<>();
    filtersCore.getFiltersMap().forEach((key, value) ->
        result.put(
            key,
            value.stream().map(
                Filterable::toJson).collect(Collectors.toList())));
    JsonNode jsonNode = MAPPER.valueToTree(result);
    return MAPPER.writeValueAsString(jsonNode);
  }
}
