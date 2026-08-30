package nostr.event.unit;

import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that computing an event id can preserve a deliberately chosen creation time.
 *
 * <p>NIP-59 requires seals and gift wraps to carry timestamps randomised into the past. That is
 * impossible if computing the id resets the timestamp to the current time, so {@code
 * update(long)} exists to keep the two concerns separate.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 */
class GenericEventUpdateTest {

  private static final PublicKey AUTHOR =
      new PublicKey("611df01bfcf85c26ae65453b772d8f1dfd25c264621c0277e1fc1518686faef9");

  private static GenericEvent anEvent() {
    GenericEvent event = new GenericEvent(AUTHOR, Kinds.SEAL);
    event.setContent("encrypted-payload");
    return event;
  }

  /** A supplied timestamp survives id computation, which is what gift wrapping depends on. */
  @Test
  @DisplayName("keeps the supplied created_at when computing the id")
  void keepsSuppliedCreatedAt() {
    long twoDaysAgo = Instant.now().minusSeconds(2 * 24 * 60 * 60).getEpochSecond();
    GenericEvent event = anEvent();

    event.update(twoDaysAgo);

    assertEquals(twoDaysAgo, event.getCreatedAt());
  }

  /** The id computed against a supplied timestamp is a real id, derived from that timestamp. */
  @Test
  @DisplayName("derives an id that reflects the supplied created_at")
  void derivesIdFromSuppliedCreatedAt() {
    GenericEvent earlier = anEvent();
    GenericEvent later = anEvent();

    earlier.update(1_700_000_000L);
    later.update(1_700_000_001L);

    assertNotNull(earlier.getId());
    assertEquals(64, earlier.getId().length());
    assertNotEquals(earlier.getId(), later.getId());
  }

  /** Updating twice with the same timestamp is deterministic, so ids are reproducible. */
  @Test
  @DisplayName("computes the same id for the same created_at")
  void computesSameIdForSameCreatedAt() {
    GenericEvent first = anEvent();
    GenericEvent second = anEvent();

    first.update(1_700_000_000L);
    second.update(1_700_000_000L);

    assertEquals(first.getId(), second.getId());
  }

  /**
   * The no-argument overload still stamps the event with the current time, so existing callers
   * are unaffected by the new seam.
   */
  @Test
  @DisplayName("still stamps the current time when no timestamp is supplied")
  void stampsCurrentTimeWithoutArgument() {
    long before = Instant.now().getEpochSecond();
    GenericEvent event = anEvent();

    event.update();

    long after = Instant.now().getEpochSecond();
    assertTrue(event.getCreatedAt() >= before && event.getCreatedAt() <= after);
  }

  /**
   * A previously chosen timestamp is discarded by the no-argument overload. This is the
   * behaviour that silently defeats gift-wrap privacy, so it is pinned here to document why
   * {@code update(long)} must be used for seals and wraps.
   */
  @Test
  @DisplayName("overwrites a preset created_at when no timestamp is supplied")
  void overwritesPresetCreatedAtWithoutArgument() {
    long twoDaysAgo = Instant.now().minusSeconds(2 * 24 * 60 * 60).getEpochSecond();
    GenericEvent event = anEvent();
    event.setCreatedAt(twoDaysAgo);

    event.update();

    assertNotEquals(twoDaysAgo, event.getCreatedAt());
  }

  /** Tags are included in the serialization that backs the id. */
  @Test
  @DisplayName("includes tags in the id computed against a supplied created_at")
  void includesTagsInId() {
    GenericEvent untagged = anEvent();
    GenericEvent tagged = anEvent();
    tagged.setTags(List.of(BaseTag.create("p", AUTHOR.toString())));

    untagged.update(1_700_000_000L);
    tagged.update(1_700_000_000L);

    assertNotEquals(untagged.getId(), tagged.getId());
  }

  /** The cached serialization is refreshed alongside the id, so signing sees the new bytes. */
  @Test
  @DisplayName("refreshes the cached serialization")
  void refreshesCachedSerialization() {
    GenericEvent event = anEvent();

    event.update(1_700_000_000L);

    assertNotNull(event.getSerializedEventCache());
    assertTrue(new String(event.getSerializedEventCache()).contains("1700000000"));
  }
}
