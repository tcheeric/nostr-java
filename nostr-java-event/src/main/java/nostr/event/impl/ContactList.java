package nostr.event.impl;

import lombok.NonNull;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The profiles someone follows, as defined by NIP-02.
 *
 * <p>A follow list is replaceable and total: every published list overwrites the last, so it
 * must carry every entry rather than a delta. Publishing a partial list is how an application
 * accidentally unfollows everyone, which is why this type is built from a complete set of
 * contacts rather than offering an "add" operation over an event.
 *
 * <p>Entries keep their order. NIP-02 asks clients to append new follows to the end so a list
 * reads chronologically, and reordering on a round-trip would quietly destroy that.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/02.md">NIP-02</a>
 */
public final class ContactList {

  private static final String CONTACT_TAG = "p";
  private static final int PUBLIC_KEY_PARAM = 0;
  private static final int RELAY_PARAM = 1;
  private static final int PETNAME_PARAM = 2;

  private final PublicKey owner;
  private final List<Contact> contacts;
  private final Long createdAt;

  /**
   * Records the profiles the given key follows.
   *
   * @param owner the key this list belongs to
   * @param contacts everyone followed, in order; a key appearing twice keeps its first entry
   * @param createdAt Unix timestamp in seconds
   */
  public ContactList(
      @NonNull PublicKey owner, @NonNull List<Contact> contacts, @NonNull Long createdAt) {
    this.owner = owner;
    this.contacts = List.copyOf(firstEntryPerKey(contacts));
    this.createdAt = createdAt;
  }

  /**
   * Reads a follow list from a kind-3 event.
   *
   * @param event the event to read
   * @return the profiles it follows
   * @throws IllegalArgumentException if the event is not a follow list
   */
  public static ContactList from(@NonNull GenericEvent event) {
    if (!Integer.valueOf(Kinds.CONTACT_LIST).equals(event.getKind())) {
      throw new IllegalArgumentException(
          "Expected a kind-"
              + Kinds.CONTACT_LIST
              + " follow list but found kind "
              + event.getKind());
    }

    List<Contact> contacts = new ArrayList<>();
    for (BaseTag tag : event.getTags()) {
      readContact(tag).ifPresent(contacts::add);
    }

    return new ContactList(event.getPubKey(), contacts, event.getCreatedAt());
  }

  /**
   * Renders this list as the kind-3 event to publish.
   *
   * <p>The returned event is unsigned; sign it with the owner's identity before publishing.
   * A contact with no relay hint still emits an empty parameter, because NIP-02 positions the
   * petname third and omitting the relay would make a petname read as one.
   *
   * @return the event carrying this list
   */
  public GenericEvent toEvent() {
    List<BaseTag> tags = new ArrayList<>();
    contacts.forEach(contact -> tags.add(toTag(contact)));

    GenericEvent event = new GenericEvent(owner, Kinds.CONTACT_LIST);
    event.setTags(tags);
    event.setContent("");
    event.update(createdAt);
    return event;
  }

  /**
   * The key this list belongs to.
   *
   * @return the owner
   */
  public PublicKey getOwner() {
    return owner;
  }

  /**
   * Everyone this list follows, in order.
   *
   * @return the contacts
   */
  public List<Contact> getContacts() {
    return Collections.unmodifiableList(contacts);
  }

  /**
   * The keys this list follows, for callers that need only the identities.
   *
   * @return the followed public keys, in order
   */
  public List<PublicKey> getFollowedKeys() {
    return contacts.stream().map(Contact::getPublicKey).toList();
  }

  /**
   * When this list was published.
   *
   * @return the Unix timestamp in seconds
   */
  public Long getCreatedAt() {
    return createdAt;
  }

  /**
   * Whether a given key is followed.
   *
   * @param publicKey the key to look for
   * @return true when the list contains that key
   */
  public boolean follows(@NonNull PublicKey publicKey) {
    return contacts.stream().anyMatch(contact -> publicKey.equals(contact.getPublicKey()));
  }

  /**
   * Reports whether this list follows anyone.
   *
   * <p>An empty list is a legitimate state, published by someone who follows nobody, and is
   * different from having no list at all.
   *
   * @return true when nobody is followed
   */
  public boolean isEmpty() {
    return contacts.isEmpty();
  }

  /**
   * Reads one contact from a tag, ignoring anything that is not a usable follow entry.
   *
   * <p>Follow lists in the wild carry tags this type does not model and entries with no key at
   * all. Discarding them keeps one malformed entry from making a whole list unreadable.
   */
  private static Optional<Contact> readContact(BaseTag tag) {
    if (!(tag instanceof GenericTag generic) || !CONTACT_TAG.equals(generic.getCode())) {
      return Optional.empty();
    }
    List<String> params = generic.getParams();
    if (params.isEmpty() || params.get(PUBLIC_KEY_PARAM).isBlank()) {
      return Optional.empty();
    }
    return Optional.of(
        new Contact(
            new PublicKey(params.get(PUBLIC_KEY_PARAM)),
            relayFrom(params),
            valueAt(params, PETNAME_PARAM)));
  }

  private static Relay relayFrom(List<String> params) {
    String uri = valueAt(params, RELAY_PARAM);
    return uri == null ? null : new Relay(uri);
  }

  private static String valueAt(List<String> params, int index) {
    if (params.size() <= index || params.get(index).isBlank()) {
      return null;
    }
    return params.get(index);
  }

  private static BaseTag toTag(Contact contact) {
    return BaseTag.create(
        CONTACT_TAG,
        contact.getPublicKey().toString(),
        contact.findRelay().map(Relay::getUri).orElse(""),
        contact.findPetname().orElse(""));
  }

  /** Keeps the first entry for each key, since a duplicate key has no meaning in a follow list. */
  private static List<Contact> firstEntryPerKey(List<Contact> contacts) {
    Map<PublicKey, Contact> byKey = new LinkedHashMap<>();
    contacts.forEach(contact -> byKey.putIfAbsent(contact.getPublicKey(), contact));
    return List.copyOf(byKey.values());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ContactList list)) {
      return false;
    }
    return Objects.equals(owner, list.owner)
        && Objects.equals(contacts, list.contacts)
        && Objects.equals(createdAt, list.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, contacts, createdAt);
  }

  @Override
  public String toString() {
    return "ContactList(owner=" + owner + ", contacts=" + contacts.size() + ")";
  }
}
