package nostr.base;

/**
 * Base interface for encoding Nostr protocol objects to JSON.
 *
 * <p>Implementations should use the centralized mappers in
 * {@code nostr.base.json.EventJsonMapper} or {@code nostr.event.json.EventJsonMapper}
 * rather than defining their own ObjectMapper instances.
 */
public interface Encoder {
  /**
   * Encodes this object to a JSON string representation.
   *
   * @return JSON string representation of this object
   * @throws nostr.event.json.codec.EventEncodingException if encoding fails
   */
  String encode();
}
