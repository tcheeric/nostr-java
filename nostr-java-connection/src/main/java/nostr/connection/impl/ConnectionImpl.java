package nostr.connection.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.Connection;
import nostr.context.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@Log
public class ConnectionImpl implements Connection {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    private Context context;

    private AtomicBoolean connected = new AtomicBoolean(false);

    public ConnectionImpl(@NonNull Relay relay, @NonNull Context context) {
        this.relay = relay;
        this.context = context;
    }

    @Override
    public void connect() {
        try {
            var relay = getRelay();

            if (isConnected()) {
                log.log(Level.FINE, "Already connected to {0}. Do nothing...", relay);
                return;
            }

            log.log(Level.INFO, "+Connecting to {0}...", relay);
            var client = HttpClient.newHttpClient();
            var openListener = new WebsocketClientListeners().new OpenListener(relay);
            var webSocket = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(1000)) // TODO - make this configurable
                    .buildAsync(URI.create(relay.getUri()), openListener)
                    .join();
        } finally {
            connected.set(true);
        }
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    public void send(@NonNull String message) {
        if (isConnected()) {
            var relay = getRelay();
            var client = HttpClient.newHttpClient();
            var textListener = new WebsocketClientListeners().new TextListener(relay, context);
            var webSocket = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(1000)) // TODO - make this configurable
                    .buildAsync(URI.create(relay.getUri()), textListener)
                    .join();

            log.log(Level.INFO, "Sending message: {0} - Relay: {1}", new Object[]{message, relay});
            webSocket.sendText(message, true);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (isConnected()) {
                var relay = getRelay();
                log.log(Level.INFO, "disconnecting from {0}", relay);
                var client = HttpClient.newHttpClient();
                var closeListener = new WebsocketClientListeners().new CloseListener(relay);
                var webSocket = client.newWebSocketBuilder()
                        .connectTimeout(Duration.ofMillis(1000)) // TODO - make this configurable
                        .buildAsync(URI.create(relay.getUri()), closeListener)
                        .join();
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye").join();
            }
        } finally {
            connected.set(false);
        }
    }
}