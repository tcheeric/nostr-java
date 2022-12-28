
package nostr.json.unmarshaller;

import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.json.unmarshaller.impl.JsonBooleanUnmarshaller;
import nostr.json.unmarshaller.impl.JsonNumberUnmarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.json.unmarshaller.impl.JsonStringUnmarshaller;
import nostr.types.values.IValue;

/**
 *
 * @author squirrel
 */
public interface IUnmarshaller {

    public abstract IValue unmarshall();

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
