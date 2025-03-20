package nostr.event.impl;

import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;

import java.util.ArrayList;
import java.util.List;

/**
 * @author squirrel
 */
@Event(name = "Canonical authentication event", nip = 42)
public class CanonicalAuthenticationEvent extends GenericEvent {

    public CanonicalAuthenticationEvent(@NonNull PublicKey pubKey, @NonNull String challenge, @NonNull Relay relay) {
        super(pubKey, Kind.CLIENT_AUTH);
        this.setNip(42);

        // Challenge tag
        List<ElementAttribute> chAttributes = new ArrayList<>();
        var attribute = ElementAttribute.builder().name("challenge").value(challenge).build();
        chAttributes.add(attribute);
        BaseTag challengeTag = new GenericTag("challenge", chAttributes);
        this.addTag(challengeTag);

        // Relay tag
        final List<ElementAttribute> relayAttributes = new ArrayList<>();
        final ElementAttribute relayAttribute = ElementAttribute.builder().name("uri").value(relay.getUri()).build();
        relayAttributes.add(relayAttribute);
        final BaseTag relayTag = new GenericTag("relay", relayAttributes);
        this.addTag(relayTag);
    }
}
