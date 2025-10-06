package nostr.event.json.codec;

import static nostr.base.json.EventJsonMapper.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.BaseTag;

/**
 * @author eric
 */
@Data
public class BaseTagDecoder<T extends BaseTag> implements IDecoder<T> {

  private final Class<T> clazz;

  // Generics are erased at runtime; BaseTag.class is the default concrete target for decoding
  @SuppressWarnings("unchecked")
  public BaseTagDecoder() {
    this.clazz = (Class<T>) BaseTag.class;
  }

  /**
   * Decodes the provided JSON string into a tag instance.
   *
   * @param jsonString JSON representation of the tag
   * @return decoded tag
   * @throws nostr.event.json.codec.EventEncodingException if decoding fails
   */
  @Override
  public T decode(String jsonString) throws EventEncodingException {
    try {
      return mapper().readValue(jsonString, clazz);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to decode tag", ex);
    }
  }
}
