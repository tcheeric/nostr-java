package nostr.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

/** Ensures sendRequest wraps IOExceptions as RuntimeException with context. */
public class WebSocketHandlerRequestErrorTest {

  @Test
  void sendRequestWrapsIOException() throws ExecutionException, InterruptedException, IOException {
    SpringWebSocketClient client = mock(SpringWebSocketClient.class);
    when(client.send(any(nostr.event.message.ReqMessage.class))).thenThrow(new IOException("net-broken"));
    WebSocketClientFactory factory = mock(WebSocketClientFactory.class);
    Function<SubscriptionId, SpringWebSocketClient> reqFactory = k -> client;
    nostr.api.WebSocketClientHandler handler =
        nostr.api.TestHandlerFactory.create(
            "relay-x", "wss://relayx", client, reqFactory, factory);

    Filters filters = new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE));
    RuntimeException ex = assertThrows(RuntimeException.class, () -> handler.sendRequest(filters, SubscriptionId.of("sub-err")));
    assertEquals("Failed to send request", ex.getMessage());
  }
}
