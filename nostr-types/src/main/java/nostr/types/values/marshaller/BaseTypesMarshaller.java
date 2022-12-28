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
            if (attribute instanceof StringValue) {
                return new StringMarshaller((StringValue) attribute);
            } else if (attribute instanceof NumberValue) {
                return new NumberMarshaller((NumberValue) attribute);
            } else if (attribute instanceof BooleanValue) {
                return new BooleanMarshaller((BooleanValue) attribute);
            } else if (attribute instanceof ArrayValue) {
                return new ArrayMarshaller((ArrayValue) attribute);
            } else if (attribute instanceof ObjectValue) {
                return new ObjectMarshaller((ObjectValue) attribute, escape);
            } else if (attribute instanceof ExpressionValue){
                return new ExpressionMarshaller((ExpressionValue) attribute, escape);
            } else {
                throw new RuntimeException();
            }
        }
    }
}
