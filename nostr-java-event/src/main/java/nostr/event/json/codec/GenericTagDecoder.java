package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.base.IDecoder;
import nostr.event.tag.GenericTag;

@Data
@Slf4j
public class GenericTagDecoder<T extends GenericTag> implements IDecoder<T> {

  private final Class<T> clazz;

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
   * @throws EventEncodingException if decoding fails
   */
  @Override
  @SuppressWarnings("unchecked")
  public T decode(@NonNull String json) throws EventEncodingException {
    try {
      String[] jsonElements = I_DECODER_MAPPER_BLACKBIRD.readValue(json, String[].class);
      GenericTag genericTag =
          new GenericTag(
              jsonElements[0],
              new ArrayList<>() {
                {
                  for (int i = 1; i < jsonElements.length; i++) {
                    ElementAttribute attribute =
                        new ElementAttribute("param" + (i - 1), jsonElements[i]);
                    if (!contains(attribute)) {
                      add(attribute);
                    }
                  }
                }
              });

      log.info("Decoded GenericTag: {}", genericTag);

      return (T) genericTag;
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to decode generic tag", ex);
    }
  }
}
