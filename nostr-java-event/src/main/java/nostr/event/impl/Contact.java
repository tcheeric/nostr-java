package nostr.event.impl;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;

import java.util.Objects;
import java.util.Optional;

/**
 * Someone a follow list follows, together with where to find them and what to call them.
 *
 * <p>NIP-02 gives each entry three parts: the key, a relay where that key's events can be found,
 * and a local nickname. The last two are optional and frequently empty, but they are the reason
 * a follow list is more than a set of keys. The relay hint is how a client discovers where to
 * look for someone it has never seen, and the petname is how it shows a human-readable name
 * without a global registry.
 *
 * <p>Both are modelled as absent rather than empty, since a follow list routinely carries
 * {@code ["p", key, "", ""]} and a caller asking for a petname wants to know there is none, not
 * to receive a blank string to test.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/02.md">NIP-02</a>
 */
public final class Contact {

  private final PublicKey publicKey;
  private final Relay relay;
  private final String petname;

  /**
   * Records a followed key, optionally with where to find them and what to call them.
   *
   * @param publicKey the followed key
   * @param relay where that key's events can be found, or {@code null} if not known
   * @param petname the local name for that profile, or {@code null} if none
   */
  public Contact(@NonNull PublicKey publicKey, Relay relay, String petname) {
    this.publicKey = publicKey;
    this.relay = relay;
    this.petname = petname == null || petname.isBlank() ? null : petname;
  }

  /**
   * Records a followed key with no relay hint and no petname.
   *
   * @param publicKey the followed key
   */
  public Contact(@NonNull PublicKey publicKey) {
    this(publicKey, null, null);
  }

  /**
   * The followed key.
   *
   * @return the public key
   */
  public PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * Where this contact's events can be found.
   *
   * @return the relay hint, or empty when the list carried none
   */
  public Optional<Relay> findRelay() {
    return Optional.ofNullable(relay);
  }

  /**
   * The local name for this contact.
   *
   * @return the petname, or empty when the list carried none
   */
  public Optional<String> findPetname() {
    return Optional.ofNullable(petname);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Contact contact)) {
      return false;
    }
    return Objects.equals(publicKey, contact.publicKey)
        && Objects.equals(relayUri(), contact.relayUri())
        && Objects.equals(petname, contact.petname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicKey, relayUri(), petname);
  }

  @Override
  public String toString() {
    return "Contact(" + publicKey + findPetname().map(name -> ", " + name).orElse("") + ")";
  }

  /** {@link Relay} does not define equality by URI, so compare on the URI itself. */
  private String relayUri() {
    return relay == null ? null : relay.getUri();
  }
}
