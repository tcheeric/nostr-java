package nostr.types.values.marshaller;

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.types.MarshallException;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Log
public class ArrayMarshaller extends BaseTypesMarshaller {

    public ArrayMarshaller(ArrayValue value) {
        this(value, false);
    }

    public ArrayMarshaller(ArrayValue value, boolean escape) {
        super(value, escape);
    }

    @Override
    public String marshall() throws MarshallException {
        StringBuilder result = new StringBuilder();
        final IValue[] attrArr = (IValue[]) attribute.getValue();

        result.append("[");

        result.append(Arrays.asList(attrArr).stream().map(ArrayMarshaller::marshall).collect(Collectors.joining(",")));

        result.append("]");

        return result.toString();
    }
    
    private static String marshall(IValue v) {
        try {
            return BaseTypesMarshaller.Factory.create(v).marshall();
        } catch (MarshallException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    } 

}
