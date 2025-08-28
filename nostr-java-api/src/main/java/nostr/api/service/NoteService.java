package nostr.api.service;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import nostr.api.WebSocketClientHandler;
import nostr.base.IEvent;

public interface NoteService {
  List<String> send(@NonNull IEvent event, @NonNull Map<String, WebSocketClientHandler> clients);
}
