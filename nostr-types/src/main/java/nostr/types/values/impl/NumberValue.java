package nostr.types.values.impl;

import nostr.types.Type;
import nostr.types.values.BaseValue;

/**
 *
 * @author squirrel
 */
public class NumberValue extends BaseValue {

    public NumberValue(Number value) {
        super(Type.NUMBER, value);
    }

    public Integer intValue() {
        return ((Number) getValue()).intValue();
    }

    public Double doubleValue() {
        return ((Number) getValue()).doubleValue();
    }

    public Long longValue() {
        return ((Number) getValue()).longValue();
    }
}
