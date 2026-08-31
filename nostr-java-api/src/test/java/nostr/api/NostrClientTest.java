package nostr.api;

import nostr.base.PublicKey;
import nostr.client.relay.FakeRelay;
import nostr.client.relay.NoRelayAcceptedException;
import nostr.client.relay.PublishResult;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.ConnectionState;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the facade signs, publishes, and cleans up according to who owns what. */
class NostrClientTest {

  private static final String FIRST_RELAY = "wss://relay.one";
  private static final String SECOND_RELAY = "wss://relay.two";

  private final Map<String, FakeRelay> relays = new ConcurrentHashMap<>();
  private final Identity identity = Identity.generateRandomIdentity();

  // Verifies a text note is signed by the configured identity and published to every relay,
  // collapsing build, sign and publish into one call.
  @Test
  void aTextNoteIsSignedAndPublishedToEveryRelay() throws Exception {
    try (NostrClient nostr = clientWithRelays(FIRST_RELAY, SECOND_RELAY)) {
      PublishResult result = nostr.publishTextNote("Hello Nostr!");

      assertEquals(List.of(FIRST_RELAY, SECOND_RELAY), result.getAcceptingRelays());
      GenericEvent published = firstEventSentTo(FIRST_RELAY);
      assertEquals(identity.getPublicKey(), published.getPubKey());
      assertEquals("Hello Nostr!", published.getContent());
      assertNotNull(published.getSignature(), "the note was published unsigned");
    }
  }

  // Verifies an event can be signed by another identity, so one client can act for several keys
  // rather than needing one client per key.
  @Test
  void anEventCanBeSignedByAnotherIdentity() throws Exception {
    Identity otherAccount = Identity.generateRandomIdentity();

    try (NostrClient nostr = clientWithRelays(FIRST_RELAY)) {
      nostr.publishAs(
          otherAccount,
          GenericEvent.builder()
              .pubKey(otherAccount.getPublicKey())
              .kind(1)
              .content("posted for another account")
              .build());

      assertEquals(otherAccount.getPublicKey(), firstEventSentTo(FIRST_RELAY).getPubKey());
      assertNotEquals(identity.getPublicKey(), firstEventSentTo(FIRST_RELAY).getPubKey());
    }
  }

  // Verifies publishing throws when no relay accepted, so an event that reached nobody cannot be
  // mistaken for a published one.
  @Test
  void publishingThrowsWhenNoRelayAccepted() throws Exception {
    relays.put(FIRST_RELAY, FakeRelay.rejecting(FIRST_RELAY, "blocked: pubkey banned"));

    try (NostrClient nostr = clientWithRelays(FIRST_RELAY)) {
      assertThrows(NoRelayAcceptedException.class, () -> nostr.publishTextNote("rejected"));
    }
  }

  // Verifies a subscription opened through the facade covers every relay in the pool.
  @Test
  void subscribingCoversEveryRelay() throws Exception {
    try (NostrClient nostr = clientWithRelays(FIRST_RELAY, SECOND_RELAY);
        var subscription =
            nostr.subscribe(List.of(EventFilter.builder().kind(1).build()), event -> {})) {

      assertEquals(
          relays.keySet(), subscription.getSubscribedRelays());
    }
  }

  // Verifies a pool the client built is closed with it, so try-with-resources releases the
  // connections the client opened.
  @Test
  void aPoolTheClientBuiltIsClosedWithIt() throws Exception {
    try (NostrClient nostr = clientWithRelays(FIRST_RELAY)) {
      assertEquals(ConnectionState.CONNECTED, relays.get(FIRST_RELAY).getConnectionState());
    }

    assertEquals(ConnectionState.CLOSED, relays.get(FIRST_RELAY).getConnectionState());
  }

  // Verifies a pool supplied by the caller outlives the client, so a container can own a pool
  // shared by several short-lived clients.
  @Test
  void aSuppliedPoolOutlivesTheClient() throws Exception {
    RelayPool callerOwnedPool =
        new RelayPool(
            List.of(FIRST_RELAY),
            relayUri -> relays.computeIfAbsent(relayUri, FakeRelay::accepting));

    try (NostrClient nostr =
        NostrClient.builder().identity(identity).relayPool(callerOwnedPool).build()) {
      nostr.publishTextNote("still mine");
    }

    assertEquals(
        ConnectionState.CONNECTED,
        relays.get(FIRST_RELAY).getConnectionState(),
        "the client closed a pool it did not create");
    callerOwnedPool.close();
    assertEquals(ConnectionState.CLOSED, relays.get(FIRST_RELAY).getConnectionState());
  }

  // Verifies a client cannot be built without an identity, since every operation signs with one.
  @Test
  void aClientCannotBeBuiltWithoutAnIdentity() {
    assertThrows(
        NullPointerException.class, () -> NostrClient.builder().relays(FIRST_RELAY).build());
  }

  // Verifies a client cannot be built without somewhere to send, which would otherwise fail only
  // at the first publish.
  @Test
  void aClientCannotBeBuiltWithoutRelays() {
    assertThrows(
        IllegalStateException.class, () -> NostrClient.builder().identity(identity).build());
  }

  // Verifies the underlying pool is reachable, so an application needing finer control is not
  // walled off by the facade.
  @Test
  void theUnderlyingPoolIsReachable() throws Exception {
    try (NostrClient nostr = clientWithRelays(FIRST_RELAY)) {
      assertTrue(nostr.getRelayPool().getRelays().contains(FIRST_RELAY));
      assertEquals(identity, nostr.getIdentity());
    }
  }

  private GenericEvent firstEventSentTo(String relayUri) {
    return ((EventMessage) relays.get(relayUri).getSentMessages().getFirst()).getEvent();
  }

  private NostrClient clientWithRelays(String... relayUris) {
    return NostrClient.builder()
        .identity(identity)
        .relays(relayUris)
        .connectionFactory(relayUri -> relays.computeIfAbsent(relayUri, FakeRelay::accepting))
        .build();
  }
}
