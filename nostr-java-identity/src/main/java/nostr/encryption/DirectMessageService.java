package nostr.encryption;

import nostr.base.PublicKey;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.GenericEvent;

import java.util.List;
import java.util.Map;

/**
 * Composes and reads NIP-17 private direct messages.
 *
 * <p>A direct message is published as one gift wrap per participant, each encrypted separately.
 * There is no shared envelope and no group identifier, which is what keeps a conversation's
 * membership private, and it is why composing a message yields several events rather than one.
 *
 * <p>This service is a pure function of its inputs. It does not publish, subscribe, or retain
 * messages, leaving the caller to route the events it produces and to decide what to keep. That
 * separation is what allows a message to be composed and verified without a relay.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
public interface DirectMessageService {

  /**
   * Seals and wraps a message for every participant, including its sender.
   *
   * <p>The sender's own copy is not a courtesy. A sender who published only their recipients'
   * copies would be unable to read the conversation back on another device, because they cannot
   * decrypt a wrap addressed to someone else.
   *
   * <p>Each returned event is addressed to exactly one participant. Publishing them all to one
   * relay is possible but wasteful; prefer {@link #composeByRecipient} and route each event to
   * the relays its recipient reads.
   *
   * @param message the message to send
   * @return one signed gift wrap per participant
   * @throws GiftWrapException if the message cannot be sealed or wrapped
   */
  List<GenericEvent> compose(ChatMessage message);

  /**
   * Seals and wraps a message, keeping each participant paired with their own event.
   *
   * <p>NIP-17 requires that a message reach a participant only through the relays that
   * participant nominated, so a caller publishing to the network needs to know which event
   * belongs to whom.
   *
   * @param message the message to send
   * @return each participant's public key mapped to the wrap addressed to them
   * @throws GiftWrapException if the message cannot be sealed or wrapped
   */
  Map<PublicKey, GenericEvent> composeByRecipient(ChatMessage message);

  /**
   * Opens a gift wrap addressed to this identity and returns the message inside.
   *
   * <p>The message is authenticated before it is returned: the seal's signature is verified and
   * its author is checked against the rumor's, so the sender reported here is the real one.
   *
   * @param giftWrap a kind-1059 event addressed to this identity
   * @return the authenticated message
   * @throws GiftWrapException if the wrap cannot be opened or fails authentication
   * @throws IllegalArgumentException if the wrap does not contain a chat message
   */
  ChatMessage read(GenericEvent giftWrap);
}
