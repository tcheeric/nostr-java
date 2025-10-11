package nostr.api.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import nostr.api.WebSocketClientHandler;
import org.junit.jupiter.api.Test;

/** Tests close semantics and error aggregation in NostrSubscriptionManager. */
public class NostrSubscriptionManagerCloseTest {

  @Test
  // When closing multiple handles, IOException takes precedence; errors are reported to consumer.
  void closesAllHandlesAndAggregatesErrors() throws Exception {
    NostrRelayRegistry registry = mock(NostrRelayRegistry.class);
    WebSocketClientHandler h1 = mock(WebSocketClientHandler.class);
    WebSocketClientHandler h2 = mock(WebSocketClientHandler.class);
    when(registry.baseHandlers()).thenReturn(List.of(h1, h2));

    AutoCloseable c1 = mock(AutoCloseable.class);
    AutoCloseable c2 = mock(AutoCloseable.class);
    when(h1.subscribe(any(), anyString(), any(), any())).thenReturn(c1);
    when(h2.subscribe(any(), anyString(), any(), any())).thenReturn(c2);

    NostrSubscriptionManager mgr = new NostrSubscriptionManager(registry);
    AtomicInteger errorCount = new AtomicInteger();
    Consumer<Throwable> errorConsumer = t -> errorCount.incrementAndGet();
    AutoCloseable handle = mgr.subscribe(new nostr.event.filter.Filters(new nostr.event.filter.KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "subX", s -> {}, errorConsumer);

    doThrow(new IOException("iofail")).when(c1).close();
    doThrow(new RuntimeException("boom")).when(c2).close();

    IOException thrown = assertThrows(IOException.class, handle::close);
    assertEquals("iofail", thrown.getMessage());
    // Both errors reported
    assertEquals(2, errorCount.get());
  }

  @Test
  // If subscribe fails mid-iteration, previously acquired handles are closed and error reported.
  void subscribeFailureClosesAcquiredHandles() throws Exception {
    NostrRelayRegistry registry = mock(NostrRelayRegistry.class);
    WebSocketClientHandler h1 = mock(WebSocketClientHandler.class);
    WebSocketClientHandler h2 = mock(WebSocketClientHandler.class);
    when(registry.baseHandlers()).thenReturn(List.of(h1, h2));

    AutoCloseable c1 = mock(AutoCloseable.class);
    when(h1.subscribe(any(), anyString(), any(), any())).thenReturn(c1);
    when(h2.subscribe(any(), anyString(), any(), any())).thenThrow(new RuntimeException("sub-fail"));

    NostrSubscriptionManager mgr = new NostrSubscriptionManager(registry);
    AtomicInteger errorCount = new AtomicInteger();
    Consumer<Throwable> errorConsumer = t -> errorCount.incrementAndGet();

    assertThrows(RuntimeException.class, () ->
        mgr.subscribe(new nostr.event.filter.Filters(new nostr.event.filter.KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "subY", s -> {}, errorConsumer));

    // First handle should be closed due to failure in second subscribe
    verify(c1, times(1)).close();
    // Error consumer not invoked because close succeeded (no exception during cleanup)
    assertEquals(0, errorCount.get());
  }
}

