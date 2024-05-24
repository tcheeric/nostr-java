package nostr.connection.impl.listeners;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.event.Response;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;

@AllArgsConstructor
@Log
public class CloseListener extends WebSocketListener {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    private final Set<Response> responses = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void onClosed(WebSocket webSocket, int statusCode, String reason) {
        log.log(Level.INFO, "WebSocket connection to {0} closed: {1}, {2}", new Object[]{relay, statusCode, reason});
        responses.clear();
    }

    @Override
    public void onClosing(WebSocket webSocket, int statusCode, String reason) {
        log.log(Level.INFO, "WebSocket connection to {0} closing: {1}, {2}", new Object[]{relay, statusCode, reason});
        responses.clear();
    }
}
