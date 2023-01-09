package nostr.types.values.impl;

import java.util.Optional;
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

    public Optional<Integer> intValue() {
        final Number number = (Number) getValue();
        return Optional.of(number.intValue());
    }

    public Optional<Double> doubleValue() {
        final Number number = (Number) getValue();
        return Optional.of(number.doubleValue());
    }

    public Optional<Long> longValue() {
        final Number number = (Number) getValue();
        return Optional.of(number.longValue());
    }

    public enum NumberType {
        INT,
        LONG,
        DOUBLE
    }
}
