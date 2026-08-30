package nostr.mcp.query;

import java.time.Duration;

/**
 * The bounds every read stays inside.
 *
 * <p>A relay serves one request at a time, so a query with no ceiling stalls every other tool
 * call behind it, and an agent's context is finite, so an unbounded result is unusable even when
 * it arrives. Both limits are configuration rather than constants because the right values
 * depend on the relays a deployment talks to.
 *
 * @param maxEventsPerQuery the most events any one query may return
 * @param queryTimeout how long to wait for relays to finish replaying
 */
public record QueryLimits(int maxEventsPerQuery, Duration queryTimeout) {

  private static final int DEFAULT_MAX_EVENTS = 500;
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

  /**
   * The defaults from the specification.
   *
   * @return limits suitable for an ordinary desktop deployment
   */
  public static QueryLimits defaults() {
    return new QueryLimits(DEFAULT_MAX_EVENTS, DEFAULT_TIMEOUT);
  }
}
