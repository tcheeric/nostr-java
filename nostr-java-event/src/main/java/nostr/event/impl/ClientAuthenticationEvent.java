package nostr.event.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;

/**
 *
 * @author squirrel
 */
@Event(name = "Authentication of clients to relays", nip = 42)
public class ClientAuthenticationEvent extends GenericEvent {

    public ClientAuthenticationEvent(@NonNull PublicKey pubKey, @NonNull List<? extends BaseTag> tags) {
        super(pubKey, Kind.CLIENT_AUTH, tags);
    }

    public ClientAuthenticationEvent(@NonNull PublicKey pubKey, String challenge, @NonNull Set<Relay> relays) {
        super(pubKey, Kind.CLIENT_AUTH);

        Set<ElementAttribute> chAttributes = new HashSet<>();
        var attribute = ElementAttribute.builder().nip(42).name("challenge").value(challenge).build();
        chAttributes.add(attribute);

        this.setTags(new ArrayList<>());
        ITag chTag = new GenericTag("auth", 42, chAttributes);

        this.addTag((GenericTag) chTag);

        relays.stream().forEach(r -> {
            try {
                final Set<ElementAttribute> relayAttributes = new HashSet<>();
                final ElementAttribute relayAttribute = getRelayAttribute(r);
                relayAttributes.add(relayAttribute);
                final ITag relayTag = new GenericTag("relay", 42, relayAttributes);
                this.addTag((BaseTag) relayTag);
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

            this.setTags(new ArrayList<>());
            ITag chTag = new GenericTag("auth", 42, chAttributes);

            this.addTag((BaseTag) chTag);

            final Set<ElementAttribute> relayAttributes = new HashSet<>();
            final ElementAttribute relayAttribute = getRelayAttribute(relay);
            relayAttributes.add(relayAttribute);
            final ITag relayTag = new GenericTag("relay", 42, relayAttributes);
            this.addTag((BaseTag) relayTag);

            this.setNip(42);
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ElementAttribute getRelayAttribute(Relay relay) throws ExecutionException, InterruptedException {
        return ElementAttribute.builder().nip(42).name("uri").value(relay.getUri()).build();
    }

}
