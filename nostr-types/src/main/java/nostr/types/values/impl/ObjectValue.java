package nostr.types.values.impl;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.types.Type;
import nostr.types.values.BaseValue;
import nostr.types.values.IValue;

/**
 *
 * @author squirrel
 */
@Log
public class ObjectValue extends BaseValue {

    public ObjectValue(List<ExpressionValue> value) {
        super(Type.OBJECT, value);
    }

    public Optional<IValue> get(String variable) {        
        for (IValue e : (List<ExpressionValue>) this.getValue()) {
            ExpressionValue expr = (ExpressionValue) e;
            if (expr.getName().equals(variable)) {
                return Optional.of((IValue) expr.getValue());
            }
        }

        log.log(Level.WARNING, "The variable {0} does not exist", variable);
        return Optional.empty();
    }
}
