package nostr.client.relay;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remembers which event identifiers have already been delivered, forgetting the oldest first.
 *
 * <p>Every relay holding an event sends it, so a five-relay subscription would otherwise show
 * each note five times. Remembering every identifier forever would fix that and leak, on exactly
 * the long-lived firehose subscriptions that need de-duplication most, so the window is bounded
 * and the oldest identifiers are evicted.
 *
 * <p>Eviction means an event can be re-delivered if its copies arrive further apart than the
 * window is wide. The default is sized well beyond any realistic spread between relays.
 */
final class DeliveredEventWindow {

  /** Identifiers remembered by default: comfortably beyond cross-relay arrival spread. */
  static final int DEFAULT_CAPACITY = 4_096;

  private final Map<String, Boolean> deliveredEventIds;

  DeliveredEventWindow(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be positive, was " + capacity);
    }
    this.deliveredEventIds =
        new LinkedHashMap<>(capacity, 0.75f, true) {
          @Override
          protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > capacity;
          }
        };
  }

  /**
   * Record an event identifier, reporting whether it is the first time it has been seen.
   *
   * @param eventId the identifier to record
   * @return {@code true} when this identifier had not been delivered, so the caller should
   *     deliver it now
   */
  synchronized boolean isFirstSighting(String eventId) {
    return deliveredEventIds.put(eventId, Boolean.TRUE) == null;
  }

  /**
   * How many identifiers are currently remembered.
   *
   * @return the window's occupancy, never above its capacity
   */
  synchronized int size() {
    return deliveredEventIds.size();
  }
}
