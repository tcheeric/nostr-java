package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.impl.GenericEvent;

/**
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

  /**
   * Decodes a JSON string into a {@link GenericEvent} instance.
   *
   * @param jsonEvent JSON representation of the event
   * @return decoded event
   * @throws nostr.event.json.codec.EventEncodingException if decoding fails
   */
  @Override
  public T decode(String jsonEvent) throws EventEncodingException {
    try {
      I_DECODER_MAPPER_BLACKBIRD.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
      return I_DECODER_MAPPER_BLACKBIRD.readValue(jsonEvent, clazz);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to decode generic event", e);
    }
  }
}
