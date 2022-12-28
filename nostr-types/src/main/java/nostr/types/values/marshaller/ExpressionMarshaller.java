package nostr.types.values.marshaller;

import nostr.types.MarshallException;
import nostr.types.values.IValue;
import nostr.types.values.impl.ExpressionValue;

/**
 *
 * @author squirrel
 */
public class ExpressionMarshaller extends BaseTypesMarshaller {

    public ExpressionMarshaller(ExpressionValue value) {
        this(value, false);
    }

    public ExpressionMarshaller(ExpressionValue value, boolean escape) {
        super(value, escape);
    }

    @Override
    public String marshall() throws MarshallException {
        StringBuilder result = new StringBuilder();
        result.append("\"");
        result.append(((ExpressionValue) attribute).getName());
        result.append("\":");

        result.append(BaseTypesMarshaller.Factory.create((IValue) attribute.getValue()).marshall());

        return result.toString();
    }

}
