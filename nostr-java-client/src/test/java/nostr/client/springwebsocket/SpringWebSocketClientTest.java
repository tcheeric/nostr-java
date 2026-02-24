package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Tests for NostrRelayClient send, subscribe, and timeout behavior.
 */
class SpringWebSocketClientTest {

  // Verifies sendAsync completes after the relay emits a termination message.
  @Test
  void sendAsyncReturnsResponseOnTermination() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    NostrRelayClient client = new NostrRelayClient(session, 5000);

    CountDownLatch sendLatch = new CountDownLatch(1);
    doAnswer(invocation -> {
      sendLatch.countDown();
      return null;
    }).when(session).sendMessage(any(TextMessage.class));

    CompletableFuture<List<String>> resultFuture = client.sendAsync("[\"REQ\",\"sub1\",{}]");

    assertTrue(sendLatch.await(1, TimeUnit.SECONDS));
    client.handleTextMessage(session, new TextMessage("[\"EOSE\",\"sub1\"]"));

    List<String> result = resultFuture.get(2, TimeUnit.SECONDS);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0).contains("EOSE"));
  }

  // Verifies blocking send times out and closes the underlying session.
  @Test
  void sendThrowsRelayTimeoutExceptionOnTimeout() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 100)) {
      assertThrows(RelayTimeoutException.class, () -> client.send("test"));
    }
    Mockito.verify(session).sendMessage(any(TextMessage.class));
    Mockito.verify(session, Mockito.atLeastOnce()).close();
  }

  // Verifies subscription callbacks are delivered asynchronously and stop after unsubscribe.
  @Test
  void subscribeDeliversMessages() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      AtomicInteger received = new AtomicInteger();
      AtomicBoolean extraMessageReceived = new AtomicBoolean(false);
      CountDownLatch receiveLatch = new CountDownLatch(2);

      AutoCloseable handle =
          client.subscribe(
              "[\"REQ\",\"sub\"]",
              message -> {
                int count = received.incrementAndGet();
                if (count > 2) {
                  extraMessageReceived.set(true);
                  return;
                }
                receiveLatch.countDown();
              },
              throwable -> {},
              null);

      client.handleTextMessage(session, new TextMessage("event-one"));
      client.handleTextMessage(session, new TextMessage("event-two"));

      assertTrue(receiveLatch.await(1, TimeUnit.SECONDS));
      assertEquals(2, received.get());

      handle.close();
      client.handleTextMessage(session, new TextMessage("event-three"));
      Thread.sleep(100);
      assertEquals(2, received.get());
      assertFalse(extraMessageReceived.get());
    }
  }

  // Verifies listener callbacks are executed on a Virtual Thread, not the caller thread.
  @Test
  void subscribeDispatchesListenerOnVirtualThread() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      long callerThreadId = Thread.currentThread().threadId();
      AtomicReference<Long> callbackThreadId = new AtomicReference<>();
      AtomicBoolean callbackWasVirtual = new AtomicBoolean(false);
      CountDownLatch callbackLatch = new CountDownLatch(1);

      AutoCloseable handle =
          client.subscribe(
              "[\"REQ\",\"sub\"]",
              message -> {
                callbackThreadId.set(Thread.currentThread().threadId());
                callbackWasVirtual.set(Thread.currentThread().isVirtual());
                callbackLatch.countDown();
              },
              throwable -> {},
              null);

      client.handleTextMessage(session, new TextMessage("event-one"));

      assertTrue(callbackLatch.await(1, TimeUnit.SECONDS));
      assertTrue(callbackWasVirtual.get());
      assertNotEquals(callerThreadId, callbackThreadId.get());
      handle.close();
    }
  }

  // Verifies subscribe rejects attempts when the session is already closed.
  @Test
  void subscribeThrowsOnClosedSession() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(false);

    NostrRelayClient client = new NostrRelayClient(session, 1_000);

    assertThrows(IOException.class, () ->
        client.subscribe("[\"REQ\",\"sub\"]", s -> {}, t -> {}, null));
  }

  // Verifies send propagates transport failures from the WebSocket session.
  @Test
  void sendWithSendFailureThrowsIOException() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    Mockito.doThrow(new IOException("connection lost"))
        .when(session).sendMessage(any(TextMessage.class));

    NostrRelayClient client = new NostrRelayClient(session, 1_000);

    assertThrows(IOException.class, () -> client.send("payload"));
  }
}
