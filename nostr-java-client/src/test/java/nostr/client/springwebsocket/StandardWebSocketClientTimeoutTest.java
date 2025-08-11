package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardWebSocketClientTimeoutTest {

    static class StubWebSocketSession implements WebSocketSession {
        private boolean open = true;

        @Override
        public String getId() { return "1"; }

        @Override
        public URI getUri() { return null; }

        @Override
        public HttpHeaders getHandshakeHeaders() { return new HttpHeaders(); }

        @Override
        public Map<String, Object> getAttributes() { return Collections.emptyMap(); }

        @Override
        public Principal getPrincipal() { return null; }

        @Override
        public InetSocketAddress getLocalAddress() { return null; }

        @Override
        public InetSocketAddress getRemoteAddress() { return null; }

        @Override
        public String getAcceptedProtocol() { return null; }

        @Override
        public void setTextMessageSizeLimit(int messageSizeLimit) { }

        @Override
        public int getTextMessageSizeLimit() { return 0; }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) { }

        @Override
        public int getBinaryMessageSizeLimit() { return 0; }

        @Override
        public List<WebSocketExtension> getExtensions() { return List.of(); }

        @Override
        public void sendMessage(WebSocketMessage<?> message) { }

        @Override
        public boolean isOpen() { return open; }

        @Override
        public void close() { open = false; }

        @Override
        public void close(CloseStatus status) { open = false; }
    }

    @Test
    void testTimeoutReturnsEmptyListAndClosesSession() throws Exception {
        StubWebSocketSession session = new StubWebSocketSession();
        StandardWebSocketClient client = new StandardWebSocketClient(session, 100, 10);

        assertTrue(client.send("payload").isEmpty());
        assertFalse(session.isOpen());
    }
}
