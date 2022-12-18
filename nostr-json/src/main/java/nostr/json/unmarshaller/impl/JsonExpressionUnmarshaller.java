
package nostr.json.unmarshaller.impl;

import nostr.json.JsonType;
import nostr.json.JsonValue;
import nostr.json.parser.impl.JsonExpressionParser;
import nostr.json.unmarshaller.IUnmarshaller;
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
