package nostr.api.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
import nostr.event.message.CloseMessage;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Verifies WebSocketClientHandler close sends CLOSE frame and closes client. */
public class WebSocketHandlerSendCloseFrameTest {

  @Test
  void closeSendsCloseFrameAndClosesClient() throws ExecutionException, InterruptedException, IOException {
    SpringWebSocketClient client = mock(SpringWebSocketClient.class);
    when(client.subscribe(any(ReqMessage.class), any(), any(), any())).thenReturn(() -> {});
    when(client.subscribe(any(CloseMessage.class), any(), any(), any())).thenReturn(() -> {});

    WebSocketClientFactory factory = mock(WebSocketClientFactory.class);
    Function<SubscriptionId, SpringWebSocketClient> reqFactory = k -> client;

    nostr.api.WebSocketClientHandler handler =
        nostr.api.TestHandlerFactory.create(
            "relay-1", "wss://relay1", client, reqFactory, factory);

    AutoCloseable handle = handler.subscribe(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-123", s -> {}, t -> {});

    // Close and verify a CLOSE frame was sent
    handle.close();
    ArgumentCaptor<CloseMessage> captor = ArgumentCaptor.forClass(CloseMessage.class);
    verify(client, atLeastOnce()).subscribe(captor.capture(), any(), any(), any());
    boolean closeSent = captor.getAllValues().stream().anyMatch(m -> m.encode().contains("\"CLOSE\",\"sub-123\""));
    assertTrue(closeSent);
    verify(client, atLeastOnce()).close();
  }
}
