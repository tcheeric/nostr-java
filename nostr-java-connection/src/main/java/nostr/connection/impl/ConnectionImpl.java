package nostr.connection.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.Connection;
import nostr.connection.impl.listeners.CloseListener;
import nostr.connection.impl.listeners.CompositeListener;
import nostr.connection.impl.listeners.ErrorListener;
import nostr.connection.impl.listeners.OpenListener;
import nostr.connection.impl.listeners.TextListener;
import nostr.context.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@Log
public class ConnectionImpl implements Connection {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final String id = UUID.randomUUID().toString();

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    private final Context context;

    private WebSocket webSocket = null;

    private final AtomicBoolean connected = new AtomicBoolean(false);

    public ConnectionImpl(@NonNull Relay relay, @NonNull Context context) {
        this.relay = relay;
        this.context = context;
    }

    @Override
    public void connect() {
        var relay = getRelay();

        try {
            if (isConnected()) {
                log.log(Level.FINE, "Already connected to {0}. Do nothing...", relay);
                return;
            }

            log.log(Level.INFO, "Connecting to {0}...", relay);
            var client = HttpClient.newHttpClient();
            var compositeListener = new CompositeListener(Arrays.asList(new OpenListener(relay), new TextListener(relay, context), new CloseListener(relay), new ErrorListener(relay)));

            webSocket = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(10000)) // TODO - make this configurable and add to the context.
                    .buildAsync(URI.create(relay.getUri()), compositeListener)
                    .join();

            connected.set(true);

            log.log(Level.INFO, "Connected to {0}", getRelay());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to connect to {0} - {1}", new Object[]{relay, e.getMessage()});
            connected.set(false);
        }
    }

    @Override
    public boolean isConnected() {
        return connected.get() && webSocket != null;
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
            log.log(Level.INFO, "Disconnecting from {0}", relay);
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye").join();
        }
        connected.set(false);
    }
}