package nostr.client.springwebsocket;

import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A listener must see frames in the order the relay sent them.
 *
 * <p>Every inbound frame used to be dispatched on a freshly started virtual thread, so two frames
 * could reach a listener in either order. That is invisible for events alone, but the protocol
 * punctuates them: a relay replays its stored events and then sends {@code EOSE}. A listener that
 * sees {@code EOSE} early concludes the backlog is drained while events are still arriving, and
 * every caller ends its query on that signal, so the query returns a partial answer that is
 * indistinguishable from the relay holding less data.
 *
 * <p>Observed against a real relay before the fix: asking for three stored events, the
 * end-of-backlog signal arrived with only one or two delivered, varying run to run.
 */
class NostrRelayClientFrameOrderTest {

  private static final int EVENT_COUNT = 200;

  // Verifies frames arrive in the order they were received, so a signal that terminates a query
  // cannot overtake the events it is meant to follow.
  @Test
  void framesReachAListenerInTheOrderTheRelaySentThem() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      List<String> seen = new CopyOnWriteArrayList<>();
      client.subscribe(new ReqMessage("sub"), seen::add, throwable -> {}, null);

      List<String> sent = new ArrayList<>();
      for (int index = 0; index < EVENT_COUNT; index++) {
        sent.add("[\"EVENT\",\"sub\",{\"id\":\"" + index + "\",\"kind\":1,\"content\":\"e\"}]");
      }
      sent.add("[\"EOSE\",\"sub\"]");
      sent.forEach(payload -> client.handleTextMessage(session, new TextMessage(payload)));

      await().atMost(5, TimeUnit.SECONDS).until(() -> seen.size() == sent.size());
      assertEquals(sent, List.copyOf(seen));
    }
  }

  // Verifies the end-of-backlog signal is delivered last, which is the specific ordering every
  // query depends on to know it has seen everything the relay stored.
  @Test
  void theEndOfStoredEventsSignalArrivesAfterTheEventsItFollows() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      List<String> seen = new CopyOnWriteArrayList<>();
      client.subscribe(new ReqMessage("sub"), seen::add, throwable -> {}, null);

      for (int index = 0; index < EVENT_COUNT; index++) {
        client.handleTextMessage(
            session,
            new TextMessage("[\"EVENT\",\"sub\",{\"id\":\"" + index + "\",\"kind\":1}]"));
      }
      client.handleTextMessage(session, new TextMessage("[\"EOSE\",\"sub\"]"));

      await().atMost(5, TimeUnit.SECONDS).until(() -> seen.size() == EVENT_COUNT + 1);
      assertEquals(EVENT_COUNT, seen.indexOf("[\"EOSE\",\"sub\"]"));
    }
  }
}
