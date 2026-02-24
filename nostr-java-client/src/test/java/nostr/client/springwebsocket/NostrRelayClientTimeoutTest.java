package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NostrRelayClientTimeoutTest {

  @Test
  public void testTimeoutThrowsRelayTimeoutExceptionAndClosesSession() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    try (NostrRelayClient client = new NostrRelayClient(session, 100)) {
      assertThrows(RelayTimeoutException.class, () -> client.send("test"));
    }
    Mockito.verify(session).sendMessage(Mockito.any(TextMessage.class));
    Mockito.verify(session).close();
  }
}
