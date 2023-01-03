package nostr.types.values.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.Type;
import nostr.types.values.BaseValue;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NumberValue extends BaseValue {

    private final NumberType numberType;

    public NumberValue(Number value) {
        this(value, NumberType.INT);
    }

    public NumberValue(Number value, NumberType numberType) {
        super(Type.NUMBER, value);
        this.numberType = numberType;
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

    public enum NumberType {
        INT,
        LONG,
        DOUBLE
    }
}
