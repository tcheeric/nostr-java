package com.tcheeric.nostr.json.parser.impl;

import com.tcheeric.nostr.json.parser.JsonParseException;
import com.tcheeric.nostr.json.parser.BaseParser;
import com.tcheeric.nostr.json.values.JsonObjectValue;
import com.tcheeric.nostr.json.values.JsonValueList;
import java.util.logging.Level;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class JsonObjectParser extends BaseParser<JsonObjectValue> {

    private int cursor;

    public JsonObjectParser(@NonNull String json) {
        super(json.trim());
        this.cursor = 0;
        log.log(Level.FINE, "Parsing object {0}", json.trim());
    }

    @Override
    public JsonObjectValue parse() throws JsonParseException {
        if (!json.startsWith("{")) {
            throw new JsonParseException("Parse error at index 0");
        }

        if (!json.endsWith("}")) {
            throw new JsonParseException("Parse error at index " + (json.length() - 1));
        }

        var subJsonStr = this.json.substring(1, this.json.length() - 1);
        return new JsonObjectValue(splitExpressions(subJsonStr));
    }

    private JsonValueList splitExpressions(String subJsonStr) {

        JsonValueList result = new JsonValueList();

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
