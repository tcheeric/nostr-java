package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardWebSocketClientSubscriptionTest {

  // Verifies that subscription listeners receive multiple messages without blocking the caller.
  @Test
  void subscribeDeliversMultipleMessagesWithoutBlocking() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenReturn(true);

    try (StandardWebSocketClient client = new StandardWebSocketClient(session, 1_000, 50)) {
      AtomicInteger received = new AtomicInteger();
      AtomicBoolean errorInvoked = new AtomicBoolean(false);

      AutoCloseable handle =
          client.subscribe(
              "[\"REQ\",\"sub\"]",
              message -> received.incrementAndGet(),
              throwable -> errorInvoked.set(true),
              null);

      client.handleTextMessage(session, new TextMessage("event-one"));
      client.handleTextMessage(session, new TextMessage("event-two"));

      assertEquals(2, received.get());
      assertFalse(errorInvoked.get());

      handle.close();
      client.handleTextMessage(session, new TextMessage("event-three"));
      assertEquals(2, received.get());

      ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
      Mockito.verify(session).sendMessage(messageCaptor.capture());
      assertTrue(messageCaptor.getValue().getPayload().contains("REQ"));
    }
  }
}
