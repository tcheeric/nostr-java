package nostr.json.parser.impl;

import nostr.json.parser.JsonParseException;
import nostr.json.parser.BaseParser;
import nostr.json.values.JsonStringValue;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class JsonStringParser extends BaseParser<JsonStringValue> {

    public JsonStringParser(String json) {
        super(json.trim().substring(1, json.length()-1));
        log.log(Level.FINE, "Parsing string {0}", json.trim());
    }

    @Override
    public JsonStringValue parse() throws JsonParseException {
        return new JsonStringValue(json);
    }

}
