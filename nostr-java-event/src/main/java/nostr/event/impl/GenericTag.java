package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.event.BaseTag;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericTag extends BaseTag implements IGenericElement {

    private final String code;

    private final List<ElementAttribute> attributes;

    public GenericTag(@NonNull String code, @NonNull ElementAttribute... attribute) {
        this(code, List.of(attribute));
    }

    public GenericTag(@NonNull String code, @NonNull List<ElementAttribute> attributes) {
        this.code = code;
        this.attributes = attributes;
    }

    @Override
    public void addAttribute(@NonNull ElementAttribute... attribute) {
        this.addAttributes(List.of(attribute));
    }

    @Override
    public void addAttributes(@NonNull List<ElementAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    public static GenericTag create(@NonNull String code, Integer nip, @NonNull String... params) {
        return create(code, nip, List.of(params));
    }

    public static GenericTag create(@NonNull String code, Integer nip, @NonNull List<String> params) {
        return new GenericTag(code,
            IntStream.range(0, params.size())
                .mapToObj(i ->
                    new ElementAttribute("param".concat(String.valueOf(i)), params.get(i)))
                .toList());
    }
}
