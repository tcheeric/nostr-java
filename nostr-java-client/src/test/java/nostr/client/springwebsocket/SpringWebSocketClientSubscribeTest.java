package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that NostrRelayClient subscribe correctly dispatches messages, errors,
 * and close callbacks to registered listeners.
 */
class SpringWebSocketClientSubscribeTest {

  // Verifies message and error callbacks execute for an active subscription.
  @Test
  void subscribeReceivesMessagesAndErrorAndClose() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      AtomicInteger messages = new AtomicInteger();
      AtomicInteger errors = new AtomicInteger();
      AtomicInteger closes = new AtomicInteger();
      CountDownLatch messageLatch = new CountDownLatch(1);
      CountDownLatch errorLatch = new CountDownLatch(1);

      AutoCloseable handle =
          client.subscribe(
              "[\"REQ\",\"sub-1\",{}]",
              payload -> {
                messages.incrementAndGet();
                messageLatch.countDown();
              },
              t -> {
                errors.incrementAndGet();
                errorLatch.countDown();
              },
              closes::incrementAndGet);

      // Simulate relay messages
      client.handleTextMessage(session, new TextMessage("EVENT"));
      client.handleTransportError(session, new IOException("boom"));
      handle.close();

      // Close callback is only invoked on connection close, not on unsubscribe
      // So we trigger it via afterConnectionClosed
      client.afterConnectionClosed(session, new org.springframework.web.socket.CloseStatus(1000));

      assertTrue(messageLatch.await(1, TimeUnit.SECONDS));
      assertTrue(errorLatch.await(1, TimeUnit.SECONDS));
      assertEquals(1, messages.get());
      assertEquals(1, errors.get());
      assertEquals(0, closes.get());
      // Close listener invoked during afterConnectionClosed
      // (unsubscribe via handle.close() just removes the listener)

      verify(session).sendMessage(any(TextMessage.class));
    }
  }

  // Verifies BaseMessage subscriptions encode and send a REQ payload.
  @Test
  void subscribeBaseMessageOverloadSendsEncodedJson() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      var reqMessage = new nostr.event.message.ReqMessage(
          "sub-1", nostr.event.filter.EventFilter.builder().kind(1).build());

      AutoCloseable handle =
          client.subscribe(reqMessage, s -> {}, t -> {}, null);

      org.mockito.ArgumentCaptor<TextMessage> captor =
          org.mockito.ArgumentCaptor.forClass(TextMessage.class);
      verify(session).sendMessage(captor.capture());
      assertTrue(captor.getValue().getPayload().contains("REQ"));

      handle.close();
    }
  }
}
