/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import nostr.event.BaseTag;
import nostr.event.impl.ClientAuthenticationEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.message.ClientAuthenticationMessage;

/**
 *
 * @author eric
 */
public class NIP42 {

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

        public ClientAuthenticationEventFactory(List<BaseTag> tags, @NonNull String challenge, @NonNull Relay relay) {
            super(tags, null);
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
    public static class RelaysTagFactory extends TagFactory {

        public RelaysTagFactory(List<Relay> relays) {
            super("relays", 42, relays.stream().map(r -> r.getUri()).collect(Collectors.joining(",")));
        }

        public RelaysTagFactory(Relay relay) {
            super("relays", 42, relay.getUri());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ChallengeTagFactory extends TagFactory {

        public ChallengeTagFactory(String challenge) {
            super("challenge", 42, challenge);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class RelayAuthenticationMessageFactory extends MessageFactory<GenericMessage> {

        private final @NonNull
        String challenge;

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

        private final @NonNull
        ClientAuthenticationEvent event;

        @Override
        public ClientAuthenticationMessage create() {
            return new ClientAuthenticationMessage(event);
        }
    }

}
