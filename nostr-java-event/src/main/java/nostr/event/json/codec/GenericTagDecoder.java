package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.base.IDecoder;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;

@Data
@Slf4j
public class GenericTagDecoder<T extends GenericTag> implements IDecoder<T> {

  private final Class<T> clazz;

  // Generics are erased at runtime; safe cast because decoder always produces the requested class
  @SuppressWarnings("unchecked")
  public GenericTagDecoder() {
    this((Class<T>) GenericTag.class);
  }

  public GenericTagDecoder(@NonNull Class<T> clazz) {
    this.clazz = clazz;
  }

  /**
   * Decodes a JSON array into a {@link GenericTag} instance.
   *
   * @param json JSON array string representing the tag
   * @return decoded tag
   * @throws nostr.event.json.codec.EventEncodingException if decoding fails
   */
  @Override
  // Generics are erased at runtime; safe cast because the created GenericTag matches T by contract
  @SuppressWarnings("unchecked")
  public T decode(@NonNull String json) throws EventEncodingException {
    try {
      String[] jsonElements = I_DECODER_MAPPER_BLACKBIRD.readValue(json, String[].class);
      var attributes = new ArrayList<ElementAttribute>(Math.max(0, jsonElements.length - 1));
      for (int i = 1; i < jsonElements.length; i++) {
        ElementAttribute attribute = new ElementAttribute("param" + (i - 1), jsonElements[i]);
        if (!attributes.contains(attribute)) {
          attributes.add(attribute);
        }
      }
      GenericTag genericTag = new GenericTag(jsonElements[0], attributes);

      log.debug("Decoded GenericTag: {}", genericTag);

      return (T) genericTag;
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to decode generic tag", ex);
    }
  }
}
