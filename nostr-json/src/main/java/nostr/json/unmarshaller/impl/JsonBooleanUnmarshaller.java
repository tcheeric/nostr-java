
package nostr.json.unmarshaller.impl;

import nostr.json.JsonType;
import nostr.json.JsonValue;
import nostr.json.parser.impl.JsonBooleanParser;
import nostr.json.unmarshaller.BaseUnmarshaller;
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
