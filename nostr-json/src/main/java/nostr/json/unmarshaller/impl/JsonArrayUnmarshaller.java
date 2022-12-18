
package nostr.json.unmarshaller.impl;

import nostr.json.JsonValue;
import nostr.json.parser.impl.JsonArrayParser;
import nostr.json.types.JsonArrayType;
import nostr.json.unmarshaller.BaseUnmarshaller;
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
