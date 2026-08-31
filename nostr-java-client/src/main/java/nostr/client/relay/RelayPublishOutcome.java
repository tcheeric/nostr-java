package nostr.client.relay;

import lombok.NonNull;

import java.util.Optional;

/**
 * What one relay did with one published event.
 *
 * <p>Relays disagree, so a publish to several of them has no single answer: one accepts, another
 * rejects because the author is banned, a third never replies. This type records that per relay
 * so the caller can act on the difference, rather than collapsing it into a boolean.
 *
 * @param relayUri the relay this outcome came from
 * @param status whether the relay accepted, rejected, or failed to answer
 * @param reason the relay's verbatim explanation when it rejected, otherwise empty
 */
public record RelayPublishOutcome(String relayUri, Status status, String reason) {

  /** How a relay responded to a published event. */
  public enum Status {
    /** The relay stored the event. */
    ACCEPTED,
    /** The relay refused the event and said why. */
    REJECTED,
    /** The relay did not answer before the pool's timeout expired. */
    TIMED_OUT,
    /** The relay could not be reached at all. */
    UNREACHABLE
  }

  public RelayPublishOutcome {
    if (relayUri == null || relayUri.isBlank()) {
      throw new IllegalArgumentException("relayUri must not be blank");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    reason = reason == null ? "" : reason;
  }

  /**
   * Record that a relay stored the event.
   *
   * @param relayUri the relay that accepted
   * @return the accepted outcome
   */
  public static RelayPublishOutcome accepted(@NonNull String relayUri) {
    return new RelayPublishOutcome(relayUri, Status.ACCEPTED, "");
  }

  /**
   * Record that a relay refused the event.
   *
   * @param relayUri the relay that rejected
   * @param reason the relay's verbatim reason, such as {@code "blocked: pubkey banned"}
   * @return the rejected outcome
   */
  public static RelayPublishOutcome rejected(@NonNull String relayUri, String reason) {
    return new RelayPublishOutcome(relayUri, Status.REJECTED, reason);
  }

  /**
   * Record that a relay did not answer in time.
   *
   * @param relayUri the relay that stayed silent
   * @param reason what the transport reported
   * @return the timed-out outcome
   */
  public static RelayPublishOutcome timedOut(@NonNull String relayUri, String reason) {
    return new RelayPublishOutcome(relayUri, Status.TIMED_OUT, reason);
  }

  /**
   * Record that a relay could not be reached.
   *
   * @param relayUri the relay that could not be reached
   * @param reason what the transport reported
   * @return the unreachable outcome
   */
  public static RelayPublishOutcome unreachable(@NonNull String relayUri, String reason) {
    return new RelayPublishOutcome(relayUri, Status.UNREACHABLE, reason);
  }

  /**
   * Whether this relay stored the event.
   *
   * @return {@code true} when the relay accepted
   */
  public boolean isAccepted() {
    return status == Status.ACCEPTED;
  }

  /**
   * The relay's explanation, when it gave one.
   *
   * @return the reason, empty for an accepted outcome
   */
  public Optional<String> findReason() {
    return reason.isBlank() ? Optional.empty() : Optional.of(reason);
  }
}
