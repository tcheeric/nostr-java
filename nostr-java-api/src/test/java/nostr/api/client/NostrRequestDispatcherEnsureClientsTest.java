package nostr.api.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import nostr.base.SubscriptionId;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import org.junit.jupiter.api.Test;

/** Verifies ensureRequestClients() is invoked per dispatcher call as expected. */
public class NostrRequestDispatcherEnsureClientsTest {

  @Test
  void ensureCalledOnceForSingleFilter() {
    NostrRelayRegistry registry = mock(NostrRelayRegistry.class);
    WebSocketClientHandler handler = mock(WebSocketClientHandler.class);
    when(registry.requestHandlers(eq(SubscriptionId.of("sub-1")))).thenReturn(List.of(handler));
    NostrRequestDispatcher dispatcher = new NostrRequestDispatcher(registry);

    dispatcher.sendRequest(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-1");
    verify(registry, times(1)).ensureRequestClients(eq(SubscriptionId.of("sub-1")));
  }

  @Test
  void ensureCalledPerFilterForListVariant() {
    NostrRelayRegistry registry = mock(NostrRelayRegistry.class);
    WebSocketClientHandler handler = mock(WebSocketClientHandler.class);
    when(registry.requestHandlers(eq(SubscriptionId.of("sub-2")))).thenReturn(List.of(handler));
    NostrRequestDispatcher dispatcher = new NostrRequestDispatcher(registry);

    List<Filters> list = List.of(
        new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)),
        new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE))
    );
    dispatcher.sendRequest(list, "sub-2");
    verify(registry, times(2)).ensureRequestClients(eq(SubscriptionId.of("sub-2")));
  }
}

