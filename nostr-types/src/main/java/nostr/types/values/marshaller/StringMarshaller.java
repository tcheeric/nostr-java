package nostr.types.values.marshaller;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.types.MarshallException;
import nostr.types.values.impl.StringValue;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StringMarshaller extends BaseTypesMarshaller {

    public StringMarshaller(StringValue attribute) {
        this(attribute, false);
    }

    public StringMarshaller(StringValue attribute, boolean escape) {
        super(attribute, escape);
    }

    @Override
    public String marshall() throws MarshallException {
        StringBuilder result = new StringBuilder();

        if (!escape) {
            result.append("\"");
        } else {
            result.append("\\\"");
        }
        result.append(attribute.getValue().toString());
        if (!escape) {
            result.append("\"");
        } else {
            result.append("\\\"");
        }
        return result.toString();
    }

}
