package nostr.types.values.marshaller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.MarshallException;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.ObjectValue;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ObjectMarshaller extends BaseTypesMarshaller {

    public ObjectMarshaller(ObjectValue value) {
        this(value, false);
    }

    public ObjectMarshaller(ObjectValue value, boolean escape) {
        super(value, escape);
    }

    @Override
    public String marshall() throws MarshallException {

        StringBuilder result = new StringBuilder();

        final List<ExpressionValue> exprList = (List<ExpressionValue>) attribute.getValue();
        
        result.append("{");
        
        result.append(exprList.stream().map((ExpressionValue e) -> {
            try {
                return new ExpressionMarshaller(e, escape).marshall();
            } catch (MarshallException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.joining(",")));

        result.append("}");

        return result.toString();
    }

}
