# Spec: `nostr-java-api` capability layer

- **Status**: Ready for agent
- **Date**: 2026-08-30
- **Decisions**: [ADR-0001](../../docs/decisions/0001-introduce-nostr-java-api-module.md) …
  [ADR-0005](../../docs/decisions/0005-pool-membership-eose-and-ownership.md)
- **Vocabulary**: [docs/CONTEXT.md](../../docs/CONTEXT.md)

## Problem Statement

A developer using `nostr-java` today can build, sign, and send an event, but only to **one
relay at a time**. Nostr is a multi-relay protocol, so that leaves them writing the same
infrastructure every time: connect to several relays, publish to all of them, work out whether
enough of them accepted, merge inbound events from all of them, throw away the duplicates, and
notice when one quietly drops off.

Three concrete gaps follow:

- Publishing to several relays, and understanding a **partial** result, is entirely manual.
- Subscribing across several relays means de-duplicating by event id by hand, and a relay that
  disconnects mid-stream silently stops contributing.
- NIP-17 direct messages can be *planned* (`Nip17DirectMessageService.planDelivery`) but not
  *sent*, because delivery targets the recipient's relays and nothing resolves or connects to
  them. `DirectMessageRelayLookup` has no implementation at all.

## Solution

A new `nostr-java-api` module exposing a `NostrClient` facade, built on a new `RelayPool` in
`nostr-java-client`. The developer names their relays and their identity once, then works in
terms of intent:

```java
try (NostrClient nostr = NostrClient.builder()
        .identity(identity)
        .relays("wss://relay.398ja.xyz", "wss://nos.lol")
        .build()) {

    PublishResult result = nostr.publish().textNote("Hello Nostr!");
    nostr.directMessages().send(recipient, "hi");
    nostr.subscriptions().subscribe(filter, event -> render(event));
}
```

Partial failure is visible in `PublishResult` rather than hidden. Duplicates are removed. A
dropped relay is reported and re-subscribed on reconnect. Direct messages are delivered to the
recipient's own relays, discovered from their kind 10050 relay list.

The facade is a **capability layer**, not a wrapper: callers may still build `GenericEvent`
directly and drop to `RelayPool` or `NostrRelayClient` when they want to.

## User Stories

1. As an application developer, I want to configure several relays once, so that I do not
   repeat connection setup at every call site.
2. As an application developer, I want to publish an event to all my relays in one call, so
   that I do not write fan-out logic myself.
3. As an application developer, I want the facade to sign events with my configured identity,
   so that build-sign-publish is a single step.
4. As a bot author running several accounts, I want to override the identity per call, so that
   one client can act for many keys.
5. As an application developer, I want to see per-relay publish outcomes, so that I can tell
   which relays accepted my event and which rejected it.
6. As an application developer, I want a relay's rejection reason surfaced verbatim, so that I
   can act on `blocked:`, `rate-limited:`, and `invalid:` differently.
7. As an application developer, I want an exception when no relay at all accepted my event, so
   that total failure cannot pass silently for success.
8. As an application developer, I want partial success to be an ordinary return value, so that
   the common case of one flaky relay is not an exception.
9. As an application developer, I want publishing to be bounded by a timeout, so that one
   unresponsive relay cannot hang my call indefinitely.
10. As an application developer, I want relays that miss the timeout recorded as timed out, so
    that I can distinguish slowness from rejection.
11. As an application developer, I want my client to start even when some relays are
    unreachable, so that one dead relay cannot stop my application booting.
12. As an operator, I want per-relay connection state observable, so that I can monitor which
    relays are actually carrying traffic.
13. As an application developer, I want unreachable relays retried in the background, so that
    they rejoin the pool without a restart.
14. As an application developer, I want one subscription to cover all my relays, so that I do
    not manage a subscription per relay.
15. As an application developer, I want the same event arriving from several relays delivered
    to me once, so that my UI does not show duplicates.
16. As an application developer running a long-lived firehose, I want de-duplication memory to
    stay bounded, so that my process does not leak over days of uptime.
17. As an application developer, I want subscription callbacks to receive parsed events, so
    that I am not decoding JSON myself.
18. As an application developer, I want one signal when the stored-event backlog is drained
    across all relays, so that I can hide a loading spinner at the right moment.
19. As an application developer, I want that signal to arrive even when a relay never responds,
    so that one dead relay cannot leave my UI loading forever.
20. As an application developer, I want to be notified when a relay drops mid-subscription, so
    that silent degradation from five relays to one is visible.
21. As an application developer, I want dropped relays automatically re-subscribed on
    reconnect, so that my long-lived stream repairs itself.
22. As an application developer, I want to close a subscription and stop receiving events from
    every relay at once, so that cleanup is a single call.
23. As an application developer, I want malformed relay payloads reported without killing my
    subscription, so that one bad relay cannot end my stream.
24. As a messaging application developer, I want to send a NIP-17 direct message in one call,
    so that I do not orchestrate rumor, seal, gift wrap, and delivery myself.
25. As a messaging application developer, I want the message delivered to the recipient's own
    relays, so that they actually receive it.
26. As a messaging application developer, I want the recipient's relay list resolved from their
    kind 10050 event, so that I do not maintain that mapping.
27. As a messaging application developer, I want a recipient with no published relay list
    reported as unreachable rather than silently skipped, so that I can tell the user.
28. As a messaging application developer, I want per-recipient delivery outcomes for a group
    message, so that I know who received it.
29. As a messaging application developer, I want relays connected only for a delivery to be
    released afterwards, so that connections do not accumulate.
30. As a messaging application developer, I want to read an incoming gift wrap back into a
    chat message through the same facade, so that send and receive are symmetrical.
31. As an application developer, I want to add and remove relays at runtime, so that my relay
    set can follow user preferences without a restart.
32. As an application developer, I want `NostrClient` to close the pool it created, so that
    try-with-resources cleans everything up.
33. As an application developer embedding the SDK in a container, I want to pass my own pool
    and keep ownership of it, so that its lifecycle can outlive the facade.
34. As an application developer, I want to construct the client in a plain `main()` method, so
    that I am not forced to run a Spring application context.
35. As an application developer, I want publish and read calls to be synchronous by default,
    so that ordinary code reads top to bottom.
36. As an application developer, I want the facade to return core types like `GenericEvent`,
    so that I am not converting between a facade model and the SDK model.
37. As a contributor, I want relay behaviour fakeable in tests, so that I can test failure
    scenarios without Docker or a live relay.
38. As a contributor, I want the pool tested against scripted relay failures, so that partial
    failure semantics are verified rather than assumed.

## Implementation Decisions

### Modules

- **`nostr-java-client`** gains the relay pool and the connection seam. The pool is a transport
  concern (ADR-0001), so it must be usable without the facade.
- **`nostr-java-api`** is new, depends on `nostr-java-client`, and nothing depends on it. Build
  order becomes `core → event → identity → client → api`.
- **`nostr-java-identity`** is unchanged. It stays transport-free; `api` executes its plans.

### The connection seam

A `RelayConnection` interface is extracted in `nostr-java-client`, exposing only what a pool
needs: connect, send, subscribe, connection state, close. It deliberately does **not** mirror
all of `NostrRelayClient`'s public methods. `NostrRelayClient` becomes its production
implementation, and a `RelayConnectionFactory` maps a relay URI to a connection so the pool can
be given fakes in tests.

This is the single seam for the whole feature. It is the highest point at which real I/O
begins, so everything above it is pure logic, and it inverts the Spring dependency: `api`
depends on the interface, never on Spring types.

### `RelayPool`

- **Mutable membership** (ADR-0005): relays can be added and removed at runtime. Connections
  opened transiently for a direct message are released by reference counting or idle eviction.
- **Best-effort construction** (ADR-0002): unreachable relays are marked down and retried
  lazily; construction never fails because a member is unreachable.
- **Serialized per relay** (ADR-0004): `NostrRelayClient` permits one request in flight per
  connection, so operations to a single relay queue behind a lock. Fan-out across relays stays
  concurrent, on Virtual Threads.
- Owns per-relay `ConnectionState`, reconnection policy, and subscription replay.

### Publishing

`publish` fans out, waits for each relay's `OK` up to a pool-level timeout, and returns a
`PublishResult`: for each relay, accepted, rejected with the relay's verbatim reason, or timed
out. It **throws when zero relays accepted** (ADR-0002). There is no quorum setting and no
all-or-nothing mode; callers wanting stricter guarantees inspect the result.

### Subscriptions

- Fan-in across relays, de-duplicated by event `id` through a **bounded LRU window**, size
  configurable, defaulting to the low thousands (ADR-0002).
- Callbacks receive **`GenericEvent`**, since the pool must parse payloads to de-duplicate
  anyway (ADR-0004).
- Per-relay `EOSE` frames are aggregated into **one synthetic EOSE**, emitted when every
  participating relay has reported or the timeout expires (ADR-0005).
- A relay dropping mid-stream triggers an error notification and, on reconnect, automatic
  re-subscription from the stored filter. Subscriptions are therefore stateful objects holding
  their filter, not fire-and-forget handles.
- Closing the subscription handle unsubscribes from every relay.

### Direct messages

`api` orchestrates: it calls `Nip17DirectMessageService.planDelivery`, resolves each
recipient's relays, adds those relays to the pool, publishes each gift wrap to its recipient's
relays, and returns per-recipient outcomes. `MessageDelivery.unreachable` recipients are
reported, never silently dropped.

`DirectMessageRelayLookup` gets its first real implementation here, fetching kind 10050 events
through the pool. It is injected into `identity`'s interface, so the dependency arrow keeps
pointing from `api` to `identity`.

### Facade

`NostrClient` is built by a builder taking an identity and either relay URIs or a pool. It
exposes `publish()`, `subscriptions()`, `directMessages()`, and the relay-list lookup, each an
interface (DIP). Identity is set at build time with a per-call override.

**Ownership follows construction** (ADR-0005): a pool built from URIs is closed by
`NostrClient`; a pool passed in is not. This must be documented on the builder method itself.

Construction must work with no Spring application context. A Spring Boot starter is explicitly
a later, separate decision.

### API scope

v1 ships `publish`, `subscriptions`, `directMessages`, and the relay-list lookup. General
profile handling (kind 0 metadata, kind 3 contacts, kind 10002) is deferred as convenience
rather than capability (ADR-0003). Any proposed method that only reorders calls the caller
could already make is out.

## Testing Decisions

**What makes a good test here.** Tests assert what a caller observes: the contents of a
`PublishResult`, which events reach a subscription callback, in what order, and how many times,
whether an exception is thrown. They must not assert on lock acquisition, thread counts, or
which internal method ran. The existing client tests reach into a package-private constructor
with a mocked `WebSocketSession`; the new seam replaces that with a fake, so new tests need no
Mockito at all.

**The fake.** A `FakeRelay` implementing `RelayConnection`, scriptable per scenario: accept,
reject with a reason string, never respond, drop mid-stream, emit a given event sequence, delay
or withhold `EOSE`. This is the only test double in the feature.

**Modules and levels.**

- `RelayPool` unit tests against fakes, covering: 3-accept/1-reject/1-timeout fan-out; zero
  acceptances throwing; construction with an unreachable member; runtime add and remove;
  per-relay serialization under concurrent publishes; reference-counted release.
- Subscription tests against fakes: duplicate event from four relays delivered once; LRU window
  eviction bounded; synthetic EOSE after all relays report; synthetic EOSE despite one silent
  relay; drop mid-stream notifies and re-subscribes on reconnect; malformed payload reported
  without ending the stream; closing the handle stops all relays.
- Direct message tests: recipient with a relay list is delivered to those relays; recipient
  without one is reported unreachable; group message yields per-recipient outcomes;
  transiently added relays released afterwards.
- `NostrClient` tests: identity signing and per-call override; ownership, a built pool is
  closed and a supplied pool is not; construction with no Spring context.
- One integration test against a live or containerised relay for the publish-then-read round
  trip, following the repo's existing Docker/no-docker profile split.

**Prior art.** `NostrRelayClientSubscriptionTest` and `NostrRelayClientConcurrencyTest` for
latch-based async assertions; `Nip17DirectMessageServiceTest` and `MessageDeliveryPlanTest` for
the direct-message domain; `EntityFactory` in `nostr-java-identity` for test data construction.
Every test method carries a plain-English comment above it, per repo convention.

## Out of Scope

- **Multiplexing `NostrRelayClient`.** The one-request-in-flight ceiling stands; per-relay
  throughput remains latency-bound. Tracked as separate follow-up work.
- **A Spring Boot starter.** Plain-Java construction only for now.
- **General profile services**: kind 0 metadata, kind 3 contacts, kind 10002 relay lists beyond
  the direct-message case.
- **Event persistence or caching** beyond the bounded de-duplication window. No local store.
- **NIP-42 relay authentication**, **NIP-65 outbox routing**, and **negentropy sync**.
- **Rewriting existing client tests** onto the new seam. Worthwhile, but not a blocker.
- **Async variants** of every facade method. Sync-first; async is added only where fan-out
  blocking proves material.

## Further Notes

Two accepted consequences are worth restating, because they will look like bugs later:

- A relay added transiently to deliver a direct message is briefly usable by other operations
  while it remains connected. This is benign sharing, not a leak, but it is real.
- Events evicted from the de-duplication window can be re-delivered. The default window size
  must comfortably exceed realistic cross-relay arrival spread.

Delivery order is dependency order: `RelayConnection` seam, then `RelayPool`, then publishing,
then subscriptions, then relay-list lookup, then direct messages, then the facade, then docs.
The facade is last because it is only an assembly of parts already proven.
