
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface IKey extends IBech32Encodable {

    public abstract byte[] getRawData();
}
