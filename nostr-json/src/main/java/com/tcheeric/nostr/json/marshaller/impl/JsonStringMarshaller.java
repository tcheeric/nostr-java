
package com.tcheeric.nostr.json.marshaller.impl;

import com.tcheeric.nostr.json.values.JsonStringValue;
import com.tcheeric.nostr.json.marshaller.BaseMarshaller;

/**
 *
 * @author squirrel
 */
public class JsonStringMarshaller extends BaseMarshaller {

    public JsonStringMarshaller(JsonStringValue jsonStringValue, boolean escape) {
        super(jsonStringValue, escape);
    }

    public JsonStringMarshaller(JsonStringValue jsonStringValue) {
        this(jsonStringValue, false);
    }

    @Override
    public String marshall() {
        if (!isEscape()) {
            return getJsonEntityValue().getValue() == null ? null : "\"" + getJsonEntityValue().getValue().toString() + "\"";
        } else {
            return getJsonEntityValue().getValue() == null ? null : "\\\"" + getJsonEntityValue().getValue().toString() + "\\\"";
        }
    }

}
