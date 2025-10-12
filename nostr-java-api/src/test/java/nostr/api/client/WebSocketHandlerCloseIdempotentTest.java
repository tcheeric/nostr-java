package nostr.api.client;

import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies calling close twice on a subscription handle does not throw. */
public class WebSocketHandlerCloseIdempotentTest {

  @Test
  void doubleCloseDoesNotThrow() throws ExecutionException, InterruptedException, IOException {
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

    AutoCloseable handle = handler.subscribe(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-dup", s -> {}, t -> {});
    assertDoesNotThrow(handle::close);
    // Second close should also not throw
    assertDoesNotThrow(handle::close);
    verify(client, atLeastOnce()).close();
  }
}
