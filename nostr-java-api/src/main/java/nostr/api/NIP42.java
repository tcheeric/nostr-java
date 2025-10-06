/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.event.message.GenericMessage;

/**
 * NIP-42 helpers (Authentication). Build auth events and AUTH messages.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/42.md">NIP-42</a>
 */
public class NIP42 extends EventNostr {

  /**
   * Create a canonical authentication event (NIP-42).
   *
   * @param challenge the challenge string received from the relay
   * @param relay the relay to which the client authenticates
   * @return this instance for chaining
   */
  public NIP42 createCanonicalAuthenticationEvent(@NonNull String challenge, @NonNull Relay relay) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.EVENT_DELETION, "").create();
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
   * Create a relay tag referencing the relay being authenticated.
   *
   * @param relay the relay
   * @return the created relay tag
   */
  public static BaseTag createRelayTag(@NonNull Relay relay) {
    return new BaseTagFactory(Constants.Tag.RELAY_CODE, relay.getUri()).create();
  }

  /**
   * Create a challenge tag holding the relay-provided token.
   *
   * @param challenge the relay-provided challenge string
   * @return the created challenge tag
   */
  public static BaseTag createChallengeTag(@NonNull String challenge) {
    return new BaseTagFactory(Constants.Tag.CHALLENGE_CODE, challenge).create();
  }

  /**
   * Create a client authentication message for the provided authentication event.
   *
   * @param event the canonical authentication event (signed)
   * @return the AUTH message to send to the relay
   */
  public static CanonicalAuthenticationMessage createClientAuthenticationMessage(
      @NonNull CanonicalAuthenticationEvent event) {
    return new CanonicalAuthenticationMessage(event);
  }

  /**
   * Create a relay AUTH message requesting client authentication.
   *
   * @param challenge the relay-provided challenge string
   * @return the AUTH message
   */
  public static GenericMessage createRelayAuthenticationMessage(@NonNull String challenge) {
    final List<ElementAttribute> attributes = new ArrayList<>();
    final var attr = new ElementAttribute("challenge", challenge);
    attributes.add(attr);
    return new GenericMessage(Command.AUTH.name(), attributes);
  }
}
