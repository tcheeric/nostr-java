package nostr.types.values.marshaller;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.MarshallException;
import nostr.types.values.impl.BooleanValue;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BooleanMarshaller extends BaseTypesMarshaller {

    public BooleanMarshaller(BooleanValue value) {
        super(value);
    }

    @Override
    public String marshall() throws MarshallException {
        return attribute.getValue().toString();
    }

}
