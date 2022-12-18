
package com.tcheeric.nostr.json.unmarshaller.impl;

import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.parser.impl.JsonArrayParser;
import com.tcheeric.nostr.json.types.JsonArrayType;
import com.tcheeric.nostr.json.unmarshaller.BaseUnmarshaller;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class JsonArrayUnmarshaller extends BaseUnmarshaller {

    public JsonArrayUnmarshaller(String json) {
        super(json);
    }

    @Override
    public JsonValue<JsonArrayType> unmarshall() {
        String jsonStr = getJson();

        return new JsonArrayParser(jsonStr).parse();        
    }
}
