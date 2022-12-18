
package com.tcheeric.nostr.json.marshaller.impl;

import com.tcheeric.nostr.json.values.JsonNumberValue;
import com.tcheeric.nostr.json.marshaller.BaseMarshaller;

/**
 *
 * @author squirrel
 */
public class JsonNumberMarshaller extends BaseMarshaller {

    public JsonNumberMarshaller(JsonNumberValue jsonNumberValue) {
        super(jsonNumberValue);
    }

    @Override
    public String marshall() {
        return this.getJsonEntityValue().getValue().toString();
    }
    
}
