# Publish and subscribe across many relays

Nostr is a multi-relay protocol: an event is worth publishing to several relays, and worth
reading from several at once. This guide shows how to do both with `NostrClient`, and how to
send a private direct message.

## Add the module

```xml
<dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-api</artifactId>
    <version>2.2.0</version>
</dependency>
```

## Create a client

Name your identity and your relays once:

```java
Identity identity = Identity.generateRandomIdentity();

try (NostrClient nostr = NostrClient.builder()
        .identity(identity)
        .relays("wss://relay.398ja.xyz", "wss://nos.lol")
        .build()) {
    // ...
}
```

The client connects to what it can. A relay that is unreachable is marked down and retried in
the background, so one dead relay never stops your application starting.

## Publish a note

```java
PublishResult result = nostr.publishTextNote("Hello Nostr!");
```

The event is signed with your identity and sent to every relay at once.

### Read the result

Relays disagree, so publishing has no single verdict:

```java
result.getAcceptingRelays();   // relays that stored the event
result.isAcceptedByAllRelays(); // true only when every relay accepted

for (RelayPublishOutcome failure : result.getFailures()) {
    System.out.println(failure.relayUri() + ": " + failure.status()
        + failure.findReason().map(reason -> " (" + reason + ")").orElse(""));
}
```

A relay may reject with a reason such as `blocked: pubkey banned`, time out, or be unreachable.
All three are ordinary data.

**Total failure throws.** If no relay stored the event, `publish` raises
`NoRelayAcceptedException` rather than returning, so an event that reached nobody cannot be
mistaken for a published one. The exception carries the same `PublishResult`:

```java
try {
    nostr.publishTextNote("Hello Nostr!");
} catch (NoRelayAcceptedException e) {
    e.getPublishResult().getFailures().forEach(System.out::println);
}
```

## Subscribe across every relay

```java
try (RelaySubscription subscription = nostr.subscribe(
        List.of(EventFilter.builder().kind(1).build()),
        event -> System.out.println(event.getContent()))) {
    // events arrive until the subscription is closed
}
```

Each event is delivered **once**, however many relays hold it, and arrives already parsed as a
`GenericEvent`. Closing the subscription stops delivery from every relay.

### React to the backlog and to failures

A relay answers a subscription with its stored events, then signals that its backlog is drained,
then streams live ones. Implement `SubscriptionListener` to see all three:

```java
nostr.subscribe(filters, new SubscriptionListener() {
    @Override
    public void onEvent(GenericEvent event) {
        render(event);
    }

    @Override
    public void onEndOfStoredEvents() {
        hideLoadingSpinner();   // fires once, after every relay has replayed
    }

    @Override
    public void onRelayFailure(String relayUri, Throwable failure) {
        log.warn("Relay {} dropped: {}", relayUri, failure.getMessage());
    }
});
```

`onEndOfStoredEvents` fires exactly once, when every relay has replayed or a timeout expires, so
one unresponsive relay cannot leave your interface loading forever. Stored events always arrive
before it.

A relay that drops mid-stream is reported and re-subscribed automatically when it reconnects, so
a long-lived subscription repairs itself instead of quietly shrinking.

## Send a private direct message

```java
RecipientDeliveryOutcome outcome = nostr.sendDirectMessage(recipientKey, "dinner at eight");

if (!outcome.isDelivered()) {
    System.out.println("not delivered: " + outcome.findReason().orElse("unknown"));
}
```

The message is gift-wrapped per NIP-17 and delivered to the **recipient's** relays, discovered
from their published kind-10050 list. Relays connected only for the delivery are released
afterwards.

A recipient who has published no relay list is reported `UNREACHABLE` rather than silently
skipped: NIP-17 treats that as declining private messages.

For several recipients, each is reported separately, because a group message can partly succeed:

```java
List<RecipientDeliveryOutcome> outcomes =
    nostr.sendDirectMessage(List.of(alice, bob), "dinner at eight");
```

Every conversation includes the sender, since NIP-17 requires a copy addressed to them so they
can read their own sent messages.

### Read incoming messages

```java
nostr.subscribe(
    List.of(EventFilter.builder().kind(1059).build()),
    wrap -> System.out.println(nostr.readDirectMessage(wrap).getContent()));
```

Only wraps addressed to your identity can be opened; attempting to read another's will fail,
which is the point of the scheme.

## Reach further down

The facade is not a wall. Events are ordinary `GenericEvent` values, and the relay pool is
available for anything the client does not cover:

```java
RelayPool pool = nostr.getRelayPool();
pool.addRelay("wss://relay.temporary");
pool.getConnectionState("wss://nos.lol");
```

## Know the edges

Three behaviours are deliberate, and each will look like a bug if you meet it unprepared.

**Throughput to one relay is bounded.** A relay connection serves one request at a time, so the
pool queues operations per relay. Publishing from ten threads to the same relay is as fast as
that relay's round-trip latency allows, no faster. Fan-out across different relays is fully
concurrent, so adding relays scales; hammering one does not.

**A relay borrowed for a direct message is briefly shared.** Delivering to someone connects to
*their* relays, and while connected those relays take part in other operations too. They are
released once the delivery finishes.

**De-duplication forgets.** The window that suppresses duplicates is bounded, so an event whose
copies arrive far apart can reach you twice. The default is sized well beyond any realistic
spread between relays; raise it with the three-argument `subscribe` if your workload needs to.

## Related

- [Private direct messages](private-direct-messages.md) — composing NIP-17 messages directly
- [Streaming subscriptions](streaming-subscriptions.md) — single-relay subscriptions
- [Architecture](../explanation/architecture.md) — how the modules fit together
