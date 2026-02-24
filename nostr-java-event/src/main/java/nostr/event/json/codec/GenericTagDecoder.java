package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.IDecoder;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.List;

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

  @Override
  @SuppressWarnings("unchecked")
  public T decode(@NonNull String json) throws EventEncodingException {
    try {
      String[] jsonElements = I_DECODER_MAPPER_BLACKBIRD.readValue(json, String[].class);
      List<String> params = new ArrayList<>(Math.max(0, jsonElements.length - 1));
      for (int i = 1; i < jsonElements.length; i++) {
        params.add(jsonElements[i]);
      }
      GenericTag genericTag = new GenericTag(jsonElements[0], params);

      log.debug("Decoded GenericTag: {}", genericTag);

      return (T) genericTag;
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to decode generic tag", ex);
    }
  }
}
