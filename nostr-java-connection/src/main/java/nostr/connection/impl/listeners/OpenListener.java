package nostr.connection.impl.listeners;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import java.util.logging.Level;

@Getter
@AllArgsConstructor
@Log
public class OpenListener extends WebSocketListener {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.log(Level.INFO, "WebSocket opened to {0}", relay);
    }
}
