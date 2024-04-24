package nostr.connection.impl.listeners;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;

import java.net.http.WebSocket;
import java.util.logging.Level;

@Getter
@AllArgsConstructor
@Log
public class OpenListener implements WebSocket.Listener {

    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    @Override
    public void onOpen(WebSocket webSocket) {
        log.log(Level.INFO, "WebSocket opened to {0}", relay);
    }
}
