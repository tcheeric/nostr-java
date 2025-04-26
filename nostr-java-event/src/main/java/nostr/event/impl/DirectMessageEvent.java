package nostr.event.impl;

import java.util.List;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.event.NIP04Event;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author squirrel
 */
@NoArgsConstructor
@Event(name = "Encrypted Direct Message", nip = 4)
public class DirectMessageEvent extends NIP04Event {

    public DirectMessageEvent(PublicKey sender, List<BaseTag> tags, String content) {
        super(sender, Kind.ENCRYPTED_DIRECT_MESSAGE, tags, content);
    }
    
    public DirectMessageEvent(@NonNull PublicKey sender, @NonNull PublicKey recipient, @NonNull String content) {
        super(sender, Kind.ENCRYPTED_DIRECT_MESSAGE);
        this.setContent(content);        
        this.addTag(PubKeyTag.builder().publicKey(recipient).build());        
    }

    @Override
    protected void validateTags() {

        super.validateTags();

        // Validate `tags` field for recipient's public key
        boolean hasRecipientTag = this.getTags().stream()
                .anyMatch(tag -> tag instanceof PubKeyTag);
        if (!hasRecipientTag) {
            throw new AssertionError("Invalid `tags`: Must include a PubKeyTag for the recipient.");
        }
    }
}
