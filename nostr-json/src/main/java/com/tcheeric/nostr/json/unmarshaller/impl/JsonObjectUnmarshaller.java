
package com.tcheeric.nostr.json.unmarshaller.impl;

import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.parser.impl.JsonObjectParser;
import com.tcheeric.nostr.json.types.JsonObjectType;
import com.tcheeric.nostr.json.unmarshaller.BaseUnmarshaller;

/**
 *
 * @author squirrel
 */
public class JsonObjectUnmarshaller extends BaseUnmarshaller {

    public JsonObjectUnmarshaller(String json) {
        super(json);
    }

    @Override
    public JsonValue<JsonObjectType> unmarshall() {
        String jsonStr = getJson();
        
        return new JsonObjectParser(jsonStr).parse();
    }
}
