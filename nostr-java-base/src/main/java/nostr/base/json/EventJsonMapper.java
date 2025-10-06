package nostr.base.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

/** Utility holder for the default Jackson mapper used across Nostr events. */
public final class EventJsonMapper {

  private EventJsonMapper() {}

  /**
   * Obtain the shared {@link ObjectMapper} configured for event serialization and deserialization.
   *
   * @return lazily initialized mapper instance
   */
  public static ObjectMapper mapper() {
    return MapperHolder.INSTANCE;
  }

  private static final class MapperHolder {
    private static final ObjectMapper INSTANCE =
        JsonMapper.builder().addModule(new BlackbirdModule()).build();

    private MapperHolder() {}
  }
}
