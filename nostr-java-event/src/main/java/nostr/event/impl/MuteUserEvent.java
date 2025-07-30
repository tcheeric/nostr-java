package nostr.event.impl;

import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Mute User on Channel", nip = 28)
@NoArgsConstructor
public class MuteUserEvent extends GenericEvent {

    public MuteUserEvent(PublicKey pubKey, List<BaseTag> baseTagList, String content) {
        super(pubKey, Kind.MUTE_USER, baseTagList, content);
    }

    public PublicKey getMutedUser() {
        return ((PubKeyTag) getTags().get(0)).getPublicKey();
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Validate `tags` field for at least one PubKeyTag
        boolean hasValidPubKeyTag = this.getTags().stream()
                .anyMatch(tag -> tag instanceof PubKeyTag);
        if (!hasValidPubKeyTag) {
            throw new AssertionError("Invalid `tags`: Must include at least one valid PubKeyTag.");
        }
    }

    @Override
    protected void validateKind() {
        if (getKind() != Kind.MUTE_USER.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.MUTE_USER.getValue());
        }
    }
}
