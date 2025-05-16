/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP42Impl;
import nostr.api.factory.impl.NIP42Impl.ChallengeTagFactory;
import nostr.api.factory.impl.NIP42Impl.ClientAuthenticationMessageFactory;
import nostr.api.factory.impl.NIP42Impl.RelayAuthenticationMessageFactory;
import nostr.api.factory.impl.NIP42Impl.RelaysTagFactory;
import nostr.base.Relay;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.tag.GenericTag;
import nostr.event.message.CanonicalAuthenticationMessage;

/**
 *
 * @author eric
 */
public class NIP42<T extends GenericEvent> extends EventNostr<T> {

    /**
     *
     * @param challenge
     * @param relay
     * @return
     */
    public NIP42<T> createCanonicalAuthenticationEvent(@NonNull String challenge, @NonNull Relay relay) {
        var factory = new NIP42Impl.CanonicalAuthenticationEventFactory(getSender(), challenge, relay);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }


    public NIP42<T> addRelayTag(@NonNull Relay relay) {
        var tag = createRelayTag(relay);
        var event = (CanonicalAuthenticationEvent) getEvent();
        event.addTag(tag);
        return this;
    }

    public NIP42<T> addChallengeTag(@NonNull String challenge) {
        var tag = createChallengeTag(challenge);
        var event = (CanonicalAuthenticationEvent) getEvent();
        event.addTag(tag);
        return this;
    }

    /**
     *
     * @param relay
     */
    public static GenericTag createRelayTag(@NonNull Relay relay) {
        return new RelaysTagFactory(relay).create();
    }

    /**
     *
     * @param challenge
     */
    public static GenericTag createChallengeTag(@NonNull String challenge) {
        return new ChallengeTagFactory(challenge).create();
    }

    /**
     *
     * @param event
     */
    public static CanonicalAuthenticationMessage createClientAuthenticationMessage(@NonNull CanonicalAuthenticationEvent event) {
        return new ClientAuthenticationMessageFactory(event).create();
    }

    /**
     *
     * @param challenge
     */
    public static GenericMessage createRelayAuthenticationMessage(@NonNull String challenge) {
        return new RelayAuthenticationMessageFactory(challenge).create();
    }
}
