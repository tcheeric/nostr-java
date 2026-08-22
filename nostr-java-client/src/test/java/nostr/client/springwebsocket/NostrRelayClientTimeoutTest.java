package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NostrRelayClientTimeoutTest {

  @Test
  public void testTimeoutThrowsRelayTimeoutExceptionAndClosesSession() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    // The session is still open when the response times out, so the timeout
    // path proceeds to close it.
    Mockito.when(session.isOpen()).thenReturn(true);
    try (NostrRelayClient client = new NostrRelayClient(session, 100)) {
      assertThrows(RelayTimeoutException.class, () -> client.send("test"));
    }
    Mockito.verify(session).sendMessage(Mockito.any(TextMessage.class));
    // The fix routes every close through close(CloseStatus) — never the no-arg
    // close(), which the ConcurrentWebSocketSessionDecorator does not override
    // and which would bypass its close/flush coordination (spec-026 US3).
    Mockito.verify(session, Mockito.atLeastOnce()).close(Mockito.any(CloseStatus.class));
    Mockito.verify(session, Mockito.never()).close();
  }
}
