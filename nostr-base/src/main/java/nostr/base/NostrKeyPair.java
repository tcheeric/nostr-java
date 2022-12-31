
package nostr.base;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.annotation.JsonString;

/**
 *
 * @author squirrel
 */
@Data
@Log
@ToString
@Builder
@Deprecated(forRemoval = true)
public final class NostrKeyPair {

    @JsonString
    private final PublicKey publicKey;

    @JsonString
    @ToString.Exclude
    private final PrivateKey privateKey;
}
