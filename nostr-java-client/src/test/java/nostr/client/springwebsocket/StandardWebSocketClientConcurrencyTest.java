package nostr.client.springwebsocket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class StandardWebSocketClientConcurrencyTest {

    @Test
    void concurrentSendsReceiveDistinctResponses() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        StandardWebSocketClient client = new StandardWebSocketClient(session);
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<List<String>>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> client.send("msg")));
        }
        Field field = StandardWebSocketClient.class.getDeclaredField("contexts");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentLinkedQueue<?> contexts =
                (java.util.concurrent.ConcurrentLinkedQueue<?>) field.get(client);
        while (contexts.size() < threads) {
            TimeUnit.MILLISECONDS.sleep(10);
        }
        for (int i = 0; i < threads; i++) {
            client.handleTextMessage(session, new TextMessage("resp" + i));
        }
        Set<String> results = new HashSet<>();
        for (Future<List<String>> future : futures) {
            results.addAll(future.get(1, TimeUnit.SECONDS));
        }
        assertEquals(threads, results.size());
        executor.shutdownNow();
    }
}
