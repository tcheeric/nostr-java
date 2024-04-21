package nostr.connection.impl.listeners;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;

import java.net.http.WebSocket;
import java.util.logging.Level;

@Log
@AllArgsConstructor
public class ErrorListener implements WebSocket.Listener {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.log(Level.WARNING, "WebSocket error: {0} - Relay {1}", new Object[]{error, relay});
        error.printStackTrace();
    }
}
