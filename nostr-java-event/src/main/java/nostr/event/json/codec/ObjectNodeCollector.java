package nostr.event.json.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ObjectNodeCollector implements Collector<JsonNode, ObjectNode, ObjectNode> {
  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Supplier<ObjectNode> supplier() {
    return mapper::createObjectNode;
  }

  @Override
  public BiConsumer<ObjectNode, JsonNode> accumulator() {
    return (objectNode, jsonNode) -> {
      objectNode.set("someval", jsonNode);
    };
  }

  @Override
  public BinaryOperator<ObjectNode> combiner() {
//    ArrayNode::addAll;
    return ObjectNode::setAll;
  }

  @Override
  public Function<ObjectNode, ObjectNode> finisher() {
    return accumulator -> accumulator;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return EnumSet.of(Characteristics.UNORDERED);
  }
}
