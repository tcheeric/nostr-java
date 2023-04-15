
package nostr.base;

import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IKey {

    public abstract byte[] getRawData();

    public String getBech32() throws NostrException;
}
