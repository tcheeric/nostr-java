package nostr.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import nostr.base.RelayUri;
import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/** Ensures CLOSE frame is sent before delegate and client close, even on exceptions. */
public class WebSocketHandlerCloseSequencingTest {

  @Test
  void closeOrderIsCloseFrameThenDelegateThenClient() throws Exception {
    SpringWebSocketClient client = mock(SpringWebSocketClient.class);
    AutoCloseable delegate = mock(AutoCloseable.class);
    AutoCloseable closeFrame = mock(AutoCloseable.class);
    when(client.subscribe(any(nostr.event.message.ReqMessage.class), any(), any(), any()))
        .thenReturn(delegate);
    when(client.subscribe(any(nostr.event.message.CloseMessage.class), any(), any(), any()))
        .thenReturn(closeFrame);

    WebSocketClientFactory factory = mock(WebSocketClientFactory.class);
    Function<SubscriptionId, SpringWebSocketClient> reqFactory = k -> client;

    nostr.api.WebSocketClientHandler handler =
        nostr.api.TestHandlerFactory.create(
            "relay-1", "wss://relay1", client, reqFactory, factory);

    AutoCloseable handle = handler.subscribe(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-789", s -> {}, t -> {});
    handle.close();

    InOrder inOrder = inOrder(closeFrame, delegate, client);
    inOrder.verify(closeFrame, times(1)).close();
    inOrder.verify(delegate, times(1)).close();
    inOrder.verify(client, times(1)).close();
  }

  @Test
  void exceptionsStillAttemptAllClosesAndThrowFirstIo() throws Exception {
    SpringWebSocketClient client = mock(SpringWebSocketClient.class);
    AutoCloseable delegate = mock(AutoCloseable.class);
    AutoCloseable closeFrame = mock(AutoCloseable.class);
    when(client.subscribe(any(nostr.event.message.ReqMessage.class), any(), any(), any()))
        .thenReturn(delegate);
    when(client.subscribe(any(nostr.event.message.CloseMessage.class), any(), any(), any()))
        .thenReturn(closeFrame);

    doThrow(new IOException("frame-io")).when(closeFrame).close();
    doThrow(new RuntimeException("del-boom")).when(delegate).close();
    doThrow(new IOException("client-io")).when(client).close();

    WebSocketClientFactory factory = mock(WebSocketClientFactory.class);
    Function<SubscriptionId, SpringWebSocketClient> reqFactory = k -> client;
    nostr.api.WebSocketClientHandler handler =
        nostr.api.TestHandlerFactory.create(
            "relay-1", "wss://relay1", client, reqFactory, factory);

    AutoCloseable handle = handler.subscribe(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-err", s -> {}, t -> {});
    IOException thrown = assertEqualsType(IOException.class, () -> handle.close());
    assertEquals("frame-io", thrown.getMessage());

    // All closes attempted even on exceptions
    verify(closeFrame, times(1)).close();
    verify(delegate, times(1)).close();
    verify(client, times(1)).close();
  }

  private static <T extends Throwable> T assertEqualsType(Class<T> type, Executable executable) {
    try {
      executable.exec();
      throw new AssertionError("Expected exception: " + type.getSimpleName());
    } catch (Throwable t) {
      if (type.isInstance(t)) {
        return type.cast(t);
      }
      throw new AssertionError("Unexpected exception type: " + t.getClass(), t);
    }
  }

  @FunctionalInterface
  private interface Executable { void exec() throws Exception; }
}
