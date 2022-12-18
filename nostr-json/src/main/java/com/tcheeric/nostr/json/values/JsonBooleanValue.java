
package com.tcheeric.nostr.json.values;

import com.tcheeric.nostr.json.marshaller.impl.JsonBooleanMarshaller;
import com.tcheeric.nostr.json.types.JsonBooleanType;

/**
 *
 * @author squirrel
 */
public class JsonBooleanValue extends BaseJsonValue<JsonBooleanType> {

    private JsonBooleanValue(JsonBooleanType type, Boolean value) {
        super(type, value);
    }

    public JsonBooleanValue(Boolean value) {
        this(new JsonBooleanType(), value);
    }

    @Override
    public Boolean getValue() {
        return (Boolean) super.getValue();
    }

    @Override
    public String toString() {
        return new JsonBooleanMarshaller(this).marshall();
    }
}
