package nostr.event.json.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import nostr.base.FEncoder;
import nostr.event.filter.Filters;

import java.util.ArrayList;
import java.util.List;

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
    List<ObjectNode> result = new ArrayList<>();

    filters.getFiltersMap().forEach((key, filterableList) -> {
      final ObjectNode objectNode = MAPPER.createObjectNode();
      ObjectNode list = filterableList.stream().map(filterable -> filterable.toObjectNode(objectNode)).toList().getFirst();
      result.add(list);
    });

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode root = mapper.createObjectNode();
    result.forEach(root::setAll);
    System.out.println(root.toPrettyString());
    return root.toString();
  }
}
