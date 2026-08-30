package nostr.event.unit;

import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.DirectMessageRelayList;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the kind-10050 list naming where someone receives private direct messages.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
class DirectMessageRelayListTest {

  private static final PublicKey OWNER =
      new PublicKey("611df01bfcf85c26ae65453b772d8f1dfd25c264621c0277e1fc1518686faef9");

  private static final long CREATED_AT = 1691518405L;

  /** The relays published in the NIP-17 example. */
  private static final Relay INBOX = new Relay("wss://inbox.nostr.wine");

  private static final Relay MY_RELAY = new Relay("wss://myrelay.nostr1.com");

  private static DirectMessageRelayList aListOf(Relay... relays) {
    return new DirectMessageRelayList(OWNER, List.of(relays), CREATED_AT);
  }

  /** A relay list renders as the kind-10050 event NIP-17 specifies, with empty content. */
  @Test
  @DisplayName("renders as a kind-10050 event")
  void rendersAsDirectMessageRelayListEvent() {
    GenericEvent event = aListOf(INBOX, MY_RELAY).toEvent();

    assertEquals(Kinds.DM_RELAY_LIST, event.getKind());
    assertEquals(OWNER, event.getPubKey());
    assertEquals("", event.getContent());
    assertEquals(CREATED_AT, event.getCreatedAt());
  }

  /** Each relay is carried as a relay tag, in the order it was nominated. */
  @Test
  @DisplayName("carries each relay as a relay tag in order")
  void carriesRelaysAsTagsInOrder() {
    GenericEvent event = aListOf(INBOX, MY_RELAY).toEvent();

    assertEquals(2, event.getTags().size());
    assertEquals("relay", event.getTags().get(0).getCode());
    assertEquals(List.of(INBOX, MY_RELAY), DirectMessageRelayList.from(event).getRelays());
  }

  /** A relay list survives the round trip through its event. */
  @Test
  @DisplayName("round-trips through its event")
  void roundTripsThroughEvent() {
    DirectMessageRelayList original = aListOf(INBOX, MY_RELAY);

    assertEquals(original, DirectMessageRelayList.from(original.toEvent()));
  }

  /**
   * An owner who nominates no relay is declining private messages, which callers must be able
   * to distinguish from a relay being unreachable.
   */
  @Test
  @DisplayName("reports an empty list as empty")
  void reportsEmptyList() {
    assertTrue(aListOf().isEmpty());
    assertFalse(aListOf(INBOX).isEmpty());
  }

  /** An empty list round-trips, so "declines messages" is not confused with "no list found". */
  @Test
  @DisplayName("round-trips an empty list")
  void roundTripsEmptyList() {
    DirectMessageRelayList empty = aListOf();

    DirectMessageRelayList restored = DirectMessageRelayList.from(empty.toEvent());

    assertTrue(restored.isEmpty());
  }

  /** A relay nominated twice is kept once, since duplicates would double-publish a message. */
  @Test
  @DisplayName("keeps a duplicated relay only once")
  void keepsDuplicateRelayOnce() {
    DirectMessageRelayList list = aListOf(INBOX, MY_RELAY, INBOX);

    assertEquals(List.of(INBOX, MY_RELAY), list.getRelays());
  }

  /** An event of another kind is not a relay list and is refused. */
  @Test
  @DisplayName("refuses to read an event that is not a relay list")
  void refusesWrongKind() {
    GenericEvent note = new GenericEvent(OWNER, Kinds.TEXT_NOTE);
    note.setContent("not a relay list");
    note.update(CREATED_AT);

    assertThrows(IllegalArgumentException.class, () -> DirectMessageRelayList.from(note));
  }

  /** Tags that are not relay tags are ignored rather than misread as relays. */
  @Test
  @DisplayName("ignores tags that do not name a relay")
  void ignoresUnrelatedTags() {
    GenericEvent event = aListOf(INBOX).toEvent();
    List<BaseTag> mixed =
        List.of(
            BaseTag.create("relay", INBOX.getUri()), BaseTag.create("p", OWNER.toString()));
    event.setTags(mixed);
    event.update(CREATED_AT);

    assertEquals(List.of(INBOX), DirectMessageRelayList.from(event).getRelays());
  }

  /** The list a caller receives cannot be modified, keeping the instance immutable. */
  @Test
  @DisplayName("exposes relays as an unmodifiable list")
  void exposesUnmodifiableRelays() {
    DirectMessageRelayList list = aListOf(INBOX);

    assertThrows(UnsupportedOperationException.class, () -> list.getRelays().add(MY_RELAY));
  }
}
