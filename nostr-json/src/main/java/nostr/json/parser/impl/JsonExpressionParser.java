package nostr.json.parser.impl;

import nostr.json.parser.JsonParseException;
import nostr.json.JsonValue;
import nostr.json.parser.BaseParser;
import nostr.json.values.JsonExpression;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class JsonExpressionParser extends BaseParser<JsonExpression> {

    private int cursor;

    public JsonExpressionParser(String json) {
        super(json.trim());
        this.cursor = 0;
        log.log(Level.FINE, "Parsing expression {0}", json.trim());
    }

    @Override
    public JsonExpression parse() throws JsonParseException {
        final String variable = getVariable();
        final JsonValue value = getValue();

        return JsonExpression.builder().jsonValue(value).variable(variable).build();
    }

    private String getVariable() {

        char c = json.charAt(cursor);
        if (c == '\"') { // -- Get the expression varaiable
            int closeIdx = json.indexOf("\"", cursor + 1);
            if (closeIdx == -1) {
                log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
            }

            String currentKey = json.substring(cursor, closeIdx + 1);
            log.log(Level.FINEST, "Key {0}", currentKey);

            cursor = closeIdx + 1;

            if (cursor >= json.length()) {
                throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
            }

            return currentKey;
        }
        throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
    }

    private JsonValue getValue() {

        char c = json.charAt(cursor);
        if (c == ':') {            
            while (skip(c)) {
                cursor++;
                if (cursor >= json.length()) {
                    throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
                }
                c = json.charAt(cursor);
            }
            
            return switch (c) {
                case '\"' -> getStringValue(c);
                case '[' -> getArrayValue(c);
                case '{' -> getObjectValue(c);
                default -> getNumberValue();
            }; 
        } else {
            log.log(Level.SEVERE, "2.Parse error at index {0}", cursor);
            throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
        }
    }

    private JsonValue getNumberValue() throws JsonParseException {
        String currentValue;

        var nextComma = json.indexOf(',', cursor);
        if (nextComma == -1) {
            currentValue = json.substring(cursor);
        } else {
            currentValue = json.substring(cursor, nextComma);
            cursor = nextComma;
        }
        if (currentValue.trim().matches("-?\\d+(\\.\\d+)?")) {
            return new JsonNumberParser(currentValue).parse();
        } else {
            return new JsonBooleanParser(currentValue).parse();
        }
    }

    private JsonValue getObjectValue(char c) throws JsonParseException {
        int closeIdx;
        String currentValue;
        
        closeIdx = getClosedParenIndex(c, cursor + 1, json);
        if (closeIdx == -1) {
            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
            throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
        }
        currentValue = json.substring(cursor, closeIdx + 1);
        cursor = closeIdx + 1;
        return new JsonObjectParser(currentValue).parse();
    }

    private JsonValue getArrayValue(char c) throws JsonParseException {
        int closeIdx;
        String currentValue;
        
        closeIdx = getClosedParenIndex(c, cursor + 1, json);
        if (closeIdx == -1) {
            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
            throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
        }
        currentValue = json.substring(cursor, closeIdx + 1);
        cursor = closeIdx + 1;
        return new JsonArrayParser(currentValue).parse();
    }

    private JsonValue getStringValue(char c) throws JsonParseException {
        int closeIdx;
        String currentValue;
        
        closeIdx = getClosedParenIndex(c, cursor + 1, json);
        if (closeIdx == -1) {
            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
            throw new JsonParseException(String.format("Invalid expression: %s (index %d)", json, cursor));
        }
        currentValue = json.substring(cursor, closeIdx + 1);
        cursor = closeIdx + 1;
        return new JsonStringParser(currentValue).parse();
    }
}
