package nostr.connection.impl.listeners;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;

import okhttp3.WebSocketListener;
import okhttp3.WebSocket;
import okhttp3.Response;
import java.util.logging.Level;

@Getter
@Log
@AllArgsConstructor
public class ErrorListener extends WebSocketListener {

    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    @Override
    public void onFailure(WebSocket webSocket, Throwable error, Response response) {
        log.log(Level.WARNING, "WebSocket error: {0} - Relay {1}", new Object[]{error, relay});
        error.printStackTrace();
    }
}
