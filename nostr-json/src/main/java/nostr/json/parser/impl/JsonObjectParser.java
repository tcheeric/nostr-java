package nostr.json.parser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.json.parser.BaseParser;
import nostr.json.parser.JsonParseException;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.ObjectValue;

/**
 *
 * @author squirrel
 */
@Log
public class JsonObjectParser extends BaseParser<ObjectValue> {

    private int cursor;

    public JsonObjectParser(@NonNull String json) {
        super(json.trim());
        this.cursor = 0;
        log.log(Level.FINE, "Parsing object {0}", json.trim());
    }

    @Override
    public ObjectValue parse() throws JsonParseException {
        if (!json.startsWith("{")) {
            throw new JsonParseException("Parse error at index 0");
        }

        if (!json.endsWith("}")) {
            throw new JsonParseException("Parse error at index " + (json.length() - 1));
        }

        var subJsonStr = this.json.substring(1, this.json.length() - 1);
        return new ObjectValue(splitExpressions(subJsonStr));
    }

    private List<ExpressionValue> splitExpressions(String subJsonStr) {
        List<ExpressionValue> result = new ArrayList<>();

        if ("".equals(subJsonStr)) {
            return result;
        }
        
        int cursor = 0, j = 0;

        while ((j = nextCommaIndex(cursor, subJsonStr)) > 0) {
            String subJson = subJsonStr.substring(cursor, j);
            cursor = j + 1;
            result.add(new JsonExpressionParser(subJson).parse());
        }
        result.add(new JsonExpressionParser(subJsonStr.substring(cursor)).parse());
        return result;
    }

    private int nextCommaIndex(int start, String subJsonStr) {

        for (int i = start; i < subJsonStr.length();) {

            if (i >= subJsonStr.length()) {
                return -1;
            }

            char c = subJsonStr.charAt(i);
            switch (c) {
                case ',' -> {
                    return i;
                }
                case '[', '{', '\"', '\'', '(' ->
                    i = getClosedParenIndex(c, i + 1, subJsonStr) + 1;
                default ->
                    i++;
            }
        }

        return -2;
    }
}
