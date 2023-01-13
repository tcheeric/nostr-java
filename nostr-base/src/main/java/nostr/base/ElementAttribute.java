package nostr.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.types.values.IValue;
import nostr.types.values.impl.StringValue;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class ElementAttribute {

    private final IValue value;

    private final Integer nip;

    public ElementAttribute(String value, Integer nip) {
        this(new StringValue(value), nip);
    }

    public ElementAttribute(String value) {
        this(value, 1);
    }
}
