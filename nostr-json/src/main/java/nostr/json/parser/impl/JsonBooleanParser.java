package nostr.json.parser.impl;

import nostr.json.parser.JsonParseException;
import nostr.json.parser.BaseParser;
import nostr.json.values.JsonBooleanValue;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author eric
 */
@Log
public class JsonBooleanParser extends BaseParser<JsonBooleanValue> {

    public JsonBooleanParser(String json) {
        super(json.trim());
        log.log(Level.FINE, "Parsing boolean {0}", json.trim());
    }

    @Override
    public JsonBooleanValue parse() throws JsonParseException {
        if (json.equalsIgnoreCase("true") || json.equalsIgnoreCase("false")) {
            return new JsonBooleanValue(Boolean.valueOf(json));
        }
        throw new JsonParseException();
    }

}
