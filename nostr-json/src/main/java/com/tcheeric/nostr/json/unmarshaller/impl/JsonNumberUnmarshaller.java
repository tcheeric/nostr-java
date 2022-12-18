package com.tcheeric.nostr.json.unmarshaller.impl;

import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.parser.impl.JsonNumberParser;
import com.tcheeric.nostr.json.types.JsonNumberType;
import com.tcheeric.nostr.json.unmarshaller.BaseUnmarshaller;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class JsonNumberUnmarshaller extends BaseUnmarshaller {

    public JsonNumberUnmarshaller(String json) {
        super(json);
    }

    @Override
    public JsonValue<JsonNumberType> unmarshall() {
        String jsonStr = getJson();
        return new JsonNumberParser(jsonStr).parse();
    }

}
