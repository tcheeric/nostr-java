
package nostr.event.tag;

import nostr.event.BaseTag;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.annotation.Key;
import nostr.base.annotation.NIPSupport;
import nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "subject")
@ToString
@NIPSupport(14)
public final class SubjectTag extends BaseTag {

    @Key
    private String subject;

    public SubjectTag(@NonNull String subject) {
        this.subject = subject;
    }
}
