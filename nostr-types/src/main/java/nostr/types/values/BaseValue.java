package nostr.types.values;

import java.util.logging.Level;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.types.MarshallException;
import nostr.types.Type;
import nostr.types.values.marshaller.BaseTypesMarshaller;

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
        try {
            return BaseTypesMarshaller.Factory.create(this).marshall();
        } catch (MarshallException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
