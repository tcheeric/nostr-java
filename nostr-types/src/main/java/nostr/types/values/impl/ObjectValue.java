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
        
        final List<IValue> exprValueList = (List<IValue>) this.getValue();        
        final Optional<IValue> findFirst = exprValueList.stream().filter(e -> ((ExpressionValue)e).getName().equals(variable)).findFirst();

        if(findFirst.isEmpty()) {
            log.log(Level.WARNING, "The variable {0} does not exist", variable);
            return Optional.empty();
        }
        
        return Optional.of((IValue) findFirst.get().getValue());

    }
}
