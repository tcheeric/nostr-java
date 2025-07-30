
package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP09Event;
import nostr.event.tag.EventTag;

import java.util.List;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Event Deletion", nip = 9)
@NoArgsConstructor
public class DeletionEvent extends NIP09Event {

    public DeletionEvent(PublicKey pubKey, List<BaseTag> tags, String content) {        
        super(pubKey, Kind.DELETION, tags, content);        
    }

    public DeletionEvent(PublicKey pubKey, List<BaseTag> tags) {        
        this(pubKey, tags, "Deletion request");
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Validate `tags` field for at least one `EventTag` or `AuthorTag`
        if (this.getTags() == null || this.getTags().isEmpty()) {
            throw new AssertionError("Invalid `tags`: Must include at least one `e` or `a` tag.");
        }

        boolean hasEventOrAuthorTag = this.getTags().stream()
                .anyMatch(tag -> tag instanceof EventTag || tag.getCode().equals("a"));
        if (!hasEventOrAuthorTag) {
            throw new AssertionError("Invalid `tags`: Must include at least one `e` or `a` tag.");
        }

        // Validate `tags` field for `KindTag` (`k` tag)
        boolean hasKindTag = this.getTags().stream()
                .anyMatch(tag -> tag.getCode().equals("k"));
        if (!hasKindTag) {
            throw new AssertionError("Invalid `tags`: Should include a `k` tag for the kind of each event being requested for deletion.");
        }
    }

    @Override
    protected void validateKind() {
        if (getKind() != Kind.DELETION.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.DELETION.getValue());
        }
    }
}
