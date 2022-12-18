package com.tcheeric.nostr.json.values;

import com.tcheeric.nostr.json.marshaller.impl.JsonStringMarshaller;
import com.tcheeric.nostr.json.types.JsonStringType;
import lombok.EqualsAndHashCode;

/**
 *
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = false)
public class JsonStringValue extends BaseJsonValue<JsonStringType> {

    private JsonStringValue(JsonStringType type, String value) {
        super(type, value);
    }

    public JsonStringValue(String value) {
        this(new JsonStringType(), value);
    }

    @Override
    public String getValue() {
        return (String) super.getValue();
    }
    
    @Override
    public String toString() {
        return new JsonStringMarshaller(this).marshall();
    }

    public String toString(boolean escape) {
        return new JsonStringMarshaller(this, escape).marshall();
    }
}
