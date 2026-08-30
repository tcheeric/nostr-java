package nostr.client.relay;

import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What every relay did with one published event.
 *
 * <p>Publishing across relays succeeds partially far more often than it succeeds completely, so
 * the result is a record to inspect rather than a value to ignore. A caller that only wants to
 * know the event got out reads {@link #isAccepted()}; one that must report delivery reads the
 * per-relay outcomes.
 *
 * <p>Total failure is not represented here. A publish where no relay accepted throws
 * {@link NoRelayAcceptedException}, so an unpublished event cannot be mistaken for a published
 * one by a caller that skipped the result.
 */
public final class PublishResult {

  private final String eventId;
  private final Map<String, RelayPublishOutcome> outcomesByRelay;

  private PublishResult(String eventId, Map<String, RelayPublishOutcome> outcomesByRelay) {
    this.eventId = eventId;
    this.outcomesByRelay = outcomesByRelay;
  }

  /**
   * Collect per-relay outcomes into a result.
   *
   * @param eventId the published event's identifier
   * @param outcomes each relay's outcome
   * @return the assembled result
   */
  public static PublishResult of(
      @NonNull String eventId, @NonNull List<RelayPublishOutcome> outcomes) {
    Map<String, RelayPublishOutcome> byRelay = new LinkedHashMap<>();
    outcomes.forEach(outcome -> byRelay.put(outcome.relayUri(), outcome));
    return new PublishResult(eventId, Collections.unmodifiableMap(byRelay));
  }

  /**
   * The identifier of the event this result describes.
   *
   * @return the event id
   */
  public String getEventId() {
    return eventId;
  }

  /**
   * Every relay's outcome.
   *
   * @return the outcomes, one per relay the event was sent to
   */
  public List<RelayPublishOutcome> getOutcomes() {
    return List.copyOf(outcomesByRelay.values());
  }

  /**
   * One relay's outcome.
   *
   * @param relayUri the relay to look up
   * @return that relay's outcome, or empty if the event was never sent there
   */
  public Optional<RelayPublishOutcome> findOutcome(@NonNull String relayUri) {
    return Optional.ofNullable(outcomesByRelay.get(relayUri));
  }

  /**
   * The relays that stored the event.
   *
   * @return the accepting relays' URIs
   */
  public List<String> getAcceptingRelays() {
    return outcomesByRelay.values().stream()
        .filter(RelayPublishOutcome::isAccepted)
        .map(RelayPublishOutcome::relayUri)
        .toList();
  }

  /**
   * The relays that did not store the event, whether they rejected, timed out, or were down.
   *
   * @return the failing relays' outcomes
   */
  public List<RelayPublishOutcome> getFailures() {
    return outcomesByRelay.values().stream().filter(outcome -> !outcome.isAccepted()).toList();
  }

  /**
   * Whether at least one relay stored the event.
   *
   * @return {@code true} when the event reached at least one relay
   */
  public boolean isAccepted() {
    return outcomesByRelay.values().stream().anyMatch(RelayPublishOutcome::isAccepted);
  }

  /**
   * Whether every relay stored the event.
   *
   * @return {@code true} when no relay rejected, timed out, or was unreachable
   */
  public boolean isAcceptedByAllRelays() {
    return !outcomesByRelay.isEmpty()
        && outcomesByRelay.values().stream().allMatch(RelayPublishOutcome::isAccepted);
  }

  @Override
  public String toString() {
    return "PublishResult[event=%s accepted=%d of %d]"
        .formatted(eventId, getAcceptingRelays().size(), outcomesByRelay.size());
  }
}
