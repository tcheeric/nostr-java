
package nostr.event.tag;

import nostr.base.annotation.Key;
import nostr.event.BaseTag;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.NIPSupport;
import lombok.ToString;
import nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Log
@Tag(code = "nonce")
@NIPSupport(13)
@ToString
public class NonceTag extends BaseTag {

    @Key
    private final Integer nonce;

    @Key
    private final Integer difficulty;

    public NonceTag(@NonNull Integer nonce, @NonNull Integer difficulty) {
        this.nonce = nonce;
        this.difficulty = difficulty;
    }
}
