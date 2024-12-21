/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.id.Identity;

import java.util.List;
import java.util.Map;

import nostr.event.BaseMessage;

/**
 * @author guilhermegps
 */
@Getter
@NoArgsConstructor
public abstract class EventNostr<T extends GenericEvent> extends NostrSpringWebSocketClient {

    @Setter
    private T event;

    private PublicKey recipient;

    public EventNostr(@NonNull Identity sender) {
        super(sender);
    }

    public EventNostr sign() {
        super.sign(getSender(), event);

        return this;
    }

    public <U extends BaseMessage> U send() {
        return this.send(getRelays());
    }

    @SuppressWarnings("unchecked")
    public <U extends BaseMessage> U send(Map<String, String> relays) {
        List<String> messages = super.send(this.event, relays);
        BaseMessageDecoder<U> decoder = new BaseMessageDecoder<U>();

        return messages.stream()
                .map(msg -> (U) decoder.decode(msg))
                .filter(msg -> msg != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No message received"));
    }

    public <U extends BaseMessage> U signAndSend() {
        return this.signAndSend(getRelays());
    }

    public <U extends BaseMessage> U signAndSend(Map<String, String> relays) {
        return (U) sign().send(relays);
    }

    public EventNostr setSender(@NonNull Identity sender) {
        super.setSender(sender);

        return this;
    }

    public EventNostr setRelays(@NonNull Map<String, String> relays) {
        super.setRelays(relays);

        return this;
    }

    public EventNostr setRecipient(@NonNull PublicKey recipient) {
        this.recipient = recipient;

        return this;
    }

    public EventNostr addTag(@NonNull BaseTag tag) {
        getEvent().addTag(tag);
        return this;
    }

    @NoArgsConstructor
    public static class GenericEventNostr<T extends GenericEvent> extends EventNostr<T> {

        public GenericEventNostr(@NonNull Identity sender) {
            super.setSender(sender);
        }

        /**
         * @param content
         * @return
         */
        public GenericEventNostr createGenericEvent(@NonNull Integer kind, @NonNull String content) {
            var factory = new GenericEventFactory(getSender(), kind, content);
            var event = factory.create();
            setEvent((T) event);

            return this;
        }

    }
}
