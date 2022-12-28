
package nostr.json.unmarshaller.impl;

import nostr.json.parser.impl.JsonObjectParser;
import nostr.json.unmarshaller.BaseUnmarshaller;
import nostr.types.values.impl.ObjectValue;

/**
 *
 * @author squirrel
 */
public class JsonObjectUnmarshaller extends BaseUnmarshaller {

    public JsonObjectUnmarshaller(String json) {
        super(json);
    }

    @Override
    public ObjectValue unmarshall() {
        String jsonStr = getJson();
        
        return new JsonObjectParser(jsonStr).parse();
    }
}
