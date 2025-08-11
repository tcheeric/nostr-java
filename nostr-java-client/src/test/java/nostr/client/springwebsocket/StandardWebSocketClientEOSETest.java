package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StandardWebSocketClientEOSETest {

  @Test
  public void testCollectsEventsUntilEOSE() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    try (StandardWebSocketClient client = new StandardWebSocketClient(session, 1000, 50)) {
      CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
        try {
          return client.send("test");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      // Ensure the send call has time to register the context
      TimeUnit.MILLISECONDS.sleep(100);

      client.handleTextMessage(session, new TextMessage("{\"type\":\"EVENT\"}"));
      client.handleTextMessage(session, new TextMessage("{\"type\":\"EOSE\"}"));

      List<String> result = future.get(1, TimeUnit.SECONDS);
      assertEquals(2, result.size());
      assertTrue(result.get(0).contains("EVENT"));
      assertTrue(result.get(1).contains("EOSE"));
    }
    Mockito.verify(session).sendMessage(Mockito.any(TextMessage.class));
  }
}
