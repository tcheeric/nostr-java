package nostr.base;

/**
 *
 * @author eric
 * @param <T>
 */
public interface FDecoder<T> {

    T decode(Class<T> clazz);
    
}
