package nostr.api;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.client.relay.RelayPool;
import nostr.client.relay.RelaySubscription;
import nostr.client.relay.SubscriptionListener;
import nostr.encryption.DirectMessageRelayLookup;
import nostr.event.filter.EventFilter;
import nostr.event.impl.DirectMessageRelayList;
import nostr.event.impl.GenericEvent;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Finds where someone receives private direct messages, by asking relays.
 *
 * <p>NIP-17 delivers a message only to the relays its recipient nominated in a kind-10050 list,
 * so sending one requires resolving that list first. {@code nostr-java-identity} declares the
 * need as {@link DirectMessageRelayLookup} but cannot satisfy it, because answering means
 * querying relays and that module is deliberately transport-free. This implementation supplies
 * the answer from a relay pool, keeping the dependency pointing from transport towards policy.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
@Slf4j
public class RelayListLookup implements DirectMessageRelayLookup {

  /** How long to wait for relays to answer before concluding no list was published. */
  public static final Duration DEFAULT_LOOKUP_TIMEOUT = Duration.ofSeconds(5);

  private static final int DIRECT_MESSAGE_RELAY_LIST_KIND = 10050;

  private final RelayPool relayPool;
  private final Duration lookupTimeout;

  /**
   * @param relayPool the relays to ask
   */
  public RelayListLookup(@NonNull RelayPool relayPool) {
    this(relayPool, DEFAULT_LOOKUP_TIMEOUT);
  }

  /**
   * @param relayPool the relays to ask
   * @param lookupTimeout how long to wait before concluding no list was published
   */
  public RelayListLookup(@NonNull RelayPool relayPool, @NonNull Duration lookupTimeout) {
    this.relayPool = relayPool;
    this.lookupTimeout = lookupTimeout;
  }

  /**
   * Find the relay list this key published for receiving direct messages.
   *
   * <p>An empty result means no list was found, which NIP-17 treats as declining private
   * messages rather than as an error. A relay that never answers therefore looks the same as one
   * confirming no list exists, which is why the wait is bounded.
   *
   * @param owner the key whose relay list is wanted
   * @return their relay list, or empty when they published none
   */
  @Override
  public Optional<DirectMessageRelayList> findFor(@NonNull PublicKey owner) {
    AtomicReference<GenericEvent> mostRecentList = new AtomicReference<>();
    CountDownLatch backlogDrained = new CountDownLatch(1);

    try (RelaySubscription subscription =
        relayPool.subscribe(
            List.of(relayListFilterFor(owner)),
            collectMostRecent(mostRecentList, backlogDrained))) {

      awaitBacklog(backlogDrained);
    }
    return Optional.ofNullable(mostRecentList.get()).map(DirectMessageRelayList::from);
  }

  private EventFilter relayListFilterFor(PublicKey owner) {
    return EventFilter.builder()
        .author(owner.toString())
        .kind(DIRECT_MESSAGE_RELAY_LIST_KIND)
        .build();
  }

  /**
   * Keep the newest list seen, since relays can hold different revisions.
   *
   * <p>A replaceable event's latest version is the one that counts, and an older copy lingering
   * on one relay must not override a newer one held elsewhere.
   */
  private SubscriptionListener collectMostRecent(
      AtomicReference<GenericEvent> mostRecentList, CountDownLatch backlogDrained) {
    return new SubscriptionListener() {
      @Override
      public void onEvent(GenericEvent event) {
        mostRecentList.accumulateAndGet(event, RelayListLookup::newerOf);
      }

      @Override
      public void onEndOfStoredEvents() {
        backlogDrained.countDown();
      }
    };
  }

  /**
   * Choose the newer of the list already held and one just received.
   *
   * <p>Argument order follows {@code AtomicReference.accumulateAndGet}, which passes the current
   * value first and the new one second; either may be absent on the first sighting.
   */
  private static GenericEvent newerOf(GenericEvent held, GenericEvent received) {
    if (held == null) {
      return received;
    }
    if (received == null) {
      return held;
    }
    return publishedAt(received) >= publishedAt(held) ? received : held;
  }

  /**
   * When an event claims to have been created, treating an absent timestamp as oldest.
   *
   * <p>A relay can serve an event without one, and an undated copy must never displace a dated
   * one that is known to be current.
   */
  private static long publishedAt(GenericEvent event) {
    return event.getCreatedAt() == null ? Long.MIN_VALUE : event.getCreatedAt();
  }

  private void awaitBacklog(CountDownLatch backlogDrained) {
    try {
      backlogDrained.await(lookupTimeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while looking up a direct message relay list");
    }
  }
}
