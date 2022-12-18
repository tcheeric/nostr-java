
package com.tcheeric.nostr.json.unmarshaller.impl;

import com.tcheeric.nostr.json.JsonType;
import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.parser.impl.JsonBooleanParser;
import com.tcheeric.nostr.json.unmarshaller.BaseUnmarshaller;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class JsonBooleanUnmarshaller extends BaseUnmarshaller {

    public JsonBooleanUnmarshaller(String json) {
        super(json);
    }

    @Override
    public JsonValue<? extends JsonType> unmarshall() {
        String jsonStr = getJson();
        return new JsonBooleanParser(jsonStr).parse();
    }
}
