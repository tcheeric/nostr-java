package nostr.base;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import nostr.event.json.codec.EventEncodingException;

/**
 * @author eric
 * @param <T>
 */
public interface IDecoder<T extends IElement> {
  ObjectMapper I_DECODER_MAPPER_BLACKBIRD =
      JsonMapper.builder()
          .addModule(new BlackbirdModule())
          .build()
          .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

  /**
   * Decodes a JSON string into an element.
   *
   * @param str JSON string to decode
   * @return decoded element
   * @throws EventEncodingException if decoding fails
   */
  T decode(String str) throws EventEncodingException;
}
