/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

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
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class NIP42Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class CanonicalAuthenticationEventFactory extends EventFactory<CanonicalAuthenticationEvent> {

        private final String challenge;
        private final Relay relay;

        public CanonicalAuthenticationEventFactory(@NonNull String challenge, @NonNull Relay relay) {
            this.challenge = challenge;
            this.relay = relay;
        }

        public CanonicalAuthenticationEventFactory(@NonNull Identity sender, @NonNull String challenge, @NonNull Relay relay) {
            super(sender, null);
            this.challenge = challenge;
            this.relay = relay;
        }

        public CanonicalAuthenticationEventFactory(@NonNull Identity sender, @NonNull List<BaseTag> tags, @NonNull String challenge, @NonNull Relay relay) {
            super(sender, tags, null);
            this.challenge = challenge;
            this.relay = relay;
        }

        @Override
        public CanonicalAuthenticationEvent create() {
            return new CanonicalAuthenticationEvent(getSender(), challenge, relay);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class RelaysTagFactory extends TagFactory {

        public RelaysTagFactory(List<Relay> relays) {
            super("relay", 42, relays.stream().map(r -> r.getUri()).collect(Collectors.joining(",")));
        }

        public RelaysTagFactory(Relay relay) {
            super("relay", 42, relay.getUri());
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

        @NonNull
        private final String challenge;

        @Override
        public GenericMessage create() {
            final List<ElementAttribute> attributes = new ArrayList<>();
            final var attr = new ElementAttribute("challenge", challenge);
            attributes.add(attr);
            return new GenericMessage(Command.AUTH.name(), attributes, 42);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class ClientAuthenticationMessageFactory extends MessageFactory<CanonicalAuthenticationMessage> {

        @NonNull
        private final CanonicalAuthenticationEvent event;

        @Override
        public CanonicalAuthenticationMessage create() {
            return new CanonicalAuthenticationMessage(event);
        }
    }

}
