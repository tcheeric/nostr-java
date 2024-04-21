package nostr.connection.impl;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.Connection;
import nostr.context.Context;
import nostr.util.NostrUtil;

@Log
public class ConnectionImpl implements Connection {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    private Context context;

    private WebSocket webSocket = null;

    private AtomicBoolean connected = new AtomicBoolean(false);

    public ConnectionImpl(@NonNull Relay relay, @NonNull Context context) {
        this.relay = relay;
        this.context = context;
    }

    @Override
    public void connect() {
    	var relay = getRelay();
        try {
            if (isConnected()) {
                log.log(Level.INFO, "Already connected to {0}. Do nothing...", relay);
                return;
            }

            log.log(Level.INFO, "+Connecting to {0}...", relay);
            var client = HttpClient.newHttpClient();
            var openListener = new WebsocketClientListeners().new OpenListener(relay);
            webSocket = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(10000)) // TODO - make this configurable and add to the context.
                    .buildAsync(NostrUtil.serverURI(relay.getUri()), openListener)
                    .join();
            
            connected.set(true);
            log.log(Level.INFO, "Connected to {0}", getRelay());
        } catch (Exception e) {
        	throw new RuntimeException("Failed to connect to " + getRelay());
		}
    }

    @Override
    public boolean isConnected() {
        return connected.get() && webSocket!=null;
    }

    public void send(@NonNull String message) {
        if (!isConnected()) {
        	log.log(Level.WARNING, "FAIL - Not properly connected with Relay: {0}", relay);
        	return;
        }
        
        log.log(Level.INFO, "Sending message: {0} - Relay: {1}", new Object[]{message, relay});
        webSocket.sendText(message, true);
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            log.log(Level.INFO, "disconnecting from {0}", relay);
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye").join();
        }
        connected.set(false);
    }
}