package nostr.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = TagDeserializer.class)
@JsonSerialize(using = BaseTagSerializer.class)
public abstract class BaseTag implements ITag {

  @JsonIgnore private IEvent parent;

  @Override
  public void setParent(IEvent event) {
    this.parent = event;
  }

  @Override
  public String getCode() {
    return this.getClass().getAnnotation(Tag.class).code();
  }

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

  public List<Field> getSupportedFields() {
    return Streams.failableStream(Arrays.stream(this.getClass().getDeclaredFields()))
        .filter(f -> Objects.nonNull(f.getAnnotation(Key.class)))
        .filter(f -> getFieldValue(f).isPresent())
        .collect(Collectors.toList());
  }

  /**
   * nip parameter to be removed
   *
   * @deprecated use {@link #create(String, String...)} instead.
   */
  public static BaseTag create(String code, Integer nip, String... params) {
    return create(code, List.of(params));
  }

  /**
   * nip parameter to be removed
   *
   * @deprecated use {@link #create(String, List)} instead.
   */
  @Deprecated(forRemoval = true)
  public static BaseTag create(String code, Integer nip, List<String> params) {
    return create(code, params);
  }

  public static BaseTag create(@NonNull String code, @NonNull String... params) {
    return create(code, List.of(params));
  }

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

  protected static <T extends BaseTag> void setOptionalField(
      JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
    Optional.ofNullable(node).ifPresent(n -> con.accept(n, tag));
  }

  protected static <T extends BaseTag> void setRequiredField(
      JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
    con.accept(Optional.ofNullable(node).orElseThrow(), tag);
  }
}
