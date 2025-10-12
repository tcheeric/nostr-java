package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StandardWebSocketClientTimeoutTest {

  @Test
  public void testTimeoutReturnsEmptyListAndClosesSession() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    try (StandardWebSocketClient client = new StandardWebSocketClient(session, 100, 50)) {
      List<String> result = client.send("test");
      assertTrue(result.isEmpty());
    }
    Mockito.verify(session).sendMessage(Mockito.any(TextMessage.class));
    Mockito.verify(session).close();
  }
}
