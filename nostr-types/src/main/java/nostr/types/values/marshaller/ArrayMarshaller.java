package nostr.types.values.marshaller;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.MarshallException;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ArrayMarshaller extends BaseTypesMarshaller {

    public ArrayMarshaller(ArrayValue value) {
        this(value, false);
    }

    public ArrayMarshaller(ArrayValue value, boolean escape) {
        super(value, escape);
    }

    @Override
    public String marshall() throws MarshallException {
        int i = 0;
        StringBuilder result = new StringBuilder();
        final IValue[] attrArr = (IValue[]) attribute.getValue();
        for (var v : attrArr) {
            result.append(BaseTypesMarshaller.Factory.create(v).marshall());

            if (++i < attrArr.length) {
                result.append(",");
            }
        }
        
        return result.toString();
    }

}
