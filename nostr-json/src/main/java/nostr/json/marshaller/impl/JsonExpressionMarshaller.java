
package nostr.json.marshaller.impl;

import nostr.json.JsonValue;
import nostr.json.values.JsonArrayValue;
import nostr.json.values.JsonBooleanValue;
import nostr.json.values.JsonExpression;
import nostr.json.values.JsonNumberValue;
import nostr.json.values.JsonObjectValue;
import nostr.json.values.JsonStringValue;
import nostr.json.marshaller.IMarshaller;
import lombok.Data;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@Log
public class JsonExpressionMarshaller implements IMarshaller {

    private final JsonExpression jsonExpression;
    private boolean escape;

    public JsonExpressionMarshaller(JsonExpression jsonExpression, boolean escape) {
        this.jsonExpression = jsonExpression;
        this.escape = escape;
    }

    public JsonExpressionMarshaller(JsonExpression jsonExpression) {
        this(jsonExpression, false);
    }

    @Override
    public String marshall() {
        StringBuilder result = new StringBuilder();
        if (!escape) {
            result.append("\"").append(jsonExpression.getVariable()).append("\":").append(IMarshaller.MarshallerFactory.create(jsonExpression.getJsonValue(), escape).marshall());
        } else {
            result.append("\\\"").append(jsonExpression.getVariable()).append("\\\":").append(IMarshaller.MarshallerFactory.create(jsonExpression.getJsonValue(), escape).marshall());
        }

        return result.toString();
    }
}
