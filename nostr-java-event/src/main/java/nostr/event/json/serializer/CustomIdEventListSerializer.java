package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.impl.GenericEvent;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author guilhermegps
 */
public class CustomIdEventListSerializer<T extends List<U>, U extends GenericEvent> extends JsonSerializer<T> {

  @Override
  public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) {
    try {

      var list = value.stream().filter(Objects::nonNull).map(U::getId).toList();

      gen.writePOJO(list);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
