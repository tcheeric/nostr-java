
package nostr.json.marshaller;

import nostr.json.JsonValue;
import nostr.json.values.JsonArrayValue;
import nostr.json.values.JsonBooleanValue;
import nostr.json.values.JsonNumberValue;
import nostr.json.values.JsonObjectValue;
import nostr.json.values.JsonStringValue;
import nostr.json.marshaller.impl.JsonArrayMarshaller;
import nostr.json.marshaller.impl.JsonBooleanMarshaller;
import nostr.json.marshaller.impl.JsonNumberMarshaller;
import nostr.json.marshaller.impl.JsonObjectMarshaller;
import nostr.json.marshaller.impl.JsonStringMarshaller;

/**
 *
 * @author squirrel
 */
public interface IMarshaller {

    public abstract String marshall();

    public static class MarshallerFactory {

        public static IMarshaller create(JsonValue jsonValue, boolean escape) {
            if (jsonValue instanceof JsonArrayValue) {
                return new JsonArrayMarshaller((JsonArrayValue) jsonValue, escape);
            } else if (jsonValue instanceof JsonBooleanValue) {
                return new JsonBooleanMarshaller((JsonBooleanValue) jsonValue);
            } else if (jsonValue instanceof JsonNumberValue) {
                return new JsonNumberMarshaller((JsonNumberValue) jsonValue);
            } else if (jsonValue instanceof JsonObjectValue) {
                return new JsonObjectMarshaller((JsonObjectValue) jsonValue, escape);
            } else if (jsonValue instanceof JsonStringValue) {
                return new JsonStringMarshaller((JsonStringValue) jsonValue, escape);
            } else {
                return null;
            }
        }
    }
}
