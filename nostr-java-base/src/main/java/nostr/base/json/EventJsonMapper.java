package nostr.base.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Provides access to the shared {@link ObjectMapper} configured for canonical Nostr event
 * serialization and deserialization.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventJsonMapper {

  private static final ObjectMapper MAPPER =
      JsonMapper.builder().addModule(new BlackbirdModule()).build();

  /** Returns the shared {@link ObjectMapper} instance for Nostr events. */
  public static ObjectMapper mapper() {
    return MAPPER;
  }
}
