package com.tcheeric.nostr.json.unmarshaller;

import com.tcheeric.nostr.json.parser.JsonParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@Log
@ToString
@EqualsAndHashCode
public abstract class BaseUnmarshaller implements IUnmarshaller {

    private final String json;

    public BaseUnmarshaller(@NonNull String json) {
        this.json = json.trim();
    }

    protected List<String> splitList(String subJsonStr) {

        int cursor = 0;
        int closeIdx = 0;
        String currentElt = null;
        final List<String> result = new ArrayList<>();

        while (true) {
            if (cursor > subJsonStr.length() - 1) {
                break;
            }
            char c = subJsonStr.charAt(cursor);

            if (skip(c)) {
                cursor++;
            } else if (c == '\"') {
                closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr);//subJsonStr.indexOf('\"', cursor + 1);
                if (closeIdx == -1) {
                    log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                    throw new JsonParseException("Parse error at index " + cursor);
                }

                currentElt = subJsonStr.substring(cursor, closeIdx + 1);
                result.add(currentElt);
                cursor = closeIdx + 1;
            } else if (c == '[') {
                closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr);//subJsonStr.indexOf("]", cursor + 1);
                if (closeIdx == -1) {
                    log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                    throw new JsonParseException("Parse error at index " + cursor);
                }

                currentElt = subJsonStr.substring(cursor, closeIdx + 1);
                result.add(currentElt);
                cursor = closeIdx + 1;
            } else if (c == '{') {
                closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr);//subJsonStr.indexOf("}", cursor + 1);
                if (closeIdx == -1) {
                    log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                    throw new JsonParseException("Parse error at index " + cursor);
                }

                currentElt = subJsonStr.substring(cursor, closeIdx + 1);
                result.add(currentElt);
                cursor = closeIdx + 1;
            } else {

                int nextComma = subJsonStr.indexOf(',', cursor);
                if (nextComma == -1) {
                    currentElt = subJsonStr.substring(cursor);
                    result.add(currentElt.trim());
                    cursor++;
                } else {
                    currentElt = subJsonStr.substring(cursor, nextComma);
                    result.add(currentElt.trim());
                    cursor = nextComma;
                }
            }
        }

        return result;

    }

    protected Map<String, String> splitExpressions(String subJsonStr) {

        int cursor = 0;
        int closeIdx = 0;
        String currentKey, currentValue = null;
        final Map<String, String> result = new HashMap<>();

        while (true) {
            if (cursor > subJsonStr.length() - 1) {
                break;
            }
            char b = cursor > 0 ? subJsonStr.charAt(cursor - 1) : 0;
            char c = subJsonStr.charAt(cursor);

            log.log(Level.FINEST, "1. cursor: {0}: {1} {2}", new Object[]{cursor, closeIdx, String.valueOf(c)});

            if (skip(c)) {
                cursor++;
            } else if (c == '\"') { // -- Get the expression varaiable
                closeIdx = subJsonStr.indexOf("\"", cursor + 1);
                if (closeIdx == -1) {
                    log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                    throw new JsonParseException(String.format("Parse error at index %d", cursor));
                }

                currentKey = subJsonStr.substring(cursor, closeIdx + 1);
                log.log(Level.FINEST, "Key {0}", currentKey);
                cursor = closeIdx + 1;
                if (cursor >= subJsonStr.length()) {
                    throw new JsonParseException(String.format("Parse error at index %d", subJsonStr.length()));
                }

                c = subJsonStr.charAt(cursor);
                if (c == ':') {
                    while (skip(c)) {
                        cursor++;

                        if (cursor >= subJsonStr.length()) {
                            throw new JsonParseException("Parse error at position " + subJsonStr.length());
                        }

                        c = subJsonStr.charAt(cursor);
                    }

                    log.log(Level.FINEST, "2. cursor: {0}", cursor);

                    if (c == '\"') { // -- Get the string expression value 
                        closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr); //subJsonStr.indexOf('\"', cursor + 1);
                        if (closeIdx == -1) {
                            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                            throw new JsonParseException(String.format("Parse error at index %d", cursor));
                        }

                        currentValue = subJsonStr.substring(cursor, closeIdx + 1);
                        cursor = closeIdx + 1;
                    } else if (c == '[') { // -- Get the array expression value 
                        closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr); //subJsonStr.indexOf("]", cursor + 1);
                        if (closeIdx == -1) {
                            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                            throw new JsonParseException(String.format("Parse error at index %d", cursor));
                        }

                        currentValue = subJsonStr.substring(cursor, closeIdx + 1);
                        cursor = closeIdx + 1;
                    } else if (c == '{') { // -- Get the object expression value 
                        closeIdx = getClosedParenIndex(c, cursor + 1, subJsonStr);//subJsonStr.indexOf("}", cursor + 1);
                        if (closeIdx == -1) {
                            log.log(Level.SEVERE, "Parse error at index {0}", cursor);
                            throw new JsonParseException(String.format("Parse error at index %d", cursor));
                        }

                        currentValue = subJsonStr.substring(cursor, closeIdx + 1);
                        cursor = closeIdx + 1;
                    } else { // -- Get the number expression value 

                        var nextComma = subJsonStr.indexOf(',', cursor);
                        if (nextComma == -1) {
                            currentValue = subJsonStr.substring(cursor);
                        } else {
                            currentValue = subJsonStr.substring(cursor, nextComma);
                            cursor = nextComma;
                        }
                    }
                } else {
                    log.log(Level.SEVERE, "2.Parse error at index {0}", cursor);
                    throw new JsonParseException(String.format("Parse error at index %d", cursor));
                }
                result.put(currentKey, currentValue.trim());
                cursor++;
            } else {
                log.log(Level.SEVERE, "1.Parse error at index {0}", cursor);
                throw new JsonParseException(String.format("Parse error at index %d: %s", cursor, json));
            }
        }

        return result;
    }

    private boolean skip(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == ',' || c == ':');
    }

    private int getClosedParenIndex(char openParen, int startIndex, String source) {

        if (startIndex < 0) {
            throw new AssertionError();
        }

        if (startIndex > source.length() - 1) {
            throw new JsonParseException("Parse error at index " + (startIndex - 1));
        }

        int square = 0, curly = 0, doubleQuote = 0, singleQuote = 0, paren = 0;

        char closeParen = 0;
        switch (openParen) {
            case '[':
                closeParen = ']';
                break;
            case '{':
                closeParen = '}';
                break;
            case '\"':
                closeParen = '\"';
                break;
            case '(':
                closeParen = ')';
                break;
            case '\'':
                closeParen = '\'';
            default:
                throw new AssertionError();
        }

        for (int i = startIndex; i < source.length(); i++) {

            final char charAt = source.charAt(i);

            if (charAt == closeParen && (square == 0 && curly == 0 && doubleQuote == 0 && singleQuote == 0 && paren == 0)) {
                return i;
            } else if (charAt == '[') {
                square++;
            } else if (charAt == '(') {
                paren++;
            } else if (charAt == '\'') {
                if (singleQuote > 0) {
                    singleQuote--;
                } else if (singleQuote == 0) {
                    singleQuote++;
                } else {
                    throw new AssertionError();
                }
            } else if (charAt == '\"') {
                if (doubleQuote > 0) {
                    doubleQuote--;
                } else if (doubleQuote == 0) {
                    doubleQuote++;
                } else {
                    throw new AssertionError();
                }
            } else if (charAt == '{') {
                curly++;
            } else if (charAt == ']') {
                if (square > 0) {
                    square--;
                } else {
                    throw new JsonParseException("Parse error at index " + i);
                }
            } else if (charAt == ')') {
                if (paren > 0) {
                    paren--;
                } else {
                    throw new JsonParseException("Parse error at index " + i);
                }
            } else if (charAt == '}') {
                if (curly > 0) {
                    curly--;
                } else {
                    throw new JsonParseException("Parse error at index " + i);
                }
            } else {
                // DO NOTHING
            }
        }

        return -1;
    }

}
