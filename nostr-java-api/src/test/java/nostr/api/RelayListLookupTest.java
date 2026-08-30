package nostr.api;

import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.client.relay.FakeRelay;
import nostr.client.relay.RelayPool;
import nostr.event.impl.DirectMessageRelayList;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies a recipient's direct message relays are resolved from what relays actually hold. */
class RelayListLookupTest {

  private static final String FIRST_RELAY = "wss://relay.one";
  private static final String SECOND_RELAY = "wss://relay.two";

  private final Map<String, FakeRelay> relays = new ConcurrentHashMap<>();

  // Verifies a published kind-10050 list is found and its relays returned, which is what makes
  // NIP-17 delivery possible at all.
  @Test
  void aPublishedRelayListIsFound() throws Exception {
    Identity recipient = Identity.generateRandomIdentity();
    GenericEvent published = relayListOf(recipient, "wss://inbox.one", "wss://inbox.two");

    try (RelayPool pool = poolOf(FIRST_RELAY, SECOND_RELAY)) {
      RelayListLookup lookup = new RelayListLookup(pool);
      answerLookupsWith(published);

      Optional<DirectMessageRelayList> found = lookup.findFor(recipient.getPublicKey());

      assertTrue(found.isPresent(), "the published relay list was not found");
      assertEquals(
          List.of("wss://inbox.one", "wss://inbox.two"),
          found.orElseThrow().getRelays().stream().map(Relay::getUri).toList());
    }
  }

  // Verifies someone who published no list yields an empty result rather than an error, since
  // NIP-17 treats that as declining private messages.
  @Test
  void someoneWithNoPublishedListYieldsAnEmptyResult() throws Exception {
    try (RelayPool pool = poolOf(FIRST_RELAY)) {
      RelayListLookup lookup = new RelayListLookup(pool);
      relays.values().forEach(relay -> relay.emitEndOfStoredEventsForNextSubscription());

      Optional<DirectMessageRelayList> found =
          lookup.findFor(Identity.generateRandomIdentity().getPublicKey());

      assertTrue(found.isEmpty());
    }
  }

  // Verifies a list held by only one relay is still found, so a recipient is not treated as
  // unreachable because some relays lack their list.
  @Test
  void aListHeldByOnlyOneRelayIsStillFound() throws Exception {
    Identity recipient = Identity.generateRandomIdentity();
    GenericEvent published = relayListOf(recipient, "wss://inbox.one");

    try (RelayPool pool = poolOf(FIRST_RELAY, SECOND_RELAY)) {
      RelayListLookup lookup = new RelayListLookup(pool);
      relays.get(FIRST_RELAY).answerNextSubscriptionWith(published);
      relays.get(SECOND_RELAY).emitEndOfStoredEventsForNextSubscription();

      assertTrue(lookup.findFor(recipient.getPublicKey()).isPresent());
    }
  }

  private void answerLookupsWith(GenericEvent relayList) {
    relays.values().forEach(relay -> relay.answerNextSubscriptionWith(relayList));
  }

  private GenericEvent relayListOf(Identity owner, String... relayUris) {
    DirectMessageRelayList relayList =
        new DirectMessageRelayList(
            owner.getPublicKey(),
            List.of(relayUris).stream().map(Relay::new).toList(),
            System.currentTimeMillis() / 1000);
    GenericEvent event = relayList.toEvent();
    owner.sign(event);
    return event;
  }

  private RelayPool poolOf(String... relayUris) {
    return new RelayPool(
        List.of(relayUris), relayUri -> relays.computeIfAbsent(relayUri, FakeRelay::accepting));
  }
}
