/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.apache.commons.lang3.stream.Streams.FailableStream;

/**
 * Base helper for building, signing, and sending Nostr events over WebSocket.
 */
@Getter
@NoArgsConstructor
public abstract class EventNostr extends NostrSpringWebSocketClient {

  @Setter private GenericEvent event;

  private PublicKey recipient;

  public EventNostr(@NonNull Identity sender) {
    super(sender);
  }

  /**
   * Sign the currently built event with the configured sender.
   *
   * @return this instance for chaining
   */
  public EventNostr sign() {
    super.sign(getSender(), event);
    return this;
  }

  /**
   * Send the current event to the configured relays and return the first response message.
   */
  public <U extends BaseMessage> U send() {
    return this.send(getRelays());
  }

  /**
   * Send the current event to the provided relays and return the first response message.
   *
   * @param relays relay map (name -> URI)
   */
  public <U extends BaseMessage> U send(Map<String, String> relays) {
    List<String> messages = super.sendEvent(this.event, relays);
    BaseMessageDecoder<U> decoder = new BaseMessageDecoder<>();

    return new FailableStream<>(messages.stream())
        .map(msg -> (U) decoder.decode(msg)).filter(Objects::nonNull).stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No message received"));
  }

  /**
   * Sign and send the current event using the configured relays.
   */
  public <U extends BaseMessage> U signAndSend() {
    return this.signAndSend(getRelays());
  }

  /**
   * Sign and send the current event using the provided relays.
   *
   * @param relays relay map (name -> URI)
   */
  public <U extends BaseMessage> U signAndSend(Map<String, String> relays) {
    return (U) sign().send(relays);
  }

  /**
   * Set the sender identity used for signing events.
   */
  public EventNostr setSender(@NonNull Identity sender) {
    super.setSender(sender);
    return this;
  }

  /**
   * Set the relays used when sending the current event.
   */
  public EventNostr setRelays(@NonNull Map<String, String> relays) {
    super.setRelays(relays);
    return this;
  }

  /**
   * Set the recipient public key (used by DMs or recipient-specific flows).
   */
  public EventNostr setRecipient(@NonNull PublicKey recipient) {
    this.recipient = recipient;
    return this;
  }

  /**
   * Replace the current event object and refresh its derived fields.
   *
   * @param event the new event instance
   */
  public void updateEvent(@NonNull GenericEvent event) {
    this.setEvent(event);
    this.event.update();
  }

  /**
   * Add a tag to the current event.
   *
   * @param tag the tag to add
   * @return this instance for chaining
   */
  public EventNostr addTag(@NonNull BaseTag tag) {
    getEvent().addTag(tag);
    return this;
  }
}
