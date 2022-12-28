
package nostr.json.unmarshaller.impl;

import nostr.json.parser.impl.JsonExpressionParser;
import nostr.json.unmarshaller.IUnmarshaller;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.types.values.impl.ExpressionValue;

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
    public ExpressionValue unmarshall() {
        return new JsonExpressionParser(json).parse();
    }
}
