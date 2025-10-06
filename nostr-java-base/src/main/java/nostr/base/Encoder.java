package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

/**
 * Base interface for encoding Nostr protocol objects to JSON.
 *
 * <p><b>Note:</b> The static ObjectMapper field in this interface is deprecated.
 * Use {@code nostr.event.json.EventJsonMapper} instead for all JSON serialization needs.
 *
 * @see nostr.event.json.EventJsonMapper
 */
public interface Encoder {
  /**
   * @deprecated Use {@link nostr.event.json.EventJsonMapper#getMapper()} instead.
   *             This field will be removed in version 1.0.0.
   */
  @Deprecated(forRemoval = true, since = "0.6.2")
  ObjectMapper ENCODER_MAPPER_BLACKBIRD =
      JsonMapper.builder()
          .addModule(new BlackbirdModule())
          .build()
          .setSerializationInclusion(Include.NON_NULL);

  /**
   * Encodes this object to a JSON string representation.
   *
   * @return JSON string representation of this object
   * @throws nostr.event.json.codec.EventEncodingException if encoding fails
   */
  String encode();
}
