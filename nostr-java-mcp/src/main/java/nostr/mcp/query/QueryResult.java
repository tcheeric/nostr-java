package nostr.mcp.query;

import nostr.event.impl.GenericEvent;

import java.util.List;

/**
 * What a one-shot query found, and what it might have missed.
 *
 * <p>The two flags are the point. An agent given a truncated or timed-out answer as though it
 * were complete will state conclusions the data does not support, so "there are no mentions" and
 * "I stopped looking" are kept distinguishable all the way to the model.
 *
 * @param events the matching events, newest first
 * @param truncated whether the event limit cut the answer short
 * @param timedOut whether the relays had not finished replaying when the deadline passed
 * @param relayCount how many relays answered
 */
public record QueryResult(
    List<GenericEvent> events, boolean truncated, boolean timedOut, int relayCount) {

  /**
   * Whether the answer is everything the relays hold.
   *
   * @return true when neither bound was reached
   */
  public boolean isComplete() {
    return !truncated && !timedOut;
  }
}
