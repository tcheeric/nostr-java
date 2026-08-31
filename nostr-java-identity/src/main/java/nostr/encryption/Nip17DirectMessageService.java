package nostr.encryption;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.Rumor;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends and reads private direct messages by gift wrapping them per NIP-17.
 *
 * <p>Every message is sealed once and wrapped separately for each participant, so the events
 * published for one message share no key, no ciphertext, and no timestamp. An observer holding
 * all of them learns only that several unrelated-looking events were addressed to several
 * people.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
public class Nip17DirectMessageService implements DirectMessageService {

  private final Identity identity;
  private final GiftWrapper giftWrapper;

  /**
   * Creates a service that sends as, and reads for, the given identity.
   *
   * @param identity the identity that signs outgoing seals and opens incoming wraps
   */
  public Nip17DirectMessageService(@NonNull Identity identity) {
    this(identity, new Nip59GiftWrapper(identity));
  }

  /**
   * Creates a service with an explicit wrapper.
   *
   * <p>Useful for ephemeral conversations, which need a wrapper configured for kind 21059, and
   * for tests that fix the randomness a wrap would otherwise draw.
   *
   * @param identity the identity that signs outgoing seals and opens incoming wraps
   * @param giftWrapper the wrapper used to conceal and reveal messages
   */
  public Nip17DirectMessageService(@NonNull Identity identity, @NonNull GiftWrapper giftWrapper) {
    this.identity = identity;
    this.giftWrapper = giftWrapper;
  }

  /**
   * Starts a message authored by this service's identity.
   *
   * <p>Saves the caller naming a sender that must, in any case, match the signing identity.
   *
   * @return a builder with the sender already set
   */
  public ChatMessage.Builder message() {
    return ChatMessage.builder().from(identity.getPublicKey());
  }

  @Override
  public List<GenericEvent> compose(@NonNull ChatMessage message) {
    return List.copyOf(composeByRecipient(message).values());
  }

  @Override
  public Map<PublicKey, GenericEvent> composeByRecipient(@NonNull ChatMessage message) {
    Rumor rumor = senderVerifiedRumor(message);

    Map<PublicKey, GenericEvent> wrapsByRecipient = new LinkedHashMap<>();
    for (PublicKey participant : message.getParticipants()) {
      wrapsByRecipient.put(participant, giftWrapper.wrap(rumor, participant));
    }
    return wrapsByRecipient;
  }

  @Override
  public List<MessageDelivery> planDelivery(
      @NonNull ChatMessage message, @NonNull DirectMessageRelayLookup relayLists) {
    Rumor rumor = senderVerifiedRumor(message);

    List<MessageDelivery> plan = new ArrayList<>();
    for (PublicKey participant : message.getParticipants()) {
      plan.add(deliveryFor(rumor, participant, relayLists));
    }
    return List.copyOf(plan);
  }

  @Override
  public ChatMessage read(@NonNull GenericEvent giftWrap) {
    return ChatMessage.from(giftWrapper.unwrap(giftWrap));
  }

  /**
   * Wraps a message for one participant, or reports them unreachable.
   *
   * <p>Nothing is wrapped for a participant who nominated no relays. NIP-17 forbids sending to
   * them, and an event that is never created cannot later be published by mistake.
   */
  private MessageDelivery deliveryFor(
      Rumor rumor, PublicKey participant, DirectMessageRelayLookup relayLists) {
    return relayLists
        .findFor(participant)
        .filter(relayList -> !relayList.isEmpty())
        .map(
            relayList ->
                MessageDelivery.to(participant, giftWrapper.wrap(rumor, participant), relayList))
        .orElseGet(() -> MessageDelivery.unreachable(participant));
  }

  /**
   * Builds the rumor to seal, refusing to send a message attributed to somebody else.
   *
   * <p>Sealing a rumor that names another author produces an event the recipient will reject as
   * forged, so catching it here turns a confusing delivery failure into a clear programming
   * error.
   */
  private Rumor senderVerifiedRumor(ChatMessage message) {
    if (!identity.getPublicKey().equals(message.getSender())) {
      throw new GiftWrapException(
          "Cannot send a message authored by another identity; this would be rejected as forged");
    }
    return message.toRumor();
  }
}
