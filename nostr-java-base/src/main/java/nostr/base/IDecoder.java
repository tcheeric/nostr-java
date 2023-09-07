package nostr.base;

import nostr.util.NostrException;

/**
 *
 * @author eric
 * @param <T>
 */
public interface IDecoder<T extends IElement> {

    public abstract T decode() throws NostrException;    
    
}
