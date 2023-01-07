package nostr.types.values.marshaller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.IMarshaller;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;
import nostr.types.values.impl.BooleanValue;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.NumberValue;
import nostr.types.values.impl.ObjectValue;
import nostr.types.values.impl.StringValue;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode
public abstract class BaseTypesMarshaller implements IMarshaller {

    protected final IValue attribute;
    protected final boolean escape;

    public BaseTypesMarshaller(IValue value) {
        this(value, false);
    }

    public BaseTypesMarshaller(IValue value, boolean escape) {
        this.attribute = value;
        this.escape = escape;
    }

    public static class Factory {

        public static IMarshaller create(IValue attribute) {
            return create(attribute, false);
        }

        public static IMarshaller create(IValue attribute, boolean escape) {
            if (attribute instanceof StringValue stringValue) {
                return new StringMarshaller(stringValue);
            } else if (attribute instanceof NumberValue numberValue) {
                return new NumberMarshaller(numberValue);
            } else if (attribute instanceof BooleanValue booleanValue) {
                return new BooleanMarshaller(booleanValue);
            } else if (attribute instanceof ArrayValue arrayValue) {
                return new ArrayMarshaller(arrayValue);
            } else if (attribute instanceof ObjectValue objectValue) {
                return new ObjectMarshaller(objectValue, escape);
            } else if (attribute instanceof ExpressionValue expressionValue){
                return new ExpressionMarshaller(expressionValue, escape);
            } else {
                throw new RuntimeException();
            }
        }
    }
}
