
package nostr.event.tag;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "subject", nip = 14)
public final class SubjectTag extends BaseTag {

    @Key
    private String subject;

    public SubjectTag(@NonNull String subject) {
        this.subject = subject;
    }
}
