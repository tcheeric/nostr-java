
package nostr.json.marshaller.impl;

import nostr.json.JsonValue;
import nostr.json.values.JsonExpression;
import nostr.json.values.JsonObjectValue;
import nostr.json.values.JsonValueList;
import nostr.json.marshaller.BaseMarshaller;
import java.util.List;

/**
 *
 * @author squirrel
 */
public class JsonObjectMarshaller extends BaseMarshaller {

    public JsonObjectMarshaller(JsonObjectValue jsonObjectValue, boolean escape) {
        super(jsonObjectValue, escape);
    }

    public JsonObjectMarshaller(JsonObjectValue jsonObjectValue) {
        this(jsonObjectValue, false);
    }

    @Override
    public String marshall() {
        JsonValueList value = (JsonValueList) (getJsonEntityValue().getValue());
        StringBuilder result = new StringBuilder();
        int i = 0;

        result.append("{");
        final List<JsonValue> valueList = value.getList();
        for (JsonValue e : valueList) {
            JsonExpression exp = (JsonExpression) e;
            result.append(new JsonExpressionMarshaller(exp, isEscape()).marshall());

            if (++i < valueList.size()) {
                result.append(",");
            }
        }
        result.append("}");

        return result.toString();
    }
    
}
