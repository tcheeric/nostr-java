package nostr.api.service;

import lombok.NonNull;
import nostr.api.WebSocketClientHandler;
import nostr.base.IEvent;

import java.util.List;
import java.util.Map;

public interface NoteService {
    List<String> send(@NonNull IEvent event, @NonNull Map<String, WebSocketClientHandler> clients);
}
