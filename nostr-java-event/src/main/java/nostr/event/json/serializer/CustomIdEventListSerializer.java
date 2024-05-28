package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.impl.GenericEvent;
import nostr.event.list.EventList;

import java.io.IOException;
import java.util.Objects;

/**
 * @author guilhermegps
 */
public class CustomIdEventListSerializer<T extends GenericEvent> extends JsonSerializer<EventList<T>> {

  @Override
  public void serialize(EventList<T> value, JsonGenerator gen, SerializerProvider serializers) {
    try {

      var list = value.getList().stream().filter(Objects::nonNull).map(T::getId).toList();

      gen.writePOJO(list);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
