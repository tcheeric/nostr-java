package nostr.json.values;

import nostr.json.JsonValue;
import nostr.json.marshaller.impl.JsonArrayMarshaller;
import nostr.json.types.JsonArrayType;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 *
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = false)
public class JsonArrayValue extends BaseJsonValue<JsonArrayType> {

    private JsonArrayValue(JsonArrayType type, List<JsonValue> value) {
        super(type, value);
    }

    public JsonArrayValue(List<JsonValue> value) {
        this(new JsonArrayType(), value);
    }

    public JsonValue get(int index) {
        return ((List<JsonValue>) this.getValue()).get(index);
    }

    public int length() {
        return ((List<JsonValue>) this.getValue()).size();
    }
    
    @Override
    public List<JsonValue> getValue() {
        return (List<JsonValue>) super.getValue();
    }

    @Override
    public String toString() {
        return new JsonArrayMarshaller(this).marshall();
    }

    public String toString(boolean escape) {
        return new JsonArrayMarshaller(this, escape).marshall();
    }
}
