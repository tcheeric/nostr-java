package nostr.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import nostr.api.WebSocketClientHandler;
import nostr.base.SubscriptionId;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import org.junit.jupiter.api.Test;

/** Tests for NostrRequestDispatcher multi-filter dispatch and aggregation. */
public class NostrRequestDispatcherTest {

  @Test
  void multiFilterDispatchAggregatesResponses() {
    NostrRelayRegistry registry = mock(NostrRelayRegistry.class);
    WebSocketClientHandler handler = mock(WebSocketClientHandler.class);

    when(registry.requestHandlers(eq(SubscriptionId.of("sub-Z")))).thenReturn(List.of(handler));
    doNothing().when(registry).ensureRequestClients(eq(SubscriptionId.of("sub-Z")));

    when(handler.sendRequest(any(Filters.class), eq(SubscriptionId.of("sub-Z"))))
        .thenReturn(List.of("R1"))
        .thenReturn(List.of("R2"));

    NostrRequestDispatcher dispatcher = new NostrRequestDispatcher(registry);
    List<Filters> list =
        List.of(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)),
                new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)));

    var out = dispatcher.sendRequest(list, "sub-Z");
    assertEquals(2, out.size());
    // ensure each filter triggered a send on handler
    verify(handler, times(2)).sendRequest(any(Filters.class), eq(SubscriptionId.of("sub-Z")));
  }

  @Test
  void multiFilterDispatchDeduplicatesResponses() {
    NostrRelayRegistry registry = mock(NostrRelayRegistry.class);
    WebSocketClientHandler handler = mock(WebSocketClientHandler.class);
    when(registry.requestHandlers(eq(SubscriptionId.of("sub-D")))).thenReturn(List.of(handler));
    doNothing().when(registry).ensureRequestClients(eq(SubscriptionId.of("sub-D")));

    // Return the same response for both filters; expect distinct aggregation
    when(handler.sendRequest(any(Filters.class), eq(SubscriptionId.of("sub-D"))))
        .thenReturn(List.of("DUP"))
        .thenReturn(List.of("DUP"));

    NostrRequestDispatcher dispatcher = new NostrRequestDispatcher(registry);
    List<Filters> list =
        List.of(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)),
                new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)));

    var out = dispatcher.sendRequest(list, "sub-D");
    assertEquals(1, out.size());
    verify(handler, times(2)).sendRequest(any(Filters.class), eq(SubscriptionId.of("sub-D")));
  }
}
