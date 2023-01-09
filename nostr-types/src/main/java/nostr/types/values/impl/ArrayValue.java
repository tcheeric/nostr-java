package nostr.types.values.impl;

import java.util.Optional;
import nostr.types.Type;
import nostr.types.values.BaseValue;
import nostr.types.values.IValue;

/**
 *
 * @author squirrel
 */
public class ArrayValue extends BaseValue {

    public ArrayValue(IValue value[]) {
        super(Type.ARRAY, value);
    }

    public int length() {
        return ((IValue[]) this.getValue()).length;
    }

    public Optional<IValue> get(int index) {
        final IValue[] arr = (IValue[]) this.getValue();
        return Optional.of(arr[index]);
    }

}
