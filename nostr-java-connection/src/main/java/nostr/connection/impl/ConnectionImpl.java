package nostr.connection.impl;

import lombok.EqualsAndHashCode;
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
import java.util.logging.Level;

@Log
public class ConnectionImpl extends ConnectionListener implements Connection {

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private WebSocket webSocket;

    public ConnectionImpl(@NonNull Relay relay, @NonNull Context context) {
        super(relay, context);
    }

    @Override
    public void connect() {

        var relay = getRelay();

        if (isConnected()) {
            log.log(Level.FINE, "Already connected to {0}. Do nothing...", relay);
            return;
        }

        log.log(Level.INFO, "+Connecting to {0}...", relay);
        HttpClient client = HttpClient.newHttpClient();

        webSocket = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(1000)) // TODO - make this configurable
                .buildAsync(URI.create(relay.getUri()), this)
                .join();
    }

    @Override
    public boolean isConnected() {
        return webSocket != null && (!this.webSocket.isOutputClosed() || !this.webSocket.isInputClosed());
    }

    public void send(@NonNull String message) {
        if (isConnected()) {
            var relay = getRelay();
            log.log(Level.INFO, "Sending message: {0} - Relay: {1}", new Object[]{message, relay});
            webSocket.sendText(message, true);
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            var relay = getRelay();
            log.log(Level.INFO, "disconnecting from {0}", relay);
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye").join();
        }
    }
}