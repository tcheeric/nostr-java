package nostr.json.parser.impl;

import nostr.json.parser.JsonParseException;
import nostr.json.parser.BaseParser;
import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.types.values.impl.BooleanValue;

/**
 *
 * @author squirrel
 */
@Log
public class JsonBooleanParser extends BaseParser<BooleanValue> {

    public JsonBooleanParser(String json) {
        super(json.trim());
        log.log(Level.FINE, "Parsing boolean {0}", json.trim());
    }

    @Override
    public BooleanValue parse() throws JsonParseException {
        if (json.equalsIgnoreCase("true") || json.equalsIgnoreCase("false")) {
            return new BooleanValue(Boolean.valueOf(json));
        }
        throw new JsonParseException("Invalid boolean value");
    }

}
