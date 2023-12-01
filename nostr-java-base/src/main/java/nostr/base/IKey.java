
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface IKey {

    byte[] getRawData();

    String getBech32();
}
