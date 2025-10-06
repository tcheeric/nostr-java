package nostr.event.json.codec;

import static nostr.base.json.EventJsonMapper.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.Nip05Content;

/**
 * @author eric
 */
@Data
public class Nip05ContentDecoder<T extends Nip05Content> implements IDecoder<T> {

  private final Class<T> clazz;

  @SuppressWarnings("unchecked")
  public Nip05ContentDecoder() {
    this.clazz = (Class<T>) Nip05Content.class;
  }

  /**
   * Decodes a JSON representation of NIP-05 content.
   *
   * @param jsonContent JSON content string
   * @return decoded content
   * @throws nostr.event.json.codec.EventEncodingException if decoding fails
   */
  @Override
  public T decode(String jsonContent) throws EventEncodingException {
    try {
      return mapper().readValue(jsonContent, clazz);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to decode nip05 content", ex);
    }
  }
}
