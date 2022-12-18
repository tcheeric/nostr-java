
package com.tcheeric.nostr.json.marshaller.impl;

import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.values.JsonArrayValue;
import com.tcheeric.nostr.json.values.JsonBooleanValue;
import com.tcheeric.nostr.json.values.JsonExpression;
import com.tcheeric.nostr.json.values.JsonNumberValue;
import com.tcheeric.nostr.json.values.JsonObjectValue;
import com.tcheeric.nostr.json.values.JsonStringValue;
import com.tcheeric.nostr.json.marshaller.IMarshaller;
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
