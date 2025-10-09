package nostr.api.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import lombok.NonNull;
import nostr.api.NostrSpringWebSocketClient;
import nostr.base.RelayUri;
import nostr.client.WebSocketClientFactory;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

/** Wires NostrSpringWebSocketClient to a mocked handler and verifies subscribe/close flow. */
public class NostrSpringWebSocketClientHandlerIntegrationTest {

  static class TestClient extends NostrSpringWebSocketClient {
    private final WebSocketClientHandler handler;
    TestClient(Identity sender, WebSocketClientHandler handler) { super(sender); this.handler = handler; }

    @Override
    protected WebSocketClientHandler newWebSocketClientHandler(String relayName, RelayUri relayUri)
        throws ExecutionException, InterruptedException {
      return handler;
    }
  }

  @Test
  void clientSubscribeDelegatesToHandlerAndCloseClosesHandle() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    WebSocketClientHandler handler = mock(WebSocketClientHandler.class);
    AutoCloseable handle = mock(AutoCloseable.class);
    when(handler.subscribe(any(), anyString(), any(Consumer.class), any())).thenReturn(handle);

    TestClient client = new TestClient(sender, handler);
    client.setRelays(Map.of("r1", "wss://relay1"));

    AutoCloseable h = client.subscribe(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-i", s -> {});
    verify(handler, times(1)).subscribe(any(), anyString(), any(Consumer.class), any());

    h.close();
    verify(handle, times(1)).close();
  }
}
