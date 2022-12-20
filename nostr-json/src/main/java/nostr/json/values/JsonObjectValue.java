package nostr.json.values;

import nostr.json.JsonType;
import nostr.json.JsonValue;
import nostr.json.marshaller.impl.JsonObjectMarshaller;
import nostr.json.types.JsonObjectType;
import lombok.EqualsAndHashCode;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = false)
@Log
public class JsonObjectValue extends BaseJsonValue<JsonObjectType> {

    private JsonObjectValue(JsonObjectType type, JsonValueList value) {
        super(type, value);
    }

    public JsonObjectValue(JsonValueList value) {
        this(new JsonObjectType(), value);
    }

    @Override
    public JsonValueList getValue() {
        return (JsonValueList) super.getValue();
    }

    public JsonValue get(String variable) {
        for (JsonValue e : this.getValue().getList()) {
            JsonExpression<JsonType> expr = (JsonExpression<JsonType>) e;
            if (expr.getVariable().equals(variable)) {
                return expr.getJsonValue();
            }
        }

        log.log(Level.WARNING, "The variable {0} does not exist", variable);
        return null;
    }

    @Override
    public String toString() {
        return new JsonObjectMarshaller(this).marshall();
    }

    public String toString(boolean escape) {
        return new JsonObjectMarshaller(this, escape).marshall();
    }
}
