
package com.tcheeric.nostr.json.marshaller;

import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.values.JsonArrayValue;
import com.tcheeric.nostr.json.values.JsonBooleanValue;
import com.tcheeric.nostr.json.values.JsonNumberValue;
import com.tcheeric.nostr.json.values.JsonObjectValue;
import com.tcheeric.nostr.json.values.JsonStringValue;
import com.tcheeric.nostr.json.marshaller.impl.JsonArrayMarshaller;
import com.tcheeric.nostr.json.marshaller.impl.JsonBooleanMarshaller;
import com.tcheeric.nostr.json.marshaller.impl.JsonNumberMarshaller;
import com.tcheeric.nostr.json.marshaller.impl.JsonObjectMarshaller;
import com.tcheeric.nostr.json.marshaller.impl.JsonStringMarshaller;

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
