package nostr.event.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

/**
 * Provides a centralized JSON ObjectMapper for event serialization and deserialization.
 *
 * <p>This utility class uses Jackson's Blackbird module for improved performance
 * and is configured with NON_NULL serialization inclusion to minimize JSON output size.
 *
 * <p><b>Why Blackbird?</b> The Blackbird module provides optimized bytecode generation
 * for getters/setters, resulting in significantly faster serialization/deserialization
 * compared to reflection-based approaches.
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>Blackbird module enabled for performance</li>
 *   <li>NON_NULL serialization - null fields are omitted from JSON output</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> ObjectMapper instances are thread-safe and can be
 * shared across the application.
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * ObjectMapper mapper = EventJsonMapper.getMapper();
 * String json = mapper.writeValueAsString(event);
 * GenericEvent event = mapper.readValue(json, GenericEvent.class);
 * }</pre>
 *
 * @see ObjectMapper
 * @see BlackbirdModule
 */
public final class EventJsonMapper {

  /**
   * Singleton ObjectMapper instance with Blackbird optimization.
   */
  private static final ObjectMapper MAPPER =
      JsonMapper.builder()
          .addModule(new BlackbirdModule())
          .build()
          .setSerializationInclusion(Include.NON_NULL);

  /**
   * Private constructor to prevent instantiation.
   */
  private EventJsonMapper() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Returns the shared ObjectMapper instance.
   *
   * <p>This mapper is optimized with the Blackbird module and configured
   * to exclude null values from JSON output.
   *
   * @return thread-safe ObjectMapper instance
   */
  public static ObjectMapper getMapper() {
    return MAPPER;
  }

  /**
   * Creates a configured ObjectMapper with custom Blackbird settings.
   *
   * <p>Use this method when you need a mapper with additional custom configuration
   * beyond the default settings.
   *
   * @return new ObjectMapper instance with Blackbird module
   */
  public static ObjectMapper createCustomMapper() {
    return JsonMapper.builder()
        .addModule(new BlackbirdModule())
        .build();
  }
}
