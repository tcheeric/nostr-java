package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
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
        GenericTag challengeTag = getTag("challenge");
        if (challengeTag != null && !challengeTag.getAttributes().isEmpty()) {
            return challengeTag.getAttributes().get(0).getValue().toString();
        }
        return null;
    }

    public Relay getRelay() {
        GenericTag relayTag = getTag("relay");
        if (relayTag != null && !relayTag.getAttributes().isEmpty()) {
            return new Relay(relayTag.getAttributes().get(0).getValue().toString());
        }
        return null;
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Check 'challenge' tag
        GenericTag challengeTag = getTag("challenge");
        if (challengeTag == null || challengeTag.getAttributes().isEmpty()) {
            throw new AssertionError("Missing or invalid `challenge` tag.");
        }

        // Check 'relay' tag
        GenericTag relayTag = getTag("relay");
        if (relayTag == null || relayTag.getAttributes().isEmpty()) {
            throw new AssertionError("Missing or invalid `relay` tag.");
        }
    }
}
