package nostr.event.json.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import nostr.base.FEncoder;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class FiltersEncoder implements FEncoder<Filters> {
  private final Filters filters;

  public FiltersEncoder(Filters filters) {
    this.filters = filters;
  }

  @SneakyThrows
  @Override
  public String encode() {
    Map<String, ArrayNode> result = new HashMap<>();
    filters.getFiltersMap().forEach((key, value) ->
        value.stream().distinct()
            .map(Filterable::toArrayNode)
            .reduce(ArrayNode::addAll)
            .ifPresent(arrayNode ->
                result.put(key, arrayNode)));
    JsonNode jsonNode = MAPPER.valueToTree(result);
    return MAPPER.writeValueAsString(jsonNode);
  }
}
