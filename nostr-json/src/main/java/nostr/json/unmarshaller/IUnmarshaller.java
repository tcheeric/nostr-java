
package nostr.json.unmarshaller;

import nostr.json.JsonType;
import nostr.json.JsonValue;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.json.unmarshaller.impl.JsonBooleanUnmarshaller;
import nostr.json.unmarshaller.impl.JsonNumberUnmarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.json.unmarshaller.impl.JsonStringUnmarshaller;

/**
 *
 * @author squirrel
 */
public interface IUnmarshaller {

    public abstract JsonValue<? extends JsonType> unmarshall();

    public static class UnmarshallerFactory {

        public static IUnmarshaller create(String strJson) {
            
            strJson = strJson.trim();
            
            if (strJson.startsWith("\"") && strJson.endsWith("\"")) {
                return new JsonStringUnmarshaller(strJson);
            } else if (strJson.startsWith("[") && strJson.endsWith("]")) {
                return new JsonArrayUnmarshaller(strJson);
            } else if (strJson.startsWith("{") && strJson.endsWith("}")) {
                return new JsonObjectUnmarshaller(strJson);
            } else {
                if (strJson.equalsIgnoreCase("true") || strJson.equalsIgnoreCase("false")) {
                    return new JsonBooleanUnmarshaller(strJson);
                } else {
                    return new JsonNumberUnmarshaller(strJson);
                }
            }
        }
    }

}
