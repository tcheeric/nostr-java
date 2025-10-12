package nostr.api.client;

import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests sendRequest for multiple sub ids and verifying subscription id usage. */
public class WebSocketHandlerSendRequestTest {

  @Test
  void sendsReqWithGivenSubscriptionId() throws ExecutionException, InterruptedException, IOException {
    SpringWebSocketClient client = mock(SpringWebSocketClient.class);
    when(client.send(any(nostr.event.message.ReqMessage.class))).thenReturn(List.of("OK"));
    WebSocketClientFactory factory = mock(WebSocketClientFactory.class);
    Function<SubscriptionId, SpringWebSocketClient> reqFactory = k -> client;

    nostr.api.WebSocketClientHandler handler =
        nostr.api.TestHandlerFactory.create(
            "relay-1", "wss://relay1", client, reqFactory, factory);

    Filters filters = new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE));
    handler.sendRequest(filters, SubscriptionId.of("sub-A"));
    handler.sendRequest(filters, SubscriptionId.of("sub-B"));

    ArgumentCaptor<nostr.event.message.ReqMessage> captor =
        ArgumentCaptor.forClass(nostr.event.message.ReqMessage.class);
    verify(client, times(2)).send(captor.capture());
    assertTrue(captor.getAllValues().get(0).encode().contains("\"sub-A\""));
    assertTrue(captor.getAllValues().get(1).encode().contains("\"sub-B\""));
  }
}
