package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

class StandardWebSocketClientTest {

    private static StandardWebSocketClient createClient() throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        StandardWebSocketClient client = (StandardWebSocketClient) unsafe.allocateInstance(StandardWebSocketClient.class);

        Field sinkField = StandardWebSocketClient.class.getDeclaredField("sink");
        sinkField.setAccessible(true);
        sinkField.set(client, Sinks.many().multicast().onBackpressureBuffer());

        Field sessionField = StandardWebSocketClient.class.getDeclaredField("clientSession");
        sessionField.setAccessible(true);
        sessionField.set(client, null);
        return client;
    }

    private static class StubSession implements WebSocketSession {
        @Override public String getId() { return "stub"; }
        @Override public java.net.URI getUri() { return null; }
        @Override public org.springframework.http.HttpHeaders getHandshakeHeaders() { return null; }
        @Override public java.util.Map<String, Object> getAttributes() { return java.util.Collections.emptyMap(); }
        @Override public java.security.Principal getPrincipal() { return null; }
        @Override public java.net.InetSocketAddress getLocalAddress() { return null; }
        @Override public java.net.InetSocketAddress getRemoteAddress() { return null; }
        @Override public String getAcceptedProtocol() { return null; }
        @Override public void setTextMessageSizeLimit(int messageSizeLimit) {}
        @Override public int getTextMessageSizeLimit() { return 0; }
        @Override public void setBinaryMessageSizeLimit(int messageSizeLimit) {}
        @Override public int getBinaryMessageSizeLimit() { return 0; }
        @Override public java.util.List<org.springframework.web.socket.WebSocketExtension> getExtensions() { return java.util.Collections.emptyList(); }
        @Override public void sendMessage(org.springframework.web.socket.WebSocketMessage<?> message) {}
        @Override public boolean isOpen() { return true; }
        @Override public void close() {}
        @Override public void close(org.springframework.web.socket.CloseStatus status) {}
    }

    @Test
    void emitsMessagesWithoutBlocking() throws Exception {
        StandardWebSocketClient client = createClient();
        Field sinkField = StandardWebSocketClient.class.getDeclaredField("sink");
        sinkField.setAccessible(true);
        var flux = ((Sinks.Many<String>) sinkField.get(client)).asFlux();
        StepVerifier.create(flux.take(1))
                .then(() -> client.handleTextMessage(new StubSession(), new TextMessage("reply")))
                .expectNext("reply")
                .verifyComplete();
    }
}
