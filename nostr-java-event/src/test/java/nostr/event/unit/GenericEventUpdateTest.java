package nostr.event.unit;

import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
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

  /** An event with no creation time is stamped with the current time. */
  @Test
  @DisplayName("stamps the current time when no created_at is set")
  void stampsCurrentTimeWhenUnset() {
    long before = Instant.now().getEpochSecond();
    GenericEvent event = anEvent();

    event.update();

    long after = Instant.now().getEpochSecond();
    assertTrue(event.getCreatedAt() >= before && event.getCreatedAt() <= after);
  }

  /** A created_at of zero counts as unset, so it is replaced with the current time. */
  @Test
  @DisplayName("treats a zero created_at as unset")
  void treatsZeroCreatedAtAsUnset() {
    long before = Instant.now().getEpochSecond();
    GenericEvent event = anEvent();
    event.setCreatedAt(0L);

    event.update();

    assertTrue(event.getCreatedAt() >= before);
  }

  /**
   * Regression for issue #559: a created_at the caller set survives {@code update()}. Losing it
   * produced ids for a different second than the one the caller published, and erased the
   * randomised timestamps NIP-59 seals and gift wraps depend on.
   */
  @Test
  @DisplayName("keeps a preset created_at when no timestamp is supplied")
  void keepsPresetCreatedAtWithoutArgument() {
    long twoDaysAgo = Instant.now().minusSeconds(2 * 24 * 60 * 60).getEpochSecond();
    GenericEvent event = anEvent();
    event.setCreatedAt(twoDaysAgo);

    event.update();

    assertEquals(twoDaysAgo, event.getCreatedAt());
  }

  /** The id computed by {@code update()} is the NIP-01 hash for the preset created_at. */
  @Test
  @DisplayName("computes the NIP-01 id for a preset created_at")
  void computesNip01IdForPresetCreatedAt() throws Exception {
    GenericEvent event = anEvent();
    event.setCreatedAt(1_700_000_000L);

    event.update();

    String canonical =
        "[0,\"" + AUTHOR + "\",1700000000," + Kinds.SEAL + ",[],\"encrypted-payload\"]";
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
    assertEquals(HexFormat.of().formatHex(digest), event.getId());
  }

  /** Restamping is still available, but only when asked for by name. */
  @Test
  @DisplayName("replaces a preset created_at when restamping explicitly")
  void replacesPresetCreatedAtWhenRestampingExplicitly() {
    long twoDaysAgo = Instant.now().minusSeconds(2 * 24 * 60 * 60).getEpochSecond();
    GenericEvent event = anEvent();
    event.setCreatedAt(twoDaysAgo);

    event.updateWithCurrentTime();

    assertTrue(event.getCreatedAt() >= Instant.now().getEpochSecond() - 1);
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
