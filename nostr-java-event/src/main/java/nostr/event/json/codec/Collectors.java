package nostr.event.json.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Collectors {
  public static ArrayNodeCollector toArrayNode() {
    return new ArrayNodeCollector();
  }

  public static ObjectNodeCollector toObjectNode() {
    return new ObjectNodeCollector();
  }

  public static Stream<JsonNode> toStream(ArrayNode arrayNode) {
    return StreamSupport.stream(arrayNode.spliterator(), false);
  }
}
