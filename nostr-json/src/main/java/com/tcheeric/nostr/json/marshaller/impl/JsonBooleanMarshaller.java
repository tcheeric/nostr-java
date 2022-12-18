
package com.tcheeric.nostr.json.marshaller.impl;

import com.tcheeric.nostr.json.values.JsonBooleanValue;
import com.tcheeric.nostr.json.marshaller.BaseMarshaller;

/**
 *
 * @author squirrel
 */
public class JsonBooleanMarshaller extends BaseMarshaller {

    public JsonBooleanMarshaller(JsonBooleanValue jsonBooleanValue) {
        super(jsonBooleanValue);
    }

    @Override
    public String marshall() {
        return getJsonEntityValue().getValue().toString();
    }
    
}
