package nostr.encryption;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.DirectMessageRelayList;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.Objects;

/**
 * Where one participant's copy of a message should be delivered.
 *
 * <p>NIP-17 requires each copy to reach only the relays its recipient nominated, so a caller
 * publishing a message needs the events and their destinations paired up.
 *
 * <p>A participant who has published no relay list is not reachable. That is a deliberate
 * signal, not a transient failure, and it is reported separately so a caller can tell "this
 * person does not accept private messages" from "the relay was down".
 *
 * @param recipient the participant this copy is addressed to
 * @param giftWrap the event to publish, or {@code null} when the recipient is unreachable
 * @param relays the relays to publish to, empty when the recipient is unreachable
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
public record MessageDelivery(PublicKey recipient, GenericEvent giftWrap, List<Relay> relays) {

  public MessageDelivery {
    Objects.requireNonNull(recipient, "recipient is required");
    relays = relays == null ? List.of() : List.copyOf(relays);
  }

  /**
   * Records a copy that can be delivered.
   *
   * @param recipient the participant this copy is addressed to
   * @param giftWrap the event to publish
   * @param relayList the recipient's nominated relays
   * @return a deliverable copy
   */
  public static MessageDelivery to(
      @NonNull PublicKey recipient,
      @NonNull GenericEvent giftWrap,
      @NonNull DirectMessageRelayList relayList) {
    return new MessageDelivery(recipient, giftWrap, relayList.getRelays());
  }

  /**
   * Records a participant who cannot receive private messages.
   *
   * <p>NIP-17 says not to attempt delivery when a recipient has published no relay list, so no
   * event is produced for them at all: an unsent wrap cannot leak.
   *
   * @param recipient the unreachable participant
   * @return an undeliverable entry carrying no event
   */
  public static MessageDelivery unreachable(@NonNull PublicKey recipient) {
    return new MessageDelivery(recipient, null, List.of());
  }

  /**
   * Reports whether this copy can be delivered.
   *
   * @return true when there is an event and somewhere to publish it
   */
  public boolean isDeliverable() {
    return giftWrap != null && !relays.isEmpty();
  }
}
