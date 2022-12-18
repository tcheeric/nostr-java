package nostr.json.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author squirrel
 * @param <T>
 */
@Data
@AllArgsConstructor
public abstract class BaseParser<T> implements IParser<T> {

    protected final String json;

    protected boolean skip(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == ',' || c == ':');
    }

    protected int getClosedParenIndex(char openParen, int startIndex, String source) {

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
                    throw new JsonParseException(String.format("Invalid expression: %s (index %d)", source, i));
                }
            } else if (charAt == ')') {
                if (paren > 0) {
                    paren--;
                } else {
                    throw new JsonParseException(String.format("Invalid expression: %s (index %d)", source, i));
                }
            } else if (charAt == '}') {
                if (curly > 0) {
                    curly--;
                } else {
                    throw new JsonParseException(String.format("Invalid expression: %s (index %d)", source, i));
                }
            } else {
                // DO NOTHING
            }
        }

        return -1;
    }

}
