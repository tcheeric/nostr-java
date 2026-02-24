package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;

import java.io.IOException;

/**
 * Serializer for BaseTag. Delegates to GenericTagSerializer for GenericTag instances.
 */
public class BaseTagSerializer extends StdSerializer<BaseTag> {

  private static final GenericTagSerializer GENERIC_TAG_SERIALIZER = new GenericTagSerializer();

  public BaseTagSerializer() {
    super(BaseTag.class);
  }

  @Override
  public void serialize(BaseTag value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if (value instanceof GenericTag genericTag) {
      GENERIC_TAG_SERIALIZER.serialize(genericTag, gen, serializers);
    } else {
      throw new IOException("Unknown BaseTag subclass: " + value.getClass().getName());
    }
  }
}
