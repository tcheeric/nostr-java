package nostr.event.tag;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.event.BaseTag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericTag extends BaseTag implements IGenericElement {

    private String code;

    private final List<ElementAttribute> attributes;

    public GenericTag() {
        this("");
    }

    public GenericTag(@NonNull String code) {
        this(code, new ArrayList<>());
    }

    /**
     * nip parameter to be removed
     *
     * @deprecated use any available proper constructor variant instead
     */
    @Deprecated(forRemoval = true)
    public GenericTag(String code, Integer nip) {
        this(code, new ArrayList<>());
    }

    public GenericTag(@NonNull String code, @NonNull ElementAttribute... attribute) {
        this(code, List.of(attribute));
    }

    public GenericTag(@NonNull String code, @NonNull List<ElementAttribute> attributes) {
        this.code = code;
        this.attributes = attributes;
    }

    @Override
    public String getCode() {
        return "".equals(this.code) ? super.getCode() : this.code;
    }

    @Override
    public void addAttribute(@NonNull ElementAttribute... attribute) {
        this.addAttributes(List.of(attribute));
    }

    @Override
    public void addAttributes(@NonNull List<ElementAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    /**
     * nip parameter to be removed
     *
     * @deprecated use {@link #create(String, String...)} instead.
     */
    public static GenericTag create(String code, Integer nip, String... params) {
        return create(code, List.of(params));
    }

    /**
     * nip parameter to be removed
     *
     * @deprecated use {@link #create(String, List)} instead.
     */

    @Deprecated(forRemoval = true)
    public static GenericTag create(String code, Integer nip, List<String> params) {
        return create(code, params);
    }

    public static GenericTag create(@NonNull String code, @NonNull String... params) {
        return create(code, List.of(params));
    }

    public static GenericTag create(@NonNull String code, @NonNull List<String> params) {
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
            case "emoji" -> convert(genericTag, EmojiTag.class);
            case "expiration" -> convert(genericTag, ExpirationTag.class);
            case "nonce" -> convert(genericTag, NonceTag.class);
            case "price" -> convert(genericTag, PriceTag.class);
            case "relays" -> convert(genericTag, RelaysTag.class);
            case "subject" -> convert(genericTag, SubjectTag.class);
            default -> genericTag;
        };
    }

    public static <T extends GenericTag> T convert(@NonNull GenericTag genericTag, @NonNull Class<T> clazz) {

        if (clazz.isInstance(genericTag)) {
            return (T) genericTag;
        }

        try {
            T tag = clazz.getConstructor().newInstance();
            tag.setCode(genericTag.getCode());
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
}


