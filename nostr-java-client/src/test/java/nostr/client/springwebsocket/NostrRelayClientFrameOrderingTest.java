package nostr.client.springwebsocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import nostr.event.message.ReqMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A listener must be handed EVENT frames before the EOSE that followed them on
 * the wire.
 *
 * <p><b>The failure this pins down.</b> {@code handleTextMessage} is called
 * sequentially per connection, so frames ARRIVE in order. Dispatch used to hand
 * each one to a fresh virtual thread with nothing sequencing them, so they were
 * HANDLED in any order. Every caller ends its query on EOSE, so an EOSE handled
 * before an EVENT that reached the socket first meant that event was simply
 * absent from the result — silently, and differently on each call.</p>
 *
 * <p><b>Why it matters downstream.</b> A wallet fetches its gift-wrapped
 * messages once when it opens and does not ask again. Losing that race shows
 * the customer an empty wallet while their coupons sit on the relay. It is not
 * a tail case: a wallet with one coupon has exactly one EVENT, so that event is
 * always the first, and the race is the whole interaction. Recorded downstream
 * as imani-wallet #36, where byte-identical queries returned zero or one
 * non-deterministically.</p>
 *
 * <p><b>No sleep between frames, deliberately.</b> A delay lets the first
 * frame's thread get far enough that a monitor is enough to order them, which
 * is the case the unordered code already passed while failing in production.
 * The frames go in back to back, which is how a relay sends its stored events
 * and then its EOSE.</p>
 */
@DisplayName("frames reach a listener in the order the relay sent them")
class NostrRelayClientFrameOrderingTest {

  /** Enough frames that an unordered dispatch loses the race reliably. */
  private static final int EVENTS = 200;

  @Test
  @DisplayName("EOSE is handled after every EVENT that preceded it")
  void eoseNeverOvertakesEarlierEvents() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = new NostrRelayClient(session, 1_000)) {
      List<String> handled = new CopyOnWriteArrayList<>();
      // Each EVENT does a little work before being recorded. Real handling
      // parses and verifies a signature, which is exactly the window an EOSE
      // on its own thread can jump; a listener that only appends to a list
      // finishes too fast for the race to show.
      client.subscribe(new ReqMessage("sub-order"), frame -> {
        if (frame.startsWith("[\"EVENT\"")) {
          java.util.concurrent.locks.LockSupport.parkNanos(200_000L);
        }
        handled.add(frame);
      }, t -> { }, null);

      for (int i = 0; i < EVENTS; i++) {
        client.handleTextMessage(session,
            new TextMessage("[\"EVENT\",\"sub-order\",{\"id\":\"e" + i + "\"}]"));
      }
      client.handleTextMessage(session, new TextMessage("[\"EOSE\",\"sub-order\"]"));

      await().atMost(10, TimeUnit.SECONDS)
          .until(() -> handled.stream().anyMatch(f -> f.startsWith("[\"EOSE\"")));

      int eoseAt = -1;
      for (int i = 0; i < handled.size(); i++) {
        if (handled.get(i).startsWith("[\"EOSE\"")) {
          eoseAt = i;
          break;
        }
      }

      assertTrue(eoseAt >= 0, "the EOSE was never handled");

      // The whole contract in one number: everything sent before the EOSE must
      // have been handed over before it. One EVENT landing after is one coupon
      // a customer never sees.
      assertEquals(EVENTS, eoseAt,
          "EOSE was handled at position " + eoseAt + " of " + handled.size()
              + ", so " + (EVENTS - eoseAt) + " EVENT(s) that arrived first were "
              + "handled after it. A caller returning on EOSE drops those.");
    }
  }
}
