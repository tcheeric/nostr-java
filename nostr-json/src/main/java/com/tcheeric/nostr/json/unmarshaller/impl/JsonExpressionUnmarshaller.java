
package com.tcheeric.nostr.json.unmarshaller.impl;

import com.tcheeric.nostr.json.JsonType;
import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.parser.impl.JsonExpressionParser;
import com.tcheeric.nostr.json.unmarshaller.IUnmarshaller;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
@Data
@AllArgsConstructor
public class JsonExpressionUnmarshaller implements IUnmarshaller {

    private final String json;

    @Override
    public JsonValue<? extends JsonType> unmarshall() {
        return new JsonExpressionParser(json).parse();
    }
}
