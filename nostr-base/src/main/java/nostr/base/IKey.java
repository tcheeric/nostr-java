
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface IKey extends Bech32Encodable {

    public abstract byte[] getRawData();
}
