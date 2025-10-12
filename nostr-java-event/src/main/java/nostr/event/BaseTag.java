package nostr.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.ElementAttribute;
import nostr.base.IEvent;
import nostr.base.ITag;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.json.deserializer.TagDeserializer;
import nostr.event.json.serializer.BaseTagSerializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.TagRegistry;
import org.apache.commons.lang3.stream.Streams;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Base class for all Nostr event tags.
 *
 * <p>Tags are metadata elements attached to Nostr events, defined as arrays in the NIP-01
 * specification. Each tag has a code (the first element) followed by one or more parameters.
 * This class provides the foundation for all tag implementations in the library.
 *
 * <p><b>Tag Structure:</b>
 * <pre>{@code
 * // JSON representation
 * ["e", "event_id", "relay_url", "marker"]
 *  ^     ^            ^            ^
 *  |     |            |            |
 * code  param0       param1      param2
 * }</pre>
 *
 * <p><b>Common Tag Types:</b>
 * <ul>
 *   <li><b>e tag:</b> References another event (EventTag)</li>
 *   <li><b>p tag:</b> References a user's public key (PubKeyTag)</li>
 *   <li><b>a tag:</b> References an addressable event (AddressTag)</li>
 *   <li><b>d tag:</b> Identifier for addressable events (IdentifierTag)</li>
 *   <li><b>t tag:</b> Hashtags (HashtagTag)</li>
 *   <li><b>r tag:</b> References a URL (ReferenceTag)</li>
 *   <li><b>Custom tags:</b> GenericTag for unknown tag codes</li>
 * </ul>
 *
 * <p><b>Tag Creation:</b>
 * <pre>{@code
 * // Method 1: Using specific tag classes
 * EventTag eventTag = EventTag.builder()
 *     .idEvent("event_id_hex")
 *     .recommendedRelayUrl("wss://relay.example.com")
 *     .marker("reply")
 *     .build();
 *
 * // Method 2: Using factory method
 * BaseTag tag = BaseTag.create("e", "event_id_hex", "relay_url", "reply");
 *
 * // Method 3: Using GenericTag for custom/unknown tags
 * GenericTag customTag = new GenericTag("customcode", List.of(
 *     new ElementAttribute("param0", "value1"),
 *     new ElementAttribute("param1", "value2")
 * ));
 * }</pre>
 *
 * <p><b>Tag Registry:</b> The library maintains a {@link TagRegistry} that maps tag codes
 * to their corresponding classes. When deserializing events, the registry is consulted to
 * create the appropriate tag type. Unknown tag codes are deserialized as {@link GenericTag}.
 *
 * <p><b>Design Patterns:</b>
 * <ul>
 *   <li><b>Factory Pattern:</b> {@code create()} methods provide flexible tag creation</li>
 *   <li><b>Registry Pattern:</b> {@link TagRegistry} maps codes to tag classes</li>
 *   <li><b>Template Method:</b> Subclasses define specific tag fields and behavior</li>
 * </ul>
 *
 * <p><b>Serialization:</b> Tags are automatically serialized/deserialized using Jackson
 * with custom {@link BaseTagSerializer} and {@link TagDeserializer}. The serialization
 * preserves the tag code and parameter order required by NIP-01.
 *
 * <p><b>Reflection API:</b> This class provides reflection-based methods for accessing
 * tag fields dynamically:
 * <ul>
 *   <li>{@link #getCode()} - Returns the tag code from {@code @Tag} annotation</li>
 *   <li>{@link #getSupportedFields()} - Returns fields annotated with {@code @Key}</li>
 *   <li>{@link #getFieldValue(Field)} - Gets field value using reflection</li>
 * </ul>
 *
 * <p><b>Example - Custom Tag Implementation:</b>
 * <pre>{@code
 * @Tag(code = "mycustom", name = "My Custom Tag")
 * @Data
 * @EqualsAndHashCode(callSuper = false)
 * public class MyCustomTag extends BaseTag {
 *
 *   @Key(order = 0)
 *   private String parameter1;
 *
 *   @Key(order = 1)
 *   private String parameter2;
 *
 *   // Builder pattern provided by Lombok @Data
 * }
 *
 * // Register the tag
 * TagRegistry.register("mycustom", genericTag -> {
 *   // Convert GenericTag to MyCustomTag
 *   return MyCustomTag.builder()
 *       .parameter1(genericTag.getAttributes().get(0).getValue())
 *       .parameter2(genericTag.getAttributes().get(1).getValue())
 *       .build();
 * });
 * }</pre>
 *
 * <p><b>Thread Safety:</b> Tag instances are immutable after creation (due to Lombok
 * {@code @Data} generating only getters for final fields). The {@code setParent()} method
 * intentionally does nothing to avoid retaining parent event references.
 *
 * @see ITag
 * @see GenericTag
 * @see TagRegistry
 * @see nostr.base.annotation.Tag
 * @see nostr.base.annotation.Key
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01 - Tags</a>
 * @since 0.1.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = TagDeserializer.class)
@JsonSerialize(using = BaseTagSerializer.class)
public abstract class BaseTag implements ITag {

  /**
   * Sets the parent event for this tag.
   *
   * <p><b>Implementation Note:</b> This method intentionally does nothing. Parent references
   * are not retained to avoid circular references and memory issues. Tags are value objects
   * that should not hold references to their containing events.
   *
   * @param event the parent event (ignored)
   */
  @Override
  public void setParent(IEvent event) {
    // Intentionally left blank to avoid retaining parent references.
  }

  /**
   * Returns the tag code as defined in the {@code @Tag} annotation.
   *
   * <p>The tag code is the first element in the tag array and identifies the tag type.
   * For example, "e" for event references, "p" for public key references, etc.
   *
   * @return tag code string (e.g., "e", "p", "a", "d", "t", etc.)
   */
  @Override
  public String getCode() {
    return this.getClass().getAnnotation(Tag.class).code();
  }

  /**
   * Gets the value of a field using reflection.
   *
   * <p>This method uses Java Beans introspection to read the field value through its getter
   * method. If the field cannot be read (no getter, access denied, etc.), an empty Optional
   * is returned.
   *
   * @param field the field to read
   * @return Optional containing the field value as a String, or empty if unavailable
   */
  public Optional<String> getFieldValue(Field field) {
    try {
      return Optional.ofNullable(
              new PropertyDescriptor(field.getName(), this.getClass()).getReadMethod().invoke(this))
          .map(Object::toString);
    } catch (IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | IntrospectionException ex) {
      return Optional.empty();
    }
  }

  /**
   * Returns all fields that are annotated with {@code @Key} and have non-null values.
   *
   * <p>This method is used during serialization to determine which tag parameters should
   * be included in the JSON array. Only fields marked with {@code @Key} annotation are
   * considered, and only those with present values are returned.
   *
   * @return list of fields with {@code @Key} annotation that have values
   */
  public List<Field> getSupportedFields() {
    return Streams.failableStream(Arrays.stream(this.getClass().getDeclaredFields()))
        .filter(f -> Objects.nonNull(f.getAnnotation(Key.class)))
        .filter(f -> getFieldValue(f).isPresent())
        .collect(Collectors.toList());
  }

  /**
   * Factory method to create a tag from a code and variable parameters.
   *
   * <p>This is a convenience method that delegates to {@link #create(String, List)}.
   *
   * @param code tag code (e.g., "e", "p", "a")
   * @param params tag parameters
   * @return BaseTag instance (specific type if registered, GenericTag otherwise)
   * @see #create(String, List)
   */
  public static BaseTag create(@NonNull String code, @NonNull String... params) {
    return create(code, List.of(params));
  }

  /**
   * Factory method to create a tag from a code and parameter list.
   *
   * <p>This method consults the {@link TagRegistry} to determine if a specific tag class
   * is registered for the given code. If found, it creates an instance of that class.
   * Otherwise, it returns a {@link GenericTag}.
   *
   * <p><b>Example:</b>
   * <pre>{@code
   * // Creates an EventTag (registered for "e" code)
   * BaseTag eventTag = BaseTag.create("e", List.of("event_id", "relay_url"));
   *
   * // Creates a GenericTag (no registration for "custom" code)
   * BaseTag customTag = BaseTag.create("custom", List.of("param1", "param2"));
   * }</pre>
   *
   * @param code tag code (e.g., "e", "p", "a")
   * @param params list of tag parameters
   * @return BaseTag instance (specific type if registered, GenericTag otherwise)
   */
  public static BaseTag create(@NonNull String code, @NonNull List<String> params) {
    GenericTag genericTag =
        new GenericTag(
            code,
            IntStream.range(0, params.size())
                .mapToObj(
                    i -> new ElementAttribute("param".concat(String.valueOf(i)), params.get(i)))
                .toList());

    return Optional.ofNullable(TagRegistry.get(code))
        .map(f -> (BaseTag) f.apply(genericTag))
        .orElse(genericTag);
  }

  /**
   * Helper method for deserializers to set optional tag fields.
   *
   * <p>If the JsonNode is null or missing, no action is taken. This is used in custom
   * deserializers to populate tag fields from JSON without throwing exceptions for
   * missing optional parameters.
   *
   * @param <T> the tag type
   * @param node the JSON node (may be null)
   * @param con consumer that sets the field value
   * @param tag the tag instance to populate
   */
  protected static <T extends BaseTag> void setOptionalField(
      JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
    Optional.ofNullable(node).ifPresent(n -> con.accept(n, tag));
  }

  /**
   * Helper method for deserializers to set required tag fields.
   *
   * <p>If the JsonNode is null or missing, a NoSuchElementException is thrown. This is
   * used in custom deserializers to populate mandatory tag fields from JSON.
   *
   * @param <T> the tag type
   * @param node the JSON node (must not be null)
   * @param con consumer that sets the field value
   * @param tag the tag instance to populate
   * @throws java.util.NoSuchElementException if node is null
   */
  protected static <T extends BaseTag> void setRequiredField(
      JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
    con.accept(Optional.ofNullable(node).orElseThrow(), tag);
  }
}
