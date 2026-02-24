package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nostr.event.tag.GenericTag;

import java.io.IOException;

import static nostr.event.json.codec.BaseTagEncoder.BASETAG_ENCODER_MAPPER_BLACKBIRD;

/**
 * Serializes a GenericTag as a JSON array: ["code", "param0", "param1", ...]
 */
public class GenericTagSerializer extends StdSerializer<GenericTag> {

  public GenericTagSerializer() {
    super(GenericTag.class);
  }

  @Override
  public void serialize(GenericTag value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    var arrayNode = BASETAG_ENCODER_MAPPER_BLACKBIRD.getNodeFactory().arrayNode();
    arrayNode.add(value.getCode());
    value.getParams().forEach(arrayNode::add);
    gen.writePOJO(arrayNode);
  }
}
