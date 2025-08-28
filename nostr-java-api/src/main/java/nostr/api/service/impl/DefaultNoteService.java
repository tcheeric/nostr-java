package nostr.api.service.impl;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import nostr.api.WebSocketClientHandler;
import nostr.api.service.NoteService;
import nostr.base.IEvent;

/** Default implementation that dispatches notes through all WebSocket clients. */
public class DefaultNoteService implements NoteService {
  @Override
  public List<String> send(
      @NonNull IEvent event, @NonNull Map<String, WebSocketClientHandler> clients) {
    return clients.values().stream()
        .map(client -> client.sendEvent(event))
        .flatMap(List::stream)
        .distinct()
        .toList();
  }
}
