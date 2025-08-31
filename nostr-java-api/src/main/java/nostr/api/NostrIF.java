package nostr.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * Core client interface for sending Nostr events and REQ messages to relays, signing and verifying
 * events, and managing sender/relay configuration.
 */
public interface NostrIF {
  /**
   * Set the sender identity used to sign events.
   *
   * @param sender the identity
   * @return this instance for chaining
   */
  NostrIF setSender(@NonNull Identity sender);

  /**
   * Configure relays for sending and requesting events.
   *
   * @param relays a map from relay name to relay URI
   * @return this instance for chaining
   */
  NostrIF setRelays(@NonNull Map<String, String> relays);

  /**
   * Send a single event to the configured relays.
   *
   * @param event the event to send
   * @return a list of relay responses (raw JSON messages)
   */
  List<String> sendEvent(@NonNull IEvent event);

  /**
   * Send a single event to the provided relays.
   *
   * @param event the event to send
   * @param relays relay map (name -> URI) to use for this send
   * @return a list of relay responses (raw JSON messages)
   */
  List<String> sendEvent(@NonNull IEvent event, Map<String, String> relays);

  /**
   * Send a REQ request with a single filter to the configured relays.
   *
   * @param filters the filter
   * @param subscriptionId the subscription identifier
   * @return a list of relay responses (raw JSON messages)
   */
  List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId);

  /**
   * Send a REQ request with a single filter to provided relays.
   *
   * @param filters the filter
   * @param subscriptionId the subscription identifier
   * @param relays relay map (name -> URI)
   * @return a list of relay responses (raw JSON messages)
   */
  List<String> sendRequest(
      @NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays);

  /**
   * Send a REQ request with multiple filters to the configured relays.
   *
   * @param filtersList filters to apply
   * @param subscriptionId the subscription identifier
   * @return a list of relay responses (raw JSON messages)
   */
  List<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId);

  /**
   * Send a REQ request with multiple filters to provided relays.
   *
   * @param filtersList filters to apply
   * @param subscriptionId the subscription identifier
   * @param relays relay map (name -> URI)
   * @return a list of relay responses (raw JSON messages)
   */
  List<String> sendRequest(
      @NonNull List<Filters> filtersList,
      @NonNull String subscriptionId,
      Map<String, String> relays);

  /**
   * Sign a signable object with the provided identity.
   *
   * @param identity the identity providing the private key
   * @param signable the object to sign
   * @return this instance for chaining
   */
  NostrIF sign(@NonNull Identity identity, @NonNull ISignable signable);

  /**
   * Verify the Schnorr signature of a GenericEvent.
   *
   * @param event the event to verify
   * @return true if signature is valid
   */
  boolean verify(@NonNull GenericEvent event);

  /**
   * Get the configured sender identity.
   *
   * @return the sender identity
   */
  Identity getSender();

  /**
   * Get the configured relays map.
   *
   * @return relay map (name -> URI)
   */
  Map<String, String> getRelays();

  /**
   * Close all underlying WebSocket clients.
   *
   * @throws IOException if an I/O error occurs
   */
  void close() throws IOException;
}
