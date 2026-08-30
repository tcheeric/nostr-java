package nostr.encryption;

import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.Rumor;

/**
 * Hides an event's author, content, and metadata behind the NIP-59 gift wrap.
 *
 * <p>A gift wrap conceals a message in three layers. The innermost is an unsigned {@link Rumor}
 * carrying the content. It is encrypted into a kind-13 seal signed by its real author, which
 * proves authorship without revealing the recipient. The seal is encrypted again into a
 * kind-1059 gift wrap signed by a single-use key, which reveals nothing about the author. An
 * observer sees only that some random key addressed some event to a recipient.
 *
 * <p>Callers never handle a seal, an ephemeral key, or a conversation key. Two methods hide a
 * rumor and reveal it; everything between is an implementation concern.
 *
 * <p>NIP-59 defines the envelope, not what travels inside it. Any event kind may be wrapped, so
 * this interface is useful beyond the private direct messages of NIP-17.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 */
public interface GiftWrapper {

  /**
   * Seals a rumor and wraps it for one recipient.
   *
   * <p>The returned event is signed by a freshly generated key that is used once and discarded,
   * so two wraps of the same rumor cannot be linked to each other or to their author. Both the
   * seal and the wrap carry timestamps randomised into the past.
   *
   * <p>To reach several recipients, call this once per recipient. Each call produces an
   * independently encrypted event, which is what keeps the recipient list private.
   *
   * @param rumor the unsigned event to conceal
   * @param recipient the public key that will be able to open the wrap
   * @return a signed gift wrap ready to publish
   * @throws GiftWrapException if the rumor cannot be sealed or wrapped
   */
  GenericEvent wrap(Rumor rumor, PublicKey recipient);

  /**
   * Opens a gift wrap addressed to this identity and returns the rumor inside.
   *
   * <p>The seal's signature is verified and its author is checked against the rumor's author
   * before the rumor is returned, so a rumor obtained here has been authenticated. A rumor is
   * unsigned, which means the seal's signature is the only evidence of who wrote it.
   *
   * @param giftWrap a kind-1059 or kind-21059 event addressed to this identity
   * @return the authenticated rumor
   * @throws GiftWrapException if the wrap cannot be opened, or if it fails authentication
   */
  Rumor unwrap(GenericEvent giftWrap);
}
