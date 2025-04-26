/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.factory.impl.GenericTagFactory;
import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.GenericMessage;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eric
 */
public class NIP42 extends EventNostr {

    /**
     *
     * @param challenge
     * @param relay
     * @return
     */
    public NIP42 createCanonicalAuthenticationEvent(@NonNull String challenge, @NonNull Relay relay) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.EVENT_DELETION,"").create();
        this.addChallengeTag(challenge);
        this.addRelayTag(relay);
        this.updateEvent(genericEvent);

        return this;
    }


    public NIP42 addRelayTag(@NonNull Relay relay) {
        var tag = createRelayTag(relay);
        getEvent().addTag(tag);
        return this;
    }

    public NIP42 addChallengeTag(@NonNull String challenge) {
        var tag = createChallengeTag(challenge);
        getEvent().addTag(tag);
        return this;
    }

    /**
     *
     * @param relay
     */
    public static GenericTag createRelayTag(@NonNull Relay relay) {
        return new GenericTagFactory(Constants.Tag.RELAY_CODE, relay.getUri()).create();
    }

    /**
     *
     * @param challenge
     */
    public static GenericTag createChallengeTag(@NonNull String challenge) {
        return new GenericTagFactory(Constants.Tag.CHALLENGE_CODE, challenge).create();
    }

    /**
     *
     * @param event
     */
    public static CanonicalAuthenticationMessage createClientAuthenticationMessage(@NonNull CanonicalAuthenticationEvent event) {
        return new CanonicalAuthenticationMessage(event);
    }

    /**
     *
     * @param challenge
     */
    public static GenericMessage createRelayAuthenticationMessage(@NonNull String challenge) {
        final List<ElementAttribute> attributes = new ArrayList<>();
        final var attr = new ElementAttribute("challenge", challenge);
        attributes.add(attr);
        return new GenericMessage(Command.AUTH.name(), attributes);
    }
}