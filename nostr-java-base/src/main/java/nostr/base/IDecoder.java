package nostr.base;

/**
 *
 * @author eric
 * @param <T>
 */
public interface IDecoder<T extends IElement> {

    public abstract T decode();
    
}
