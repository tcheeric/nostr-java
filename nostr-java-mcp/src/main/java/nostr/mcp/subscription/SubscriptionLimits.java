package nostr.mcp.subscription;

import java.time.Duration;

/**
 * The bounds every subscription lives inside.
 *
 * <p>All three exist because an agent's session can end without this server being told, so a
 * subscription that is never closed, never read, or opened in a loop must not be able to consume
 * the process indefinitely.
 *
 * @param maxSubscriptions how many may be open at once
 * @param bufferCapacity how many events one holds between polls
 * @param idleTimeout how long an unread subscription survives
 */
public record SubscriptionLimits(int maxSubscriptions, int bufferCapacity, Duration idleTimeout) {

  private static final int DEFAULT_MAX_SUBSCRIPTIONS = 20;
  private static final int DEFAULT_BUFFER_CAPACITY = 500;
  private static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofHours(1);

  /**
   * The defaults from the specification.
   *
   * @return limits suitable for an ordinary desktop deployment
   */
  public static SubscriptionLimits defaults() {
    return new SubscriptionLimits(
        DEFAULT_MAX_SUBSCRIPTIONS, DEFAULT_BUFFER_CAPACITY, DEFAULT_IDLE_TIMEOUT);
  }
}
