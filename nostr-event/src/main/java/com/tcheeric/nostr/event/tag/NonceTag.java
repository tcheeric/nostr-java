
package com.tcheeric.nostr.event.tag;

import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.Tag;
import com.tcheeric.nostr.event.BaseTag;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import lombok.ToString;

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
