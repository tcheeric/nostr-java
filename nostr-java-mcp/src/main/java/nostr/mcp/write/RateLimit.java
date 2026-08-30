package nostr.mcp.write;

import lombok.NonNull;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Caps how often a subject may write.
 *
 * <p>An agent in a loop is the ordinary failure, not the exceptional one: a model that
 * misreads its own output can publish the same note repeatedly, and on a public medium each
 * repetition is permanent. The limit is per subject, so one identity exhausting its budget
 * cannot silence another.
 *
 * <p>A sliding window rather than a fixed one, because a fixed window lets an agent spend a
 * whole budget at the end of one period and another at the start of the next, producing a burst
 * of twice the intended size at exactly the moment a runaway loop is most likely.
 */
public final class RateLimit {

  private final Map<String, Deque<Long>> timestampsBySubject = new HashMap<>();
  private final int maxWrites;
  private final Duration window;
  private final Clock clock;

  /**
   * @param maxWrites the most writes allowed in the window
   * @param window how far back the limit looks
   * @param clock the source of time, so tests need not sleep
   */
  public RateLimit(int maxWrites, @NonNull Duration window, @NonNull Clock clock) {
    this.maxWrites = maxWrites;
    this.window = window;
    this.clock = clock;
  }

  /**
   * Record a write, if the subject has budget left.
   *
   * @param subject who or what is being limited, such as an identity alias or a relay
   * @return true when the write may proceed
   */
  public synchronized boolean tryAcquire(@NonNull String subject) {
    long now = clock.millis();
    Deque<Long> timestamps = timestampsBySubject.computeIfAbsent(subject, key -> new ArrayDeque<>());
    long windowStart = now - window.toMillis();
    while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
      timestamps.pollFirst();
    }
    if (timestamps.size() >= maxWrites) {
      return false;
    }
    timestamps.addLast(now);
    return true;
  }

  /**
   * Describe the limit, for an error an agent can act on.
   *
   * @return the limit in words, such as "10 writes per 1m"
   */
  public String describe() {
    return maxWrites + " writes per " + window.toString().substring(2).toLowerCase(java.util.Locale.ROOT);
  }
}
