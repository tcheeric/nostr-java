package nostr.types.values.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.Type;
import nostr.types.values.BaseValue;
import nostr.types.values.IValue;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
//@JsonSerialize(using=ExpressionValueSerializer.class)
public class ExpressionValue extends BaseValue {

    private final String name;
    
    public ExpressionValue(String name, IValue value) {
        super(Type.EXPRESSION, value);
        this.name = name;
    }
    
    
}
