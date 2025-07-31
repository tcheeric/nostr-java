/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.id.Identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import reactor.core.publisher.Mono;

/**
 * @author guilhermegps
 */
@Getter
@NoArgsConstructor
public abstract class EventNostr extends NostrSpringWebSocketClient {

    @Setter
    private GenericEvent event;

    private PublicKey recipient;

    public EventNostr(@NonNull Identity sender) {
        super(sender);
    }

    public EventNostr sign() {
        super.sign(getSender(), event);
        return this;
    }

    public <U extends BaseMessage> Mono<U> send() {
        return this.send(getRelays());
    }

    public <U extends BaseMessage> Mono<U> send(Map<String, String> relays) {
        BaseMessageDecoder<U> decoder = new BaseMessageDecoder<>();

        return super.sendEvent(this.event, relays)
                .map(msg -> {
                    try {
                        return (U) decoder.decode(msg);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("No message received")));
    }

    public <U extends BaseMessage> Mono<U> signAndSend() {
        return this.signAndSend(getRelays());
    }

    public <U extends BaseMessage> Mono<U> signAndSend(Map<String, String> relays) {
        return sign().send(relays);
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

    public void updateEvent(@NonNull GenericEvent event) {
        this.setEvent(event);
        this.event.update();
    }

    public EventNostr addTag(@NonNull BaseTag tag) {
        getEvent().addTag(tag);
        return this;
    }
}
