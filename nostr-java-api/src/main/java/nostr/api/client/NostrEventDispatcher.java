package nostr.api.client;

import lombok.NonNull;
import nostr.api.service.NoteService;
import nostr.base.IEvent;
import nostr.crypto.schnorr.Schnorr;
import nostr.crypto.schnorr.SchnorrException;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrUtil;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Handles event verification and dispatching to relays.
 *
 * <p>Performs BIP-340 Schnorr signature verification before forwarding events to all configured
 * relays.
 *
 * @see nostr.crypto.schnorr.Schnorr
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
 */
public final class NostrEventDispatcher {

  private final NoteService noteService;
  private final NostrRelayRegistry relayRegistry;

  /**
   * Create a dispatcher that uses the provided services to verify and distribute events.
   *
   * @param noteService service responsible for communicating with relays
   * @param relayRegistry registry that tracks the connected relay handlers
   */
  public NostrEventDispatcher(NoteService noteService, NostrRelayRegistry relayRegistry) {
    this.noteService = noteService;
    this.relayRegistry = relayRegistry;
  }

  /**
   * Verify the supplied event and forward it to all configured relays.
   *
   * @param event event to send
   * @return responses returned by relays
   * @throws IllegalStateException if verification fails
   */
  public List<String> send(@NonNull IEvent event) {
    if (event instanceof GenericEvent genericEvent) {
      if (!verify(genericEvent)) {
        throw new IllegalStateException("Event verification failed");
      }
    }
    return noteService.send(event, relayRegistry.getClientMap());
  }

  /**
   * Verify the Schnorr signature of the provided event.
   *
   * @param event event to verify
   * @return {@code true} if the signature is valid
   * @throws IllegalStateException if the event is unsigned or verification cannot complete
   */
  public boolean verify(@NonNull GenericEvent event) {
    if (!event.isSigned()) {
      throw new IllegalStateException("The event is not signed");
    }
    try {
      return Schnorr.verify(
          NostrUtil.sha256(event.getSerializedEventCache()),
          event.getPubKey().getRawData(),
          event.getSignature().getRawData());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    } catch (SchnorrException e) {
      throw new IllegalStateException("Failed to verify Schnorr signature", e);
    }
  }
}
