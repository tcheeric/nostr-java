package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardWebSocketClientConcurrencyTest {

    static class StubWebSocketSession implements WebSocketSession {
        private final CountDownLatch sendLatch;
        private boolean open = true;

        StubWebSocketSession(CountDownLatch sendLatch) {
            this.sendLatch = sendLatch;
        }

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
        public void sendMessage(WebSocketMessage<?> message) {
            sendLatch.countDown();
        }

        @Override
        public boolean isOpen() { return open; }

        @Override
        public void close() { open = false; }

        @Override
        public void close(CloseStatus status) { open = false; }
    }

    @Test
    void concurrentSendsReceiveResponses() throws Exception {
        CountDownLatch sendLatch = new CountDownLatch(2);
        StubWebSocketSession session = new StubWebSocketSession(sendLatch);
        StandardWebSocketClient client = new StandardWebSocketClient(session, 1000, 10);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<List<String>> f1 = executor.submit(() -> {
            start.await();
            return client.send("msg1");
        });
        Future<List<String>> f2 = executor.submit(() -> {
            start.await();
            return client.send("msg2");
        });

        start.countDown();
        sendLatch.await(1, TimeUnit.SECONDS);

        client.handleTextMessage(session, new TextMessage("resp1"));
        client.handleTextMessage(session, new TextMessage("resp2"));

        List<String> r1 = f1.get(2, TimeUnit.SECONDS);
        List<String> r2 = f2.get(2, TimeUnit.SECONDS);
        assertEquals(Set.of(List.of("resp1"), List.of("resp2")), Set.of(r1, r2));

        executor.shutdownNow();
    }
}
