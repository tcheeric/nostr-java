# Nostr Java API Reference

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how-to](../howto/use-nostr-java-api.md) · [Streaming subscriptions](../howto/streaming-subscriptions.md) · [Custom events](../howto/custom-events.md)

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
