
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
        return doubleValue().intValue();
    }

    public Double doubleValue() {
        return ((Double) getValue());
    }

    public Long longValue() {
        return ((Long) getValue());
    }
}
