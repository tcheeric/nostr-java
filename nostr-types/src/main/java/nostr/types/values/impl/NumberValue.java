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

    public int intValue() {
        final Number number = (Number) getValue();
        final Optional<Integer> optNum = Optional.of(number.intValue());
        
        if (optNum.isEmpty() || optNum.get() == null) {
            throw new RuntimeException("Invalid number");
        }
        
        return optNum.get();
    }

    public double doubleValue() {
        final Number number = (Number) getValue();
        final Optional<Double> optNum = Optional.of(number.doubleValue());
        
        if (optNum.isEmpty() || optNum.get() == null) {
            throw new RuntimeException("Invalid number");
        }
        
        return optNum.get();
    }

    public long longValue() {
        final Number number = (Number) getValue();
        final Optional<Long> optNum = Optional.of(number.longValue());
        
        if (optNum.isEmpty() || optNum.get() == null) {
            throw new RuntimeException("Invalid number");
        }
        
        return optNum.get();
    }

    public enum NumberType {
        INT,
        LONG,
        DOUBLE
    }
}
