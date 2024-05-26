package nostr.connection.impl.listeners;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import okio.ByteString;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CompositeListener extends WebSocketListener {
    private final List<WebSocketListener> listeners;

    public CompositeListener(List<WebSocketListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        listeners.forEach(listener -> listener.onOpen(webSocket, response));
    }

    @Override
    public void onMessage(WebSocket webSocket, String data) {
        listeners.forEach(listener -> listener.onMessage(webSocket, data));
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        listeners.forEach(listener -> listener.onMessage(webSocket, bytes));
    }

    @Override
    public void onClosing(WebSocket webSocket, int statusCode, String reason) {
        listeners.forEach(listener -> listener.onClosing(webSocket, statusCode, reason));
    }

    @Override
    public void onClosed(WebSocket webSocket, int statusCode, String reason) {
        listeners.forEach(listener -> listener.onClosed(webSocket, statusCode, reason));
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable error, Response response) {
        listeners.forEach(listener -> listener.onFailure(webSocket, error, response));
    }
}
