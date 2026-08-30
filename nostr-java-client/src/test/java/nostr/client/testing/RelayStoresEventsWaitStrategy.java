package nostr.client.testing;

import lombok.extern.slf4j.Slf4j;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.time.Duration;
import java.util.List;

/**
 * Holds a relay container until it has actually stored an event.
 *
 * <p>Ordinary readiness checks are not enough for a Nostr relay. The port binds before database
 * migration completes, and on some hardware a relay worker panics during startup after which the
 * relay still accepts WebSocket connections but never answers. Either way a test's first publish
 * hangs until it times out, and the resulting failure points at the client rather than the
 * container.
 *
 * <p>So readiness is defined as the behaviour the tests actually depend on: publish a throwaway
 * event and require the relay to acknowledge it. A relay that does that is ready by definition.
 */
@Slf4j
public final class RelayStoresEventsWaitStrategy extends AbstractWaitStrategy {

  private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(2);
  private static final Duration PROBE_INTERVAL = Duration.ofMillis(250);
  private static final int PROBE_KIND = 1;

  @Override
  protected void waitUntilReady() {
    String relayUri =
        "ws://" + waitStrategyTarget.getHost() + ":" + waitStrategyTarget.getFirstMappedPort();
    long giveUpAt = System.currentTimeMillis() + startupTimeout.toMillis();

    while (System.currentTimeMillis() < giveUpAt) {
      if (acknowledgesAnEvent(relayUri)) {
        return;
      }
      sleepBriefly();
    }
    throw new IllegalStateException(
        "Relay at " + relayUri + " never acknowledged an event within " + startupTimeout);
  }

  private boolean acknowledgesAnEvent(String relayUri) {
    try (NostrRelayClient client = new NostrRelayClient(relayUri, PROBE_TIMEOUT.toMillis())) {
      List<String> replies = client.send(new EventMessage(probeEvent()));
      return replies.stream().anyMatch(reply -> reply.startsWith("[\"OK\""));
    } catch (Exception notReadyYet) {
      log.debug("Relay {} is not ready: {}", relayUri, notReadyYet.getMessage());
      return false;
    }
  }

  private GenericEvent probeEvent() {
    Identity prober = Identity.generateRandomIdentity();
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(prober.getPublicKey())
            .kind(PROBE_KIND)
            .content("readiness probe")
            .build();
    prober.sign(event);
    return event;
  }

  private void sleepBriefly() {
    try {
      Thread.sleep(PROBE_INTERVAL.toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for a relay", e);
    }
  }
}
