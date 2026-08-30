package nostr.api;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.client.relay.NoRelayAcceptedException;
import nostr.client.relay.PublishResult;
import nostr.client.relay.RelayPool;
import nostr.encryption.DirectMessageRelayLookup;
import nostr.encryption.DirectMessageService;
import nostr.encryption.MessageDelivery;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.GenericEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends NIP-17 private direct messages and reads them back.
 *
 * <p>{@code nostr-java-identity} can plan a delivery but not perform one: it works out which
 * gift wrap belongs to which recipient and which relays each nominated, then stops, because
 * sending would give a policy module a transport. This service performs the plan, connecting to
 * each recipient's own relays rather than the sender's, since NIP-17 permits delivery only
 * there.
 *
 * <p>Relays borrowed for a delivery are released afterwards, so a long-running application does
 * not accumulate a connection for every person it has ever messaged.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
@Slf4j
public class DirectMessagePublisher {

  private final DirectMessageService directMessages;
  private final DirectMessageRelayLookup relayLists;
  private final RelayPool relayPool;

  /**
   * @param directMessages composes and reads NIP-17 messages
   * @param relayLists finds where each recipient receives messages
   * @param relayPool the connections used to deliver
   */
  public DirectMessagePublisher(
      @NonNull DirectMessageService directMessages,
      @NonNull DirectMessageRelayLookup relayLists,
      @NonNull RelayPool relayPool) {
    this.directMessages = directMessages;
    this.relayLists = relayLists;
    this.relayPool = relayPool;
  }

  /**
   * Send a message to every participant, reporting who received it.
   *
   * <p>Never throws for an undelivered recipient: a group message that reaches three of four
   * people has partly succeeded, and the caller needs to know which one missed out rather than
   * losing the whole result to an exception.
   *
   * @param message the message to send
   * @return one outcome per participant
   */
  public List<RecipientDeliveryOutcome> send(@NonNull ChatMessage message) {
    List<RecipientDeliveryOutcome> outcomes = new ArrayList<>();
    for (MessageDelivery delivery : directMessages.planDelivery(message, relayLists)) {
      outcomes.add(deliver(delivery));
    }
    return List.copyOf(outcomes);
  }

  /**
   * Read an incoming gift wrap back into the message it conceals.
   *
   * @param giftWrap the received wrap
   * @return the message inside
   */
  public ChatMessage read(@NonNull GenericEvent giftWrap) {
    return directMessages.read(giftWrap);
  }

  private RecipientDeliveryOutcome deliver(MessageDelivery delivery) {
    String recipient = delivery.recipient().toString();
    if (!delivery.isDeliverable()) {
      log.info("Not sending to {}: they publish no direct message relay list", recipient);
      return RecipientDeliveryOutcome.unreachable(recipient);
    }
    List<String> recipientRelays = relayUrisOf(delivery);
    recipientRelays.forEach(relayPool::addRelay);
    try {
      return publishTo(recipient, delivery.giftWrap(), recipientRelays);
    } finally {
      recipientRelays.forEach(relayPool::releaseRelay);
    }
  }

  /**
   * Publish one recipient's wrap and report only what their own relays did with it.
   *
   * <p>The pool may hold other relays, and an acceptance by one of those says nothing about
   * whether this recipient can read the message.
   */
  private RecipientDeliveryOutcome publishTo(
      String recipient, GenericEvent giftWrap, List<String> recipientRelays) {
    try {
      PublishResult result = relayPool.publish(giftWrap);
      List<String> accepted =
          result.getAcceptingRelays().stream().filter(recipientRelays::contains).toList();
      return accepted.isEmpty()
          ? RecipientDeliveryOutcome.rejected(recipient, describeFailures(result))
          : RecipientDeliveryOutcome.delivered(recipient, accepted);
    } catch (NoRelayAcceptedException e) {
      return RecipientDeliveryOutcome.rejected(recipient, e.getMessage());
    }
  }

  private String describeFailures(PublishResult result) {
    return result.getFailures().stream()
        .map(outcome -> outcome.relayUri() + ": " + outcome.status())
        .reduce((first, second) -> first + ", " + second)
        .orElse("No relay of theirs accepted the message");
  }

  private List<String> relayUrisOf(MessageDelivery delivery) {
    return delivery.relays().stream().map(Relay::getUri).distinct().toList();
  }
}
