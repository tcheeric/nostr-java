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

        if (attribute instanceof NumberValue numberValue) {
            
            switch (numberValue.getNumberType()) {
                case DOUBLE -> {
                    return numberValue.doubleValue().toString();
                }
                case INT -> {
                    return numberValue.intValue().toString();
                }
                case LONG -> {
                    return numberValue.longValue().toString();
                }
            }
        }

        // Should never be thrown!
        throw new MarshallException("Invalid numeric type.");
    }

}
