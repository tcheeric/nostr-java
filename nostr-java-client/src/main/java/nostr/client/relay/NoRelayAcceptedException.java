package nostr.client.relay;

import lombok.Getter;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Thrown when an event was published to relays and not one of them stored it.
 *
 * <p>Partial failure is ordinary and is reported in a {@link PublishResult}. Total failure is
 * not: an event that reached no relay does not exist as far as the network is concerned, and a
 * caller that ignored a returned value would carry on believing it had published. This
 * mirrors the reasoning behind {@code RelayTimeoutException}, which replaced silently returning
 * an empty list when a relay never answered.
 *
 * <p>The full {@link PublishResult} is attached, so the caller can still see what each relay
 * said rather than only that everything failed.
 */
@Getter
public class NoRelayAcceptedException extends IOException {

  private final transient PublishResult publishResult;

  /**
   * @param publishResult the per-relay outcomes, none of which was an acceptance
   */
  public NoRelayAcceptedException(PublishResult publishResult) {
    super(describe(publishResult));
    this.publishResult = publishResult;
  }

  private static String describe(PublishResult publishResult) {
    String failures =
        publishResult.getFailures().stream()
            .map(
                outcome ->
                    outcome.relayUri()
                        + " ("
                        + outcome.status()
                        + outcome.findReason().map(reason -> ": " + reason).orElse("")
                        + ")")
            .collect(Collectors.joining(", "));
    return "No relay accepted event %s. Attempted: %s"
        .formatted(publishResult.getEventId(), failures);
  }
}
