package nostr.types.values.marshaller;

import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.MarshallException;
import nostr.types.values.IValue;
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
        int i = 0;

        final ObjectValue attrObj = (ObjectValue) attribute.getValue();
        final List<ExpressionValue> exprList = (List<ExpressionValue>) attrObj.getValue();
        
        result.append("{");
        for (var e : exprList) {

            result.append(new ExpressionMarshaller(e, escape).marshall());

            if (++i < exprList.size()) {
                result.append(",");
            }
        }
        result.append("}");

        return result.toString();
    }

}
