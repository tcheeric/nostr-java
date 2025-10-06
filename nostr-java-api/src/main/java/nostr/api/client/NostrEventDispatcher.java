package nostr.api.client;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.NonNull;
import nostr.api.service.NoteService;
import nostr.base.IEvent;
import nostr.crypto.schnorr.Schnorr;
import nostr.crypto.schnorr.SchnorrException;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrUtil;

/**
 * Handles event verification and dispatching to relays.
 */
public final class NostrEventDispatcher {

  private final NoteService noteService;
  private final NostrRelayRegistry relayRegistry;

  public NostrEventDispatcher(NoteService noteService, NostrRelayRegistry relayRegistry) {
    this.noteService = noteService;
    this.relayRegistry = relayRegistry;
  }

  public List<String> send(@NonNull IEvent event) {
    if (event instanceof GenericEvent genericEvent) {
      if (!verify(genericEvent)) {
        throw new IllegalStateException("Event verification failed");
      }
    }
    return noteService.send(event, relayRegistry.getClientMap());
  }

  public boolean verify(@NonNull GenericEvent event) {
    if (!event.isSigned()) {
      throw new IllegalStateException("The event is not signed");
    }
    try {
      var message = NostrUtil.sha256(event.get_serializedEvent());
      return Schnorr.verify(message, event.getPubKey().getRawData(), event.getSignature().getRawData());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    } catch (SchnorrException e) {
      throw new IllegalStateException("Failed to verify Schnorr signature", e);
    }
  }
}
