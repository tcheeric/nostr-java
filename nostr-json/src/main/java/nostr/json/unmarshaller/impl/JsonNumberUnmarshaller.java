package nostr.json.unmarshaller.impl;

import nostr.json.JsonValue;
import nostr.json.parser.impl.JsonNumberParser;
import nostr.json.types.JsonNumberType;
import nostr.json.unmarshaller.BaseUnmarshaller;
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
