package nostr.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import nostr.event.tag.AddressTag;
import nostr.event.tag.EmojiTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.ExpirationTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import nostr.event.tag.NonceTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.event.tag.RelaysTag;
import nostr.event.tag.SubjectTag;
import nostr.event.tag.VoteTag;
import org.apache.commons.lang3.stream.Streams;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = TagDeserializer.class)
@JsonSerialize(using = BaseTagSerializer.class)
public abstract class BaseTag implements ITag {

    @JsonIgnore
    private IEvent parent;

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
                new PropertyDescriptor(field.getName(), this.getClass())
                    .getReadMethod().invoke(this))
                .map(Object::toString);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException ex) {
            return Optional.empty();
        }
    }

    public List<Field> getSupportedFields() {
        return Streams.failableStream(Arrays.stream(this.getClass().getDeclaredFields()))
            .filter(f ->
                Objects.nonNull(f.getAnnotation(Key.class)))
            .filter(f ->
                getFieldValue(f).isPresent())
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
        GenericTag genericTag = new GenericTag(code,
                IntStream.range(0, params.size())
                        .mapToObj(i ->
                                new ElementAttribute("param".concat(String.valueOf(i)), params.get(i)))
                        .toList());

        return switch (code) {
            case "a" -> convert(genericTag, AddressTag.class);
            case "d" -> convert(genericTag, IdentifierTag.class);
            case "e" -> convert(genericTag, EventTag.class);
            case "g" -> convert(genericTag, GeohashTag.class);
            case "l" -> convert(genericTag, LabelTag.class);
            case "L" -> convert(genericTag, LabelNamespaceTag.class);
            case "p" -> convert(genericTag, PubKeyTag.class);
            case "r" -> convert(genericTag, ReferenceTag.class);
            case "t" -> convert(genericTag, HashtagTag.class);
            case "v" -> convert(genericTag, VoteTag.class);
            case "emoji" -> convert(genericTag, EmojiTag.class);
            case "expiration" -> convert(genericTag, ExpirationTag.class);
            case "nonce" -> convert(genericTag, NonceTag.class);
            case "price" -> convert(genericTag, PriceTag.class);
            case "relays" -> convert(genericTag, RelaysTag.class);
            case "subject" -> convert(genericTag, SubjectTag.class);
            default -> genericTag;
        };
    }

    public static <T extends BaseTag> T convert(@NonNull GenericTag genericTag, @NonNull Class<T> clazz) {

        try {
            T tag = clazz.getConstructor().newInstance();
            if (genericTag.getParent() != null) {
                tag.setParent(genericTag.getParent());
            }

            Method staticUpdateFields = clazz.getMethod("updateFields", GenericTag.class);
            return (T) staticUpdateFields.invoke(null, genericTag);

        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T extends BaseTag> void setOptionalField(JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
        Optional.ofNullable(node).ifPresent(n -> con.accept(n, tag));
    }

    protected static <T extends BaseTag> void setRequiredField(JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
        con.accept(Optional.ofNullable(node).orElseThrow(), tag);
    }
}
