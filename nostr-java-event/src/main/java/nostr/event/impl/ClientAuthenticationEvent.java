package nostr.event.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.list.TagList;

/**
 *
 * @author squirrel
 */
@Event(name = "Authentication of clients to relays", nip = 42)
public class ClientAuthenticationEvent extends GenericEvent {

    public ClientAuthenticationEvent(@NonNull PublicKey pubKey, @NonNull TagList tags) {
        super(pubKey, Kind.CLIENT_AUTH, tags);
    }

    public ClientAuthenticationEvent(@NonNull PublicKey pubKey, String challenge, @NonNull Set<Relay> relays) {
        super(pubKey, Kind.CLIENT_AUTH);

        Set<ElementAttribute> chAttributes = new HashSet<>();
        var attribute = ElementAttribute.builder().nip(42).name("challenge").value(challenge).build();
        chAttributes.add(attribute);

        this.setTags(new TagList());
        ITag chTag = new GenericTag(42, "challenge", chAttributes);

        this.addTag(chTag);

        relays.stream().forEach(r -> {
            try {
                final Set<ElementAttribute> relayAttributes = new HashSet<>();
                final ElementAttribute relayAttribute = getRelayAttribute(r);
                relayAttributes.add(relayAttribute);
                final ITag relayTag = new GenericTag(42, "relay", relayAttributes);
                this.addTag(relayTag);
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        });

        this.setNip(42);
    }

    public ClientAuthenticationEvent(@NonNull PublicKey pubKey, String challenge, @NonNull Relay relay) {
        super(pubKey, Kind.CLIENT_AUTH);

        try {
            Set<ElementAttribute> chAttributes = new HashSet<>();
            var attribute = ElementAttribute.builder().nip(42).name("challenge").value(challenge).build();
            chAttributes.add(attribute);

            this.setTags(new TagList());
            ITag chTag = new GenericTag(42, "challenge", chAttributes);

            this.addTag(chTag);

            final Set<ElementAttribute> relayAttributes = new HashSet<>();
            final ElementAttribute relayAttribute = getRelayAttribute(relay);
            relayAttributes.add(relayAttribute);
            final ITag relayTag = new GenericTag(42, "relay", relayAttributes);
            this.addTag(relayTag);

            this.setNip(42);
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ElementAttribute getRelayAttribute(Relay relay) throws ExecutionException, InterruptedException {
        return ElementAttribute.builder().nip(42).name("uri").value(relay.getUri()).build();
    }

}
