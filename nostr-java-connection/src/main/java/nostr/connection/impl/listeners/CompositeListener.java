package nostr.connection.impl.listeners;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CompositeListener implements WebSocket.Listener {
    private final List<WebSocket.Listener> listeners;

    public CompositeListener(List<WebSocket.Listener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        listeners.forEach(listener -> listener.onOpen(webSocket));
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        return CompletableFuture.allOf(listeners.stream()
                .map(listener -> {
                    CompletionStage<?> stage = listener.onText(webSocket, data, last);
                    return stage != null ? stage : CompletableFuture.completedFuture(null);
                })
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        return CompletableFuture.allOf(listeners.stream()
                .map(listener -> {
                    CompletionStage<?> stage = listener.onPing(webSocket, message);
                    return stage != null ? stage : CompletableFuture.completedFuture(null);
                })
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        return CompletableFuture.allOf(listeners.stream()
                .map(listener -> {
                    CompletionStage<?> stage = listener.onPong(webSocket, message);
                    return stage != null ? stage : CompletableFuture.completedFuture(null);
                })
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        return CompletableFuture.allOf(listeners.stream()
                .map(listener -> {
                    CompletionStage<?> stage = listener.onClose(webSocket, statusCode, reason);
                    return stage != null ? stage : CompletableFuture.completedFuture(null);
                })
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        listeners.forEach(listener -> listener.onError(webSocket, error));
    }
}
