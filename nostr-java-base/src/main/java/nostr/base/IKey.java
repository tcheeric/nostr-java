
package nostr.base;

import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IKey {

    byte[] getRawData();

    String getBech32() throws NostrException;
}
