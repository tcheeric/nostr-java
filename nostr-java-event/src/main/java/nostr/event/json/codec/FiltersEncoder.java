package nostr.event.json.codec;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.FEncoder;
import nostr.event.filter.Filters;

@Data
@EqualsAndHashCode(callSuper = false)
public class FiltersEncoder implements FEncoder<Filters> {
  private final Filters filters;

  public FiltersEncoder(Filters filters) {
    this.filters = filters;
  }

  @Override
  public String encode() {
    ObjectNode root = MAPPER.createObjectNode();

    filters.getFiltersMap().forEach((key, filterableList) -> {
      final ObjectNode objectNode = MAPPER.createObjectNode();
      root.setAll(
          filterableList
              .stream()
              .map(filterable ->
                  filterable.toObjectNode(objectNode))
              .toList()
              .getFirst());
    });

    return root.toString();
  }
}
