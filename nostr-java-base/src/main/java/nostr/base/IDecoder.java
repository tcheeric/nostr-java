package nostr.base;

/**
 *
 * @author eric
 * @param <T>
 */
public interface IDecoder<T extends IElement> {

    T decode(String str);

}
