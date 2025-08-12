package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;

import java.util.List;

/**
 * @author squirrel
 */
@Event(name = "Canonical authentication event", nip = 42)
@NoArgsConstructor
public class CanonicalAuthenticationEvent extends EphemeralEvent {

    public CanonicalAuthenticationEvent(@NonNull PublicKey pubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
        super(pubKey, Kind.CLIENT_AUTH, tags, content);
    }

    public String getChallenge() {
        BaseTag challengeTag = getTag("challenge");
        if (challengeTag != null && !((GenericTag) challengeTag).getAttributes().isEmpty()) {
            return ((GenericTag) challengeTag).getAttributes().get(0).value().toString();
        }
        return null;
    }

    public Relay getRelay() {
        BaseTag relayTag = getTag("relay");
        if (relayTag != null && !((GenericTag) relayTag).getAttributes().isEmpty()) {
            return new Relay(((GenericTag) relayTag).getAttributes().get(0).value().toString());
        }
        return null;
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Check 'challenge' tag
        BaseTag challengeTag = getTag("challenge");
        if (challengeTag == null || ((GenericTag) challengeTag).getAttributes().isEmpty()) {
            throw new AssertionError("Missing or invalid `challenge` tag.");
        }

        // Check 'relay' tag
        BaseTag relayTag = getTag("relay");
        if (relayTag == null || ((GenericTag) relayTag).getAttributes().isEmpty()) {
            throw new AssertionError("Missing or invalid `relay` tag.");
        }
    }

    @Override
    public void validateKind() {
        if (getKind() != Kind.CLIENT_AUTH.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.CLIENT_AUTH.getValue());
        }
    }
}
