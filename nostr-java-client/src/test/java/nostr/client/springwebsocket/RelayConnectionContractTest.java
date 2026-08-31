package nostr.client.springwebsocket;

import nostr.client.relay.FakeRelay;
import nostr.client.relay.RelayConnection;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs one scenario against both the real {@link NostrRelayClient} and the {@link FakeRelay},
 * asserting they agree.
 *
 * <p>A fake is only worth trusting where it behaves like the thing it replaces. Later work
 * verifies multi-relay behaviour entirely against {@code FakeRelay}, so if the fake's routing
 * diverged from the client's, those tests would pass while production failed. This test is the
 * guard against that: both are driven through the same {@link RelayConnection} interface, and
 * the same assertions are applied to each.
 */
class RelayConnectionContractTest {

  private static final String FIRST_SUBSCRIPTION = "sub-a";
  private static final String SECOND_SUBSCRIPTION = "sub-b";
  private static final String EVENT_FOR_FIRST =
      "[\"EVENT\",\"" + FIRST_SUBSCRIPTION + "\",{\"id\":\"one\"}]";

  // Verifies the real client and the fake agree that a payload naming one subscription reaches
  // only that subscription's listener, so multi-relay tests written against the fake hold for
  // the real transport too.
  @Test
  void realClientAndFakeAgreeOnSubscriptionRouting() throws Exception {
    assertEquals(
        routeThroughRealClient(), routeThroughFake(), "fake diverged from the real relay client");
  }

  private RoutingOutcome routeThroughRealClient() throws Exception {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenReturn(true);

    try (NostrRelayClient client = NostrRelayClient.forTestWithRawSession(session, 1_000)) {
      RoutingOutcome outcome = subscribeToBothSubscriptions(client);
      client.handleTextMessage(session, new TextMessage(EVENT_FOR_FIRST));
      return outcome;
    }
  }

  private RoutingOutcome routeThroughFake() throws Exception {
    try (FakeRelay relay = FakeRelay.accepting("wss://relay.one")) {
      RoutingOutcome outcome = subscribeToBothSubscriptions(relay);
      relay.emitTo(FIRST_SUBSCRIPTION, EVENT_FOR_FIRST);
      return outcome;
    }
  }

  private RoutingOutcome subscribeToBothSubscriptions(RelayConnection relay) throws Exception {
    RoutingOutcome outcome = new RoutingOutcome();
    relay.subscribe(
        new ReqMessage(FIRST_SUBSCRIPTION), outcome.seenByFirst::add, error -> {}, null);
    relay.subscribe(
        new ReqMessage(SECOND_SUBSCRIPTION), outcome.seenBySecond::add, error -> {}, null);
    return outcome;
  }

  /** What each subscription's listener observed, compared across implementations. */
  private static final class RoutingOutcome {
    private final List<String> seenByFirst = new ArrayList<>();
    private final List<String> seenBySecond = new ArrayList<>();

    @Override
    public boolean equals(Object other) {
      return other instanceof RoutingOutcome outcome
          && seenByFirst.equals(outcome.seenByFirst)
          && seenBySecond.equals(outcome.seenBySecond);
    }

    @Override
    public int hashCode() {
      return seenByFirst.hashCode() * 31 + seenBySecond.hashCode();
    }

    @Override
    public String toString() {
      return "first=" + seenByFirst + " second=" + seenBySecond;
    }
  }
}
