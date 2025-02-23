package nostr.base;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 * @author eric
 * @param <T>
 */
public interface IDecoder<T extends IElement> {

    T decode(String str) throws JsonProcessingException;

}
