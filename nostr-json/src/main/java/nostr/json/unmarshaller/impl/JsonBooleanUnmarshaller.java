
package nostr.json.unmarshaller.impl;

import nostr.json.parser.impl.JsonBooleanParser;
import nostr.json.unmarshaller.BaseUnmarshaller;
import lombok.extern.java.Log;
import nostr.types.values.impl.BooleanValue;

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
    public BooleanValue unmarshall() {
        String jsonStr = getJson();
        return new JsonBooleanParser(jsonStr).parse();
    }
}
