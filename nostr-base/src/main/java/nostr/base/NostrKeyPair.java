
package nostr.base;

import com.tcheeric.nostr.base.annotation.JsonString;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@Log
@ToString
@Builder
public final class NostrKeyPair {

    @JsonString
    private final PublicKey publicKey;

    @JsonString
    @ToString.Exclude
    private final PrivateKey privateKey;
}
