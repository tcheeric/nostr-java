package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author eric
 */
@Data
public class GenericEventDecoder<T extends GenericEvent> implements IDecoder<T> {

  private final Class<T> clazz;

  public GenericEventDecoder() {
    this.clazz = (Class<T>) GenericEvent.class;
  }

  public GenericEventDecoder(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T decode(String jsonEvent) throws JsonProcessingException {
    var mapper = new ObjectMapper();
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    return mapper.readValue(jsonEvent, clazz);
  }
}
