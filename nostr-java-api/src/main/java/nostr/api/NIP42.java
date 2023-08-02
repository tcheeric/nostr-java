/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.api.factory.MessageFactory;
import nostr.api.factory.TagFactory;
import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.Relay;
import nostr.client.Client;
import nostr.event.impl.ClientAuthenticationEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.impl.GenericTag;
import nostr.event.message.ClientAuthenticationMessage;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class NIP42 extends Nostr {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ClientAuthenticationEventFactory extends EventFactory<ClientAuthenticationEvent> {

        private final String challenge;
        private final Relay relay;

        public ClientAuthenticationEventFactory(@NonNull String challenge, @NonNull Relay relay) {
            super(null);
            this.challenge = challenge;
            this.relay = relay;
        }

        @Override
        public ClientAuthenticationEvent create() {
            return new ClientAuthenticationEvent(getSender(), challenge, relay);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class RelayTagFactory extends TagFactory<GenericTag> {

        private final @NonNull
        Relay relay;

        @Override
        public GenericTag create() {
            final var relayAttribute = ElementAttribute.builder().nip(42).name("uri").value(relay.getUri()).build();
            final Set<ElementAttribute> relayAttributes = new HashSet<>();
            relayAttributes.add(relayAttribute);
            return new GenericTag("relay", 42, relayAttributes);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class ChallengeTagFactory extends TagFactory<GenericTag> {

        private final @NonNull
        String challenge;

        @Override
        public GenericTag create() {
            final var relayAttribute = ElementAttribute.builder().nip(42).name("challenge").value(challenge).build();
            final Set<ElementAttribute> relayAttributes = new HashSet<>();
            relayAttributes.add(relayAttribute);
            return new GenericTag("challenge", 42, relayAttributes);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class RelayAuthenticationMessageFactory extends MessageFactory<GenericMessage> {
        
        private final @NonNull String challenge;

        @Override
        public GenericMessage create() {
            final Set<ElementAttribute> attributes = new HashSet<>();
            final var attr = new ElementAttribute("challenge", challenge, 42);
            attributes.add(attr);
            return new GenericMessage(Command.AUTH.name(), attributes, 42);
        }
    }
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class ClientAuthenticationMessageFactory extends MessageFactory<ClientAuthenticationMessage> {
        
        private final @NonNull ClientAuthenticationEvent event;

        @Override
        public ClientAuthenticationMessage create() {
            return new ClientAuthenticationMessage(event);
        }
    }

    public static void auth(String challenge, Relay relay) throws NostrException {
        Client client = Nostr.createClient();
        client.auth(challenge, relay);
    }

}
