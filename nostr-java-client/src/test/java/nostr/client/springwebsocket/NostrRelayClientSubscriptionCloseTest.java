package nostr.client.springwebsocket;

import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Closing a subscription handle must tell the relay, or relays accumulate open REQs until they
 * refuse new ones ("too many concurrent REQs").
 */
class NostrRelayClientSubscriptionCloseTest {

  private static WebSocketSession openSession() {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenReturn(true);
    return session;
  }

  private static List<String> sentFrames(WebSocketSession session) throws IOException {
    @SuppressWarnings("unchecked")
    ArgumentCaptor<WebSocketMessage<?>> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
    Mockito.verify(session, Mockito.atLeastOnce()).sendMessage(captor.capture());
    return captor.getAllValues().stream().map(m -> ((TextMessage) m).getPayload()).toList();
  }

  // Closing a typed subscription handle twice sends exactly one CLOSE for its id.
  @Test
  void closingTheHandleSendsExactlyOneClose() throws Exception {
    WebSocketSession session = openSession();
    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      AutoCloseable handle = client.subscribe(new ReqMessage("sub-1"), p -> {}, t -> {}, null);

      handle.close();
      handle.close();

      List<String> closes =
          sentFrames(session).stream().filter(f -> f.startsWith("[\"CLOSE\"")).toList();
      assertEquals(List.of("[\"CLOSE\",\"sub-1\"]"), closes);
    }
  }

  // A raw-JSON subscription has no parsed id, so closing its handle sends no CLOSE.
  @Test
  void closingARawJsonSubscriptionSendsNoClose() throws Exception {
    WebSocketSession session = openSession();
    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      AutoCloseable handle = client.subscribe("[\"REQ\",\"raw\",{}]", p -> {}, t -> {}, null);

      handle.close();

      assertTrue(sentFrames(session).stream().noneMatch(f -> f.startsWith("[\"CLOSE\"")));
    }
  }

  // Closing a handle after the session closed only logs, it never throws.
  @Test
  void closingAfterTheSessionClosedDoesNotThrow() throws Exception {
    WebSocketSession session = openSession();
    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      AutoCloseable handle = client.subscribe(new ReqMessage("sub-1"), p -> {}, t -> {}, null);
      Mockito.when(session.isOpen()).thenReturn(false);

      assertDoesNotThrow(handle::close);
    }
  }

  // Subscribe, close, subscribe again on one connection: the second subscription still gets
  // its events, and the relay's CLOSED reply to the first does not end a following request.
  @Test
  void backToBackSubscriptionsOnOneConnectionStillDeliverEvents() throws Exception {
    WebSocketSession session = openSession();
    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      List<String> seenByFirst = new CopyOnWriteArrayList<>();
      List<String> seenBySecond = new CopyOnWriteArrayList<>();

      AutoCloseable first =
          client.subscribe(new ReqMessage("sub-1"), seenByFirst::add, t -> {}, null);
      first.close();
      AutoCloseable second =
          client.subscribe(new ReqMessage("sub-2"), seenBySecond::add, t -> {}, null);

      client.handleTextMessage(session, new TextMessage("[\"CLOSED\",\"sub-1\",\"\"]"));
      String event = "[\"EVENT\",\"sub-2\",{\"id\":\"e1\",\"kind\":1,\"content\":\"hi\"}]";
      client.handleTextMessage(session, new TextMessage(event));
      client.handleTextMessage(session, new TextMessage("[\"EOSE\",\"sub-2\"]"));

      await().atMost(2, TimeUnit.SECONDS).until(() -> seenBySecond.size() >= 2);
      assertEquals(List.of(event, "[\"EOSE\",\"sub-2\"]"), seenBySecond);
      assertTrue(seenByFirst.isEmpty(), "a closed subscription must receive nothing");
      second.close();

      assertEquals(
          List.of(
              "[\"REQ\",\"sub-1\"]", "[\"CLOSE\",\"sub-1\"]",
              "[\"REQ\",\"sub-2\"]", "[\"CLOSE\",\"sub-2\"]"),
          sentFrames(session));
    }
  }
}
