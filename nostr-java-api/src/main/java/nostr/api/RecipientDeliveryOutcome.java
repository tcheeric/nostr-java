package nostr.api;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Whether one participant's copy of a direct message reached them.
 *
 * <p>A group message succeeds for some recipients and fails for others, so delivery has no
 * single verdict. Reporting per recipient lets an application tell a user precisely who did not
 * receive their message, rather than only that something went wrong.
 *
 * @param recipient the participant this outcome concerns
 * @param status whether their copy was delivered
 * @param relays the relays their copy was accepted by
 * @param reason why delivery failed, when it did
 */
public record RecipientDeliveryOutcome(
    String recipient, Status status, List<String> relays, String reason) {

  /** How a participant's copy fared. */
  public enum Status {
    /** At least one of the recipient's relays stored their copy. */
    DELIVERED,
    /** The recipient published no relay list, so NIP-17 forbids sending to them. */
    UNREACHABLE,
    /** The recipient's relays were asked but none stored their copy. */
    REJECTED
  }

  public RecipientDeliveryOutcome {
    relays = relays == null ? List.of() : List.copyOf(relays);
    reason = reason == null ? "" : reason;
  }

  /**
   * Record a copy that reached its recipient.
   *
   * @param recipient the participant reached
   * @param relays the relays that stored their copy
   * @return the delivered outcome
   */
  public static RecipientDeliveryOutcome delivered(
      @NonNull String recipient, @NonNull List<String> relays) {
    return new RecipientDeliveryOutcome(recipient, Status.DELIVERED, relays, "");
  }

  /**
   * Record a participant who publishes no relay list.
   *
   * <p>NIP-17 treats this as declining private messages, so nothing is sent for them at all.
   *
   * @param recipient the unreachable participant
   * @return the unreachable outcome
   */
  public static RecipientDeliveryOutcome unreachable(@NonNull String recipient) {
    return new RecipientDeliveryOutcome(
        recipient, Status.UNREACHABLE, List.of(), "Publishes no direct message relay list");
  }

  /**
   * Record a copy that every one of the recipient's relays refused.
   *
   * @param recipient the participant not reached
   * @param reason what the relays reported
   * @return the rejected outcome
   */
  public static RecipientDeliveryOutcome rejected(@NonNull String recipient, String reason) {
    return new RecipientDeliveryOutcome(recipient, Status.REJECTED, List.of(), reason);
  }

  /**
   * Whether this participant received the message.
   *
   * @return {@code true} when at least one of their relays stored it
   */
  public boolean isDelivered() {
    return status == Status.DELIVERED;
  }

  /**
   * Why delivery failed, when it did.
   *
   * @return the reason, empty for a delivered copy
   */
  public Optional<String> findReason() {
    return reason.isBlank() ? Optional.empty() : Optional.of(reason);
  }
}
