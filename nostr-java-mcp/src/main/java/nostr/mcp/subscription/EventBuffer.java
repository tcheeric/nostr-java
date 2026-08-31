package nostr.mcp.subscription;

import lombok.NonNull;
import nostr.event.impl.GenericEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds the events that arrived since the agent last looked.
 *
 * <p>MCP has no way to push into a tool result, so events arriving between polls must wait
 * somewhere. This is that place, and it is bounded: an agent that opens a subscription and
 * forgets it must not be able to exhaust the server's memory, and a buffer large enough to be
 * unbounded would exceed a model's context anyway.
 *
 * <p>Overflow drops the oldest and counts what it dropped. The count matters more than the
 * events: an agent that silently receives a gap will summarise a partial feed as though it were
 * complete, whereas one told it missed thirty events can say so or narrow its filter.
 */
public final class EventBuffer {

  private final Deque<GenericEvent> events = new ArrayDeque<>();
  private final Set<String> seenEventIds = new LinkedHashSet<>();
  private final int capacity;
  private long droppedCount;

  /**
   * @param capacity the most events to hold before dropping the oldest
   */
  public EventBuffer(int capacity) {
    this.capacity = capacity;
  }

  /**
   * Take an event, dropping the oldest if full.
   *
   * <p>A repeat is ignored rather than stored twice. The SDK already delivers each event once
   * however many relays carry it, but it does so over a bounded window, so a relay replaying an
   * old event long afterwards can arrive as a duplicate. An agent seeing the same note twice
   * would reasonably conclude it was posted twice.
   *
   * @param event the event to hold
   */
  public synchronized void add(@NonNull GenericEvent event) {
    if (!seenEventIds.add(event.getId())) {
      return;
    }
    if (events.size() >= capacity) {
      GenericEvent dropped = events.pollFirst();
      if (dropped != null) {
        seenEventIds.remove(dropped.getId());
      }
      droppedCount++;
    }
    events.addLast(event);
  }

  /**
   * Take everything held, leaving the buffer empty.
   *
   * <p>Draining is what keeps repeated polls from refilling an agent's context with events it
   * has already read. This is a command that also answers, which is normally worth avoiding, but
   * an at-most-once read is the property that makes polling usable at all.
   *
   * @return the events in arrival order
   */
  public synchronized List<GenericEvent> drain() {
    List<GenericEvent> drained = List.copyOf(events);
    events.clear();
    seenEventIds.clear();
    return drained;
  }

  /**
   * How many events are waiting.
   *
   * @return the current depth
   */
  public synchronized int depth() {
    return events.size();
  }

  /**
   * How many events were dropped for want of room, over the buffer's whole life.
   *
   * <p>Monotonic on purpose: it survives draining, so an agent polling repeatedly can tell that
   * a gap happened at some point rather than only within the last window.
   *
   * @return the total dropped
   */
  public synchronized long droppedCount() {
    return droppedCount;
  }

  /**
   * The most events this buffer holds.
   *
   * @return the capacity
   */
  public int capacity() {
    return capacity;
  }
}
