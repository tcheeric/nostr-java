package nostr.client.relay;

import nostr.event.impl.GenericEvent;

/**
 * Receives what a multi-relay subscription produces.
 *
 * <p>Fanning one subscription across many relays turns a stream of raw frames into three
 * distinct things a caller reacts to differently: matching events, the moment the stored backlog
 * is drained, and trouble at an individual relay. Separating them here means a caller never has
 * to inspect a payload to work out which it just received.
 */
public interface SubscriptionListener {

  /**
   * A matching event, delivered once however many relays sent it.
   *
   * @param event the event, already parsed
   */
  void onEvent(GenericEvent event);

  /**
   * Every relay has finished replaying stored events, or stopped being waited for.
   *
   * <p>Fires exactly once per subscription. Events arriving after this are live rather than
   * historical, which is what lets an application stop showing a loading state.
   */
  default void onEndOfStoredEvents() {
    // Callers interested only in events need not distinguish stored from live.
  }

  /**
   * One relay failed, while the subscription continues on the others.
   *
   * <p>This is the signal that a firehose is quietly degrading: without it, a subscription can
   * fall from five relays to one over a day and look healthy throughout.
   *
   * @param relayUri the relay that failed
   * @param failure what went wrong
   */
  default void onRelayFailure(String relayUri, Throwable failure) {
    // A caller that does not track relay health can ignore individual failures.
  }
}
