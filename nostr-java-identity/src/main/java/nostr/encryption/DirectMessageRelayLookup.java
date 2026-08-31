package nostr.encryption;

import nostr.base.PublicKey;
import nostr.event.impl.DirectMessageRelayList;

import java.util.Optional;

/**
 * Finds where someone receives private direct messages.
 *
 * <p>Resolving a kind-10050 list means querying relays, which the SDK's messaging types
 * deliberately do not do. This interface is the seam: a caller supplies the lookup, backed by a
 * relay query, a local cache, or fixed configuration, and message composition stays a pure
 * function that can be verified without a network.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
@FunctionalInterface
public interface DirectMessageRelayLookup {

  /**
   * Returns the relay list published by the given key, if there is one.
   *
   * <p>An empty result means the key has published no list, which NIP-17 treats as declining
   * private messages rather than as a lookup failure.
   *
   * @param owner the key whose relay list is wanted
   * @return their relay list, or empty when they have published none
   */
  Optional<DirectMessageRelayList> findFor(PublicKey owner);
}
