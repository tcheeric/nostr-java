
package nostr.json.unmarshaller.impl;

import nostr.json.parser.JsonParseException;
import nostr.json.JsonValue;
import nostr.json.parser.impl.JsonStringParser;
import nostr.json.types.JsonStringType;
import nostr.json.unmarshaller.BaseUnmarshaller;

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
