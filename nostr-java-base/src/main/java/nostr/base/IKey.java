
package nostr.base;

import java.io.Serializable;

/**
 *
 * @author squirrel
 */
public interface IKey extends Serializable {

    byte[] getRawData();

    String toBech32String();
}
