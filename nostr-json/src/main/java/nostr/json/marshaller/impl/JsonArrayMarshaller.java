
package nostr.json.marshaller.impl;

import nostr.json.JsonValue;
import nostr.json.values.JsonArrayValue;
import nostr.json.marshaller.BaseMarshaller;
import nostr.json.marshaller.IMarshaller;
import java.util.List;

/**
 *
 * @author squirrel
 */
public class JsonArrayMarshaller extends BaseMarshaller {

    public JsonArrayMarshaller(JsonArrayValue jsonArrayValue, boolean escape) {
        super(jsonArrayValue, escape);
    }
    
    public JsonArrayMarshaller(JsonArrayValue jsonArrayValue) {
        this(jsonArrayValue, false);
    }
    
    @Override
    public String marshall() {
        List<JsonValue> value = (List<JsonValue>) (getJsonEntityValue().getValue());
        StringBuilder result = new StringBuilder();
        int i = 0;

        result.append("[");
        for (JsonValue v : value) {
            result.append(IMarshaller.MarshallerFactory.create(v, isEscape()).marshall());

            if (++i < value.size()) {
                result.append(",");
            }
        }
        result.append("]");

        return result.toString();
    }
    
}
