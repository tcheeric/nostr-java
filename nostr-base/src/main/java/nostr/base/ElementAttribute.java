package nostr.base;

import java.util.Objects;
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

    private final String name;

    private final IValue value;

    private final Integer nip;

    public ElementAttribute(String name, String value, Integer nip) {
        this(name, new StringValue(value), nip);
    }

    public ElementAttribute(String value, Integer nip) {
        this(null, value, nip);
    }

    public ElementAttribute(String value) {
        this(null, value, 1);
    }
}
