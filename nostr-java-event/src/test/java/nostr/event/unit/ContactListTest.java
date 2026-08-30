package nostr.event.unit;

import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.Contact;
import nostr.event.impl.ContactList;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies a NIP-02 follow list survives a round trip through its event form. */
class ContactListTest {

  private static final PublicKey OWNER = key("aa");
  private static final PublicKey ALICE = key("bb");
  private static final PublicKey BOB = key("cc");
  private static final Long CREATED_AT = 1_700_000_000L;

  // Verifies a list renders to a kind-3 event and reads back with every contact intact.
  @Test
  void aFollowListSurvivesARoundTrip() {
    ContactList original =
        new ContactList(
            OWNER,
            List.of(
                new Contact(ALICE, new Relay("wss://alicerelay.com"), "alice"),
                new Contact(BOB)),
            CREATED_AT);

    ContactList readBack = ContactList.from(original.toEvent());

    assertEquals(original, readBack);
    assertEquals(List.of(ALICE, BOB), readBack.getFollowedKeys());
  }

  // Verifies the relay hint and petname survive, since those are what make a follow list more
  // than a set of keys and are silently lost by any implementation that reads only the key.
  @Test
  void relayHintsAndPetnamesSurviveTheRoundTrip() {
    ContactList original =
        new ContactList(
            OWNER, List.of(new Contact(ALICE, new Relay("wss://alicerelay.com"), "alice")), CREATED_AT);

    Contact readBack = ContactList.from(original.toEvent()).getContacts().getFirst();

    assertEquals("wss://alicerelay.com", readBack.findRelay().orElseThrow().getUri());
    assertEquals("alice", readBack.findPetname().orElseThrow());
  }

  // Verifies a contact with neither hint nor petname reports them absent rather than blank,
  // since a follow list routinely carries ["p", key, "", ""].
  @Test
  void aContactWithoutAHintOrPetnameReportsThemAbsent() {
    ContactList original = new ContactList(OWNER, List.of(new Contact(ALICE)), CREATED_AT);

    Contact readBack = ContactList.from(original.toEvent()).getContacts().getFirst();

    assertTrue(readBack.findRelay().isEmpty());
    assertTrue(readBack.findPetname().isEmpty());
  }

  // Verifies entries keep their order, since NIP-02 asks clients to append new follows so the
  // list reads chronologically.
  @Test
  void contactsKeepTheirOrder() {
    List<Contact> inOrder =
        List.of(new Contact(BOB), new Contact(ALICE), new Contact(key("dd")));

    ContactList readBack =
        ContactList.from(new ContactList(OWNER, inOrder, CREATED_AT).toEvent());

    assertEquals(
        inOrder.stream().map(Contact::getPublicKey).toList(), readBack.getFollowedKeys());
  }

  // Verifies an event of the wrong kind is rejected with a message naming both kinds, so a
  // caller can see what it actually passed.
  @Test
  void anEventOfTheWrongKindIsRejected() {
    GenericEvent textNote =
        GenericEvent.builder().pubKey(OWNER).kind(Kinds.TEXT_NOTE).content("not a list").build();

    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> ContactList.from(textNote));

    assertTrue(thrown.getMessage().contains("kind-" + Kinds.CONTACT_LIST));
    assertTrue(thrown.getMessage().contains(String.valueOf(Kinds.TEXT_NOTE)));
  }

  // Verifies a list following nobody is read as empty rather than failing, since that is a
  // legitimate state and differs from having no list at all.
  @Test
  void aListFollowingNobodyIsEmptyRatherThanAnError() {
    ContactList readBack =
        ContactList.from(new ContactList(OWNER, List.of(), CREATED_AT).toEvent());

    assertTrue(readBack.isEmpty());
    assertEquals(List.of(), readBack.getFollowedKeys());
  }

  // Verifies tags that are not contacts are ignored, so a list carrying anything else remains
  // readable rather than being misread as a follow.
  @Test
  void tagsThatAreNotContactsAreIgnored() {
    GenericEvent event = new GenericEvent(OWNER, Kinds.CONTACT_LIST);
    event.setTags(
        List.of(
            BaseTag.create("p", ALICE.toString(), "", ""),
            BaseTag.create("t", "nostr"),
            BaseTag.create("e", "an-event-id")));
    event.setContent("");
    event.update(CREATED_AT);

    assertEquals(List.of(ALICE), ContactList.from(event).getFollowedKeys());
  }

  // Verifies a contact tag carrying no key is discarded, so one malformed entry cannot make a
  // whole follow list unreadable.
  @Test
  void aContactTagWithoutAKeyIsDiscarded() {
    GenericEvent event = new GenericEvent(OWNER, Kinds.CONTACT_LIST);
    event.setTags(List.of(BaseTag.create("p", ""), BaseTag.create("p", ALICE.toString())));
    event.setContent("");
    event.update(CREATED_AT);

    assertEquals(List.of(ALICE), ContactList.from(event).getFollowedKeys());
  }

  // Verifies the same key twice keeps only its first entry, since a duplicate follow has no
  // meaning and would otherwise be published back to relays.
  @Test
  void aDuplicatedKeyKeepsItsFirstEntry() {
    ContactList list =
        new ContactList(
            OWNER,
            List.of(new Contact(ALICE, null, "first"), new Contact(ALICE, null, "second")),
            CREATED_AT);

    assertEquals(1, list.getContacts().size());
    assertEquals("first", list.getContacts().getFirst().findPetname().orElseThrow());
  }

  // Verifies membership can be asked directly, which is the question callers actually have.
  @Test
  void membershipCanBeQueried() {
    ContactList list = new ContactList(OWNER, List.of(new Contact(ALICE)), CREATED_AT);

    assertTrue(list.follows(ALICE));
    assertFalse(list.follows(BOB));
  }

  // Verifies the rendered event is a kind-3 authored by the owner with no content, as NIP-02
  // specifies.
  @Test
  void theRenderedEventIsAnAuthoredKindThreeWithNoContent() {
    GenericEvent event =
        new ContactList(OWNER, List.of(new Contact(ALICE)), CREATED_AT).toEvent();

    assertEquals(Integer.valueOf(Kinds.CONTACT_LIST), event.getKind());
    assertEquals(OWNER, event.getPubKey());
    assertEquals("", event.getContent());
    assertEquals(CREATED_AT, event.getCreatedAt());
  }

  // Verifies a petname is positioned third even when there is no relay hint, since NIP-02 reads
  // tag parameters by position and a shifted petname would be read as a relay.
  @Test
  void aPetnameStaysInItsPositionWhenThereIsNoRelayHint() {
    GenericEvent event =
        new ContactList(OWNER, List.of(new Contact(ALICE, null, "alice")), CREATED_AT).toEvent();

    GenericTag tag = (GenericTag) event.getTags().getFirst();

    assertEquals(List.of(ALICE.toString(), "", "alice"), tag.getParams());
  }

  private static PublicKey key(String seed) {
    return new PublicKey(seed.repeat(32));
  }
}
