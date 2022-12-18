
package com.tcheeric.nostr.json.unmarshaller.impl;

import com.tcheeric.nostr.json.parser.JsonParseException;
import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.parser.impl.JsonStringParser;
import com.tcheeric.nostr.json.types.JsonStringType;
import com.tcheeric.nostr.json.unmarshaller.BaseUnmarshaller;

/**
 *
 * @author squirrel
 */
public class JsonStringUnmarshaller extends BaseUnmarshaller {

    public JsonStringUnmarshaller(String json) {
        super(json);
    }

    
    @Override
    public JsonValue<JsonStringType> unmarshall() {
        String jsonStr = getJson();
        if (!jsonStr.startsWith("\"")) {
            throw new JsonParseException("Parse error at index 0");
        }

        if (!jsonStr.endsWith("\"")) {
            throw new JsonParseException("Parse error at index " + (jsonStr.length() - 1));
        }

        return new JsonStringParser(jsonStr).parse();
    }

}
