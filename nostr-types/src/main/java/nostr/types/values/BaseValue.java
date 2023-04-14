package nostr.types.values;

import lombok.Data;
import lombok.extern.java.Log;
import nostr.types.Type;

/**
 *
 * @author squirrel
 */
@Data
@Log
public abstract class BaseValue implements IValue {

    private final Type type;
    private final Object value;

    public BaseValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
