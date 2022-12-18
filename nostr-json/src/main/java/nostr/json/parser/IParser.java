
package nostr.json.parser;

/**
 *
 * @author squirrel
 * @param <T>
 */
public interface IParser<T> {
    
    public abstract T parse() throws JsonParseException;
}
