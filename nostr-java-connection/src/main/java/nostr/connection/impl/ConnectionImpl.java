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
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.Request;
import okhttp3.HttpUrl;
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

            log.log(Level.INFO, "Connecting to {0}... httpUrl = {1}", new Object[]{relay, HttpUrl.parse(relay.getHttpUri())});
            var client = new OkHttpClient();
            var compositeListener = new CompositeListener(Arrays.asList(new OpenListener(relay), new TextListener(relay, context), new CloseListener(relay), new ErrorListener(relay)));
            
            webSocket = client.newWebSocket((new Request.Builder()).url(HttpUrl.parse(relay.getHttpUri())).build(), compositeListener);
            //.connectTimeout(Duration.ofMillis(10000)) // TODO - make this configurable and add to the context.

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

    @Override
    public void send(@NonNull String message) {
        if (!isConnected()) {
            log.log(Level.WARNING, "FAIL - Not properly connected with Relay: {0}", relay);
            return;
        }

        log.log(Level.INFO, "Sending message: {0} - Relay: {1}", new Object[]{message, relay});
        webSocket.send(message);
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            log.log(Level.INFO, "Disconnecting from {0}", relay);
            // NORMAL_CLOSURE is 1000
            webSocket.close(1000, "bye"); // TODO check return result of close
        }
        connected.set(false);
    }
}