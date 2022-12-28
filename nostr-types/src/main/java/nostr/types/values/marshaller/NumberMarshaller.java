
package nostr.types.values.marshaller;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.MarshallException;
import nostr.types.values.impl.NumberValue;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NumberMarshaller extends BaseTypesMarshaller {

    public NumberMarshaller(NumberValue value) {
        super(value);
    }

    @Override
    public String marshall() throws MarshallException {
        
        return  attribute.getValue().toString();
    }
    
}
