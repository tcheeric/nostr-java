
package nostr.json.unmarshaller.impl;

import nostr.json.JsonValue;
import nostr.json.parser.impl.JsonObjectParser;
import nostr.json.types.JsonObjectType;
import nostr.json.unmarshaller.BaseUnmarshaller;

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
