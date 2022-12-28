package nostr.json.parser.impl;

import nostr.json.parser.JsonParseException;
import nostr.json.parser.BaseParser;
import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.types.values.impl.StringValue;

/**
 *
 * @author squirrel
 */
@Log
public class JsonStringParser extends BaseParser<StringValue> {

    public JsonStringParser(String json) {
        super(json.trim().substring(1, json.length()-1));
        log.log(Level.FINE, "Parsing string {0}", json.trim());
    }

    @Override
    public StringValue parse() throws JsonParseException {
        return new StringValue(json);
    }

}
