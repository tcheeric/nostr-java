package nostr.connection.impl.listeners;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.event.Response;

import java.net.http.WebSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;

@AllArgsConstructor
@Log
public class CloseListener implements WebSocket.Listener {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    private final Set<Response> responses = Collections.synchronizedSet(new HashSet<>());

    @Override
    public CompletionStage<Void> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.log(Level.INFO, "WebSocket connection to {0} closed: {1}, {2}", new Object[]{relay, statusCode, reason});
        responses.clear();
        return CompletableFuture.completedFuture(null);
    }
}
