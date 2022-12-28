package nostr.json.parser.impl;

import nostr.json.parser.JsonParseException;
import nostr.json.parser.BaseParser;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;

/**
 *
 * @author eric
 */
@Log
public class JsonArrayParser extends BaseParser<ArrayValue> {

    private int cursor;

    public JsonArrayParser(@NonNull String json) {
        super(json.trim());
        cursor = 0;
        log.log(Level.FINE, "Parsing array {0}", json.trim());
    }

    @Override
    public ArrayValue parse() throws JsonParseException {
        if (!json.startsWith("[")) {
            throw new JsonParseException("Parse error at index 0");
        }

        if (!json.endsWith("]")) {
            throw new JsonParseException("Parse error at index " + (json.length() - 1));
        }

        var subJsonStr = this.json.substring(1, this.json.length() - 1);
        
        return new ArrayValue(splitList(subJsonStr));
    }

    private IValue[] splitList(String subJsonStr) throws JsonParseException {

        final List<IValue> list = new ArrayList<>();
        while (true) {
            if (cursor > subJsonStr.length() - 1) {
                break;
            }
            char c = subJsonStr.charAt(cursor);

            if (skip(c)) {
                cursor++;
            } else if (c == '\"') {
                parseString(c, subJsonStr, list);
            } else if (c == '[') {
                parseArray(c, subJsonStr, list);
            } else if (c == '{') {
                parseObject(c, subJsonStr, list);
            } else {
                parseNumber(subJsonStr, list);
            }
        }

        IValue[] result = new IValue[list.size()];
        list.toArray(result);
        
        return result;
    }

    private void parseNumber(String subJsonStr, final List<IValue> result) throws JsonParseException {
        String currentElt;
        int nextComma = subJsonStr.indexOf(',', cursor);
        if (nextComma == -1) {
            currentElt = subJsonStr.substring(cursor);
            cursor++;
        } else {
            currentElt = subJsonStr.substring(cursor, nextComma);
            cursor = nextComma;
        }
        if (currentElt.trim().matches("-?\\d+(\\.\\d+)?")) {
            result.add(new JsonNumberParser(currentElt).parse());
        } else {
            result.add(new JsonBooleanParser(currentElt).parse());
        }
    }

    private void parseString(char c, String subJsonStr, final List<IValue> result) throws JsonParseException {
        int closeIdx;
        String currentElt;
        closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr);
        if (closeIdx == -1) {
            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
            throw new JsonParseException("Parse error at index " + cursor);
        }
        currentElt = subJsonStr.substring(cursor, closeIdx + 1);
        result.add(new JsonStringParser(currentElt).parse());
        cursor = closeIdx + 1;
    }

    private void parseArray(char c, String subJsonStr, final List<IValue> result) throws JsonParseException {
        int closeIdx;
        String currentElt;
        closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr);
        if (closeIdx == -1) {
            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
            throw new JsonParseException("Parse error at index " + cursor);
        }
        currentElt = subJsonStr.substring(cursor, closeIdx + 1);
        result.add(new JsonArrayParser(currentElt).parse());
        cursor = closeIdx + 1;
    }

    private void parseObject(char c, String subJsonStr, final List<IValue> result) throws JsonParseException {
        int closeIdx;
        String currentElt;
        closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr);
        if (closeIdx == -1) {
            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
            throw new JsonParseException("Parse error at index " + cursor);
        }
        currentElt = subJsonStr.substring(cursor, closeIdx + 1);
        result.add(new JsonObjectParser(currentElt).parse());
        cursor = closeIdx + 1;
    }

}
