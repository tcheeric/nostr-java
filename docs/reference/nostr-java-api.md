# Nostr Java API Reference

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how-to](../howto/use-nostr-java-api.md) · [Multi-relay publishing](../howto/multi-relay-publishing.md) · [Streaming subscriptions](../howto/streaming-subscriptions.md) · [Custom events](../howto/custom-events.md)

This document provides an overview of the public API exposed by the `nostr-java` modules. It lists the major classes, their key method signatures, and shows brief usage examples.

---

## Identity (`nostr-java-identity`)

### `Identity`
Represents a Nostr identity backed by a private key. Derives the public key and signs `ISignable` objects.

```java
public static Identity create(PrivateKey privateKey)
public static Identity create(String privateKey)
public static Identity generateRandomIdentity()
public PublicKey getPublicKey()
public Signature sign(ISignable signable)
```

**Usage:**
```java
Identity identity = Identity.generateRandomIdentity();
PublicKey pub = identity.getPublicKey();
identity.sign(event);
```

---

## Event Model (`nostr-java-event`)

### `GenericEvent`
The sole event class for all Nostr event kinds. Implements `ISignable`.

```java
// Builder
public static GenericEventBuilder builder()

// Fields
public String getId()
public PublicKey getPubKey()
public Long getCreatedAt()
public int getKind()
public List<GenericTag> getTags()
public String getContent()
public Signature getSignature()

// Kind classification
public boolean isReplaceable()
public boolean isEphemeral()
public boolean isAddressable()

// Bech32 encoding
public String toBech32()
```

**Usage:**
```java
GenericEvent event = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello Nostr!")
    .tags(List.of(GenericTag.of("t", "nostr")))
    .build();

identity.sign(event);
```

### `GenericTag`
The sole tag class. A code and a list of string parameters.

```java
// Factory methods
public static GenericTag of(String code, String... params)
public static GenericTag of(String code, List<String> params)

// Accessors
public String getCode()
public List<String> getParams()
public List<String> toArray()
```

**Usage:**
```java
GenericTag tag = GenericTag.of("e", "eventId123", "wss://relay.example.com", "reply");
tag.getCode()           // "e"
tag.getParams().get(0)  // "eventId123"
tag.toArray()           // ["e", "eventId123", "wss://relay.example.com", "reply"]
```

### `Kinds`
Static `int` constants for common event kinds plus range-check utilities.

```java
public static final int SET_METADATA = 0;
public static final int TEXT_NOTE = 1;
public static final int RECOMMEND_SERVER = 2;
public static final int CONTACT_LIST = 3;
public static final int ENCRYPTED_DIRECT_MESSAGE = 4;
public static final int DELETION = 5;
public static final int REPOST = 6;
public static final int REACTION = 7;
public static final int ZAP_REQUEST = 9734;
public static final int ZAP_RECEIPT = 9735;

public static boolean isValid(int kind)
public static boolean isReplaceable(int kind)
public static boolean isEphemeral(int kind)
public static boolean isAddressable(int kind)
```

### `EventFilter`
Builder-based composable filter for relay REQ messages.

```java
public static EventFilterBuilder builder()

// Builder methods
.kinds(List<Integer> kinds)
.authors(List<String> authors)
.ids(List<String> ids)
.since(long timestamp)
.until(long timestamp)
.limit(int limit)
.addTagFilter(String tagCode, List<String> values)
.build()
```

**Usage:**
```java
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .authors(List.of(pubKeyHex))
    .since(timestamp)
    .limit(100)
    .build();
```

### `Filters`
Container for one or more `EventFilter` instances (OR logic for REQ messages).

```java
public Filters(EventFilter... filters)
public Filters(Filterable... filterables)
```

### Messages

| Class | Command | Purpose |
|-------|---------|---------|
| `EventMessage` | `EVENT` | Send or receive an event |
| `ReqMessage` | `REQ` | Subscribe to events matching filters |
| `CloseMessage` | `CLOSE` | Close a subscription |
| `OkMessage` | `OK` | Relay acknowledgment |
| `EoseMessage` | `EOSE` | End of stored events |
| `NoticeMessage` | `NOTICE` | Relay notice/error |

```java
// Encode a message
String json = new EventMessage(event).encode();

// Decode a message
BaseMessage msg = BaseMessage.read(json);
```

---

## WebSocket Client (`nostr-java-client`)

### `NostrRelayClient`
Spring `TextWebSocketHandler`-based WebSocket client with retry and Virtual Thread support.

**Constructors:**
```java
public NostrRelayClient(String relayUri)
public NostrRelayClient(String relayUri, long awaitTimeoutMs)
```

**Blocking operations:**
```java
public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException
public List<String> send(String json) throws IOException
public AutoCloseable subscribe(String requestJson,
                               Consumer<String> messageListener,
                               Consumer<Throwable> errorListener,
                               Runnable closeListener) throws IOException
public <T extends BaseMessage> AutoCloseable subscribe(T message,
                                                        Consumer<String> messageListener,
                                                        Consumer<Throwable> errorListener,
                                                        Runnable closeListener) throws IOException
public void close() throws IOException
```

**Async operations (Virtual Threads):**
```java
public static CompletableFuture<NostrRelayClient> connectAsync(String relayUri)
public static CompletableFuture<NostrRelayClient> connectAsync(String relayUri, long awaitTimeoutMs)
public CompletableFuture<List<String>> sendAsync(String json)
public <T extends BaseMessage> CompletableFuture<List<String>> sendAsync(T eventMessage)
public CompletableFuture<AutoCloseable> subscribeAsync(String requestJson,
                                                        Consumer<String> messageListener,
                                                        Consumer<Throwable> errorListener,
                                                        Runnable closeListener)
```

**Usage:**
```java
// Blocking
try (NostrRelayClient client = new NostrRelayClient("wss://relay.example.com")) {
    List<String> responses = client.send(new EventMessage(event));
}

// Async
NostrRelayClient.connectAsync("wss://relay.example.com")
    .thenCompose(client -> client.sendAsync(new EventMessage(event)))
    .thenAccept(responses -> System.out.println("Done: " + responses));
```

### `RelayTimeoutException`
Thrown when the relay does not respond within the configured timeout. Extends `IOException`.

```java
public String getRelayUri()
public long getTimeoutMs()
```

### `ConnectionState`
Enum tracking WebSocket connection state.

```java
CONNECTING, CONNECTED, RECONNECTING, CLOSED
```

### Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `nostr.websocket.await-timeout-ms` | `60000` | Max time to await a relay response |
| `nostr.websocket.max-idle-timeout-ms` | `3600000` | Max idle timeout for WebSocket sessions |
| `nostr.websocket.max-text-message-buffer-size` | `1048576` | WebSocket text message buffer size |
| `nostr.websocket.max-binary-message-buffer-size` | `1048576` | WebSocket binary message buffer size |

### Retry behavior

Send and subscribe operations are annotated with `@NostrRetryable`:
- Included exception: `IOException`
- Max attempts: `3`
- Backoff: initial `500ms`, multiplier `2.0`

---

## Client API (`nostr-java-api`)

The entry point for applications. See the
[multi-relay how-to](../howto/multi-relay-publishing.md) for worked examples.

### `NostrClient`
An identity, a set of relays, and the operations between them.

```java
static NostrClient.Builder builder()

PublishResult publish(GenericEvent event) throws NoRelayAcceptedException
PublishResult publishAs(Identity signer, GenericEvent event) throws NoRelayAcceptedException
PublishResult publishTextNote(String content) throws NoRelayAcceptedException

RelaySubscription subscribe(List<EventFilter> filters, SubscriptionListener listener)

RecipientDeliveryOutcome sendDirectMessage(PublicKey recipient, String content)
List<RecipientDeliveryOutcome> sendDirectMessage(List<PublicKey> recipients, String content)
ChatMessage readDirectMessage(GenericEvent giftWrap)

Optional<DirectMessageRelayList> findDirectMessageRelays(PublicKey owner)
RelayPool getRelayPool()
Identity getIdentity()
void close()
```

Builder: `identity(Identity)`, `relays(String...)`, `relays(List<String>)`,
`relayPool(RelayPool)`, `connectionFactory(RelayConnectionFactory)`, `build()`.

**Ownership follows construction.** A pool built from relay URIs is closed with the client; a
pool passed to `relayPool(...)` is left to whoever created it, so it may outlive the client.

### `RecipientDeliveryOutcome`
Whether one participant received a direct message: `recipient()`, `status()`
(`DELIVERED`, `UNREACHABLE`, `REJECTED`), `relays()`, `isDelivered()`, `findReason()`.

A recipient who published no kind-10050 relay list is `UNREACHABLE`, since NIP-17 treats that as
declining private messages.

### `RelayListLookup`
Implements `DirectMessageRelayLookup` by querying relays for kind-10050 lists.

---

## Relay Pool (`nostr-java-client`)

### `RelayPool`
A mutable set of relay connections, with fan-out publishing and fan-in subscriptions.

```java
RelayPool(List<String> relayUris, RelayConnectionFactory connectionFactory)
RelayPool(List<String> relayUris, RelayConnectionFactory factory, Duration publishTimeout)
// further constructors add reconnectInterval and backlogTimeout

PublishResult publish(GenericEvent event) throws NoRelayAcceptedException
RelaySubscription subscribe(List<EventFilter> filters, SubscriptionListener listener)
RelaySubscription subscribe(List<EventFilter> filters, SubscriptionListener listener, int windowSize)

boolean addRelay(String relayUri)
boolean releaseRelay(String relayUri)
boolean removeRelay(String relayUri)
List<String> retryUnreachableRelays()

List<String> getRelays()
List<String> getConnectedRelays()
List<String> getUnreachableRelays()
Optional<ConnectionState> getConnectionState(String relayUri)
void close()
```

Defaults: `DEFAULT_PUBLISH_TIMEOUT` 10s, `DEFAULT_RECONNECT_INTERVAL` 30s,
`DEFAULT_BACKLOG_TIMEOUT` 10s.

### `PublishResult`
What every relay did with one event: `getEventId()`, `getOutcomes()`, `findOutcome(String)`,
`getAcceptingRelays()`, `getFailures()`, `isAccepted()`, `isAcceptedByAllRelays()`.

### `RelayPublishOutcome`
One relay's verdict: `relayUri()`, `status()` (`ACCEPTED`, `REJECTED`, `TIMED_OUT`,
`UNREACHABLE`), `reason()`, `isAccepted()`, `findReason()`.

### `NoRelayAcceptedException`
Thrown when no relay stored the event. Carries the full `PublishResult` via
`getPublishResult()`, so the caller can still see what each relay said.

### `RelaySubscription`
One subscription across many relays: `getSubscriptionId()`, `getSubscribedRelays()`,
`hasAnnouncedEndOfStoredEvents()`, `close()`.

### `SubscriptionListener`
`onEvent(GenericEvent)`, plus optional `onEndOfStoredEvents()` and
`onRelayFailure(String, Throwable)`.

Events are delivered in the order the relay sent them, so stored events always arrive before
the end-of-backlog signal.

### `RelayConnection` / `RelayConnectionFactory`
The seam between relay coordination and transport. `NostrRelayClient` implements
`RelayConnection`; supplying a different factory is how tests substitute scripted relays.

### Known limitations

**One request in flight per relay.** `NostrRelayClient` serves a single request at a time, so
the pool serialises operations per relay. Throughput to any one relay is bounded by round-trip
latency. Fan-out across relays is unaffected. Making the client multiplex is separate work; see
[ADR-0004](../decisions/0004-pool-concurrency-and-subscription-lifecycle.md).

**Transient relays are briefly shared.** A relay added to deliver a direct message is available
to other operations while it remains connected. This is benign, but it means the pool can
publish through a relay the caller never configured.

**De-duplication is windowed.** Events evicted from the bounded window are treated as new if
they arrive again, so a copy arriving much later than its siblings can be delivered twice. The
default window is sized well beyond realistic cross-relay spread.

---

## Encryption (`nostr-java-identity`)

### `MessageCipher`
Strategy interface for message encryption.

```java
String encrypt(String message)
String decrypt(String message)
```

Implementations:
- `MessageCipher04` — NIP-04 direct message encryption (legacy).
- `MessageCipher44` — NIP-44 versioned encryption (recommended).

---

## Cryptography (`nostr-java-core`)

### `Schnorr`
BIP-340 Schnorr signature utility.

```java
static byte[] sign(byte[] msg, byte[] secKey, byte[] auxRand)
static boolean verify(byte[] msg, byte[] pubKey, byte[] sig)
static byte[] generatePrivateKey()
static byte[] genPubKey(byte[] secKey)
```

### `Bech32`
Bech32/Bech32m encoding for NIP-19.

```java
static String toBech32(Bech32Prefix hrp, byte[] hexKey)
static String fromBech32(String str)
```

### `NostrUtil`
General helper functions using `java.util.HexFormat`.

```java
static String bytesToHex(byte[] bytes)
static byte[] hexToBytes(String hex)
static byte[] sha256(byte[] data)
static byte[] createRandomByteArray(int len)
```

---

## Key Types (`nostr-java-event`)

### `PublicKey`
Nostr public key value object with Bech32 encoding (`npub` prefix).

```java
public PublicKey(String hex)
public String toBech32()
public String toString()  // hex representation
```

### `PrivateKey`
Nostr private key value object with Bech32 encoding (`nsec` prefix).

```java
public PrivateKey(String hex)
public String toBech32()
```

### `Signature`
BIP-340 Schnorr signature value object.

```java
public Signature(String hex)
```

---

This reference is a starting point; consult the source for complete details.
