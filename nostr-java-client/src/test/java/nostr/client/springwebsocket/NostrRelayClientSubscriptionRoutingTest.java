package nostr.client.springwebsocket;

import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A payload addressed to one subscription must not reach another.
 *
 * <p>Listeners are registered per connection, so before routing every REQ saw
 * every other REQ's EVENT and EOSE frames. A caller that ends its query on EOSE
 * — which is every caller — was therefore released by somebody else's EOSE and
 * returned however many events had happened to arrive by then.
 */
class NostrRelayClientSubscriptionRoutingTest {

  private static final String EVENT_A =
      "[\"EVENT\",\"sub-a\",{\"id\":\"a1\",\"kind\":1,\"content\":\"for-a\"}]";
  private static final String EVENT_B =
      "[\"EVENT\",\"sub-b\",{\"id\":\"b1\",\"kind\":1,\"content\":\"for-b\"}]";
  private static final String EOSE_B = "[\"EOSE\",\"sub-b\"]";
  private static final String NOTICE = "[\"NOTICE\",\"relay is restarting\"]";

  @Test
  void payloadsGoOnlyToTheSubscriptionTheyAddress() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      List<String> seenByA = new CopyOnWriteArrayList<>();
      List<String> seenByB = new CopyOnWriteArrayList<>();
      List<String> seenByRaw = new CopyOnWriteArrayList<>();
      AtomicBoolean errored = new AtomicBoolean(false);

      client.subscribe(new ReqMessage("sub-a"), seenByA::add, t -> errored.set(true), null);
      client.subscribe(new ReqMessage("sub-b"), seenByB::add, t -> errored.set(true), null);
      // Subscribed with raw JSON: no id was parsed, so this one still sees the
      // whole connection — the pre-routing contract.
      client.subscribe("[\"REQ\",\"sub-raw\"]", seenByRaw::add, t -> errored.set(true), null);

      client.handleTextMessage(session, new TextMessage(EVENT_B));
      client.handleTextMessage(session, new TextMessage(EOSE_B));
      client.handleTextMessage(session, new TextMessage(EVENT_A));
      client.handleTextMessage(session, new TextMessage(NOTICE));

      // A gets its own EVENT and the connection-wide NOTICE, and nothing of B's.
      await().atMost(2, TimeUnit.SECONDS).until(() -> seenByA.size() >= 2);
      await().atMost(2, TimeUnit.SECONDS).until(() -> seenByB.size() >= 3);
      await().atMost(2, TimeUnit.SECONDS).until(() -> seenByRaw.size() >= 4);

      // Compared as sets: each payload is dispatched on its own executor task,
      // so delivery order across payloads is not part of the contract.
      assertEquals(Set.of(EVENT_A, NOTICE), Set.copyOf(seenByA),
          "sub-a was handed sub-b's traffic — a foreign EOSE ends this caller's query early");
      assertEquals(2, seenByA.size());
      assertEquals(Set.of(EVENT_B, EOSE_B, NOTICE), Set.copyOf(seenByB));
      assertEquals(3, seenByB.size());
      assertEquals(Set.of(EVENT_B, EOSE_B, EVENT_A, NOTICE), Set.copyOf(seenByRaw),
          "a listener registered without a subscription id must still see everything");
      assertEquals(4, seenByRaw.size());
      assertTrue(!errored.get(), "no listener should have errored");
    }
  }
}
