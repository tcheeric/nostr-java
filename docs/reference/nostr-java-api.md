# Nostr Java API Reference

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how‑to](../howto/use-nostr-java-api.md) · [Streaming subscriptions](../howto/streaming-subscriptions.md) · [Custom events](../howto/custom-events.md)

This document provides an overview of the public API exposed by the `nostr-java` modules. It lists the major classes, configuration objects and their key method signatures, and shows brief examples of how to use them. Where applicable, links to related [Nostr Improvement Proposals (NIPs)](https://github.com/nostr-protocol/nips) are provided.

## Identity (`nostr-java-id`)

### `Identity`
Represents a Nostr identity backed by a private key. It can derive a public key and sign `ISignable` objects.

```java
public static Identity create(PrivateKey privateKey)
public static Identity create(String privateKey)
public static Identity generateRandomIdentity()
public PublicKey getPublicKey()
public Signature sign(ISignable signable)
```

**Usage:**
```java
Identity id = Identity.generateRandomIdentity();
PublicKey pub = id.getPublicKey();
Signature sig = id.sign(event);
```

## Event Model (`nostr-java-event`)

### Core Types
- `BaseMessage` – base class for all relay messages.
- `BaseEvent` – root class for Nostr events.
- `BaseTag` – helper for tag encoding and decoding.

### Predefined Events
The `nostr.event` package provides event implementations for many NIPs:

| Class | NIP |
|-------|-----|
| `NIP01Event` | [NIP-01](https://github.com/nostr-protocol/nips/blob/master/01.md) – standard text notes. |
| `NIP04Event` | [NIP-04](https://github.com/nostr-protocol/nips/blob/master/04.md) – encrypted direct messages. |
| `NIP05Event` | [NIP-05](https://github.com/nostr-protocol/nips/blob/master/05.md) – DNS identifiers. |
| `NIP09Event` | [NIP-09](https://github.com/nostr-protocol/nips/blob/master/09.md) – event deletion. |
| `NIP25Event` | [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md) – reactions. |
| `NIP52Event` | [NIP-52](https://github.com/nostr-protocol/nips/blob/master/52.md) – calendar events. |
| `NIP99Event` | [NIP-99](https://github.com/nostr-protocol/nips/blob/master/99.md) – classified listings. |

### Filters
`Filters` and related `Filterable` implementations help build subscription requests.

```java
new Filters(Filterable... filterables)
List<Filterable> getFilterByType(String type)
void setLimit(Integer limit)
```

**Usage:**
```java
Filters filters = new Filters(new AuthorFilter(pubKey));
filters.setLimit(100);
```

## WebSocket Clients (`nostr-java-client`, `nostr-java-api`)

### `WebSocketClientIF`
Abstraction over a WebSocket connection to a relay.

```java
<T extends BaseMessage> List<String> send(T eventMessage) throws IOException
List<String> send(String json) throws IOException
AutoCloseable subscribe(String requestJson,
                        Consumer<String> messageListener,
                        Consumer<Throwable> errorListener,
                        Runnable closeListener) throws IOException
<T extends BaseMessage> AutoCloseable subscribe(T eventMessage,
                                                Consumer<String> messageListener,
                                                Consumer<Throwable> errorListener,
                                                Runnable closeListener) throws IOException
void close() throws IOException
```

### `StandardWebSocketClient`
Spring `TextWebSocketHandler` based implementation of `WebSocketClientIF`.

```java
public StandardWebSocketClient(String relayUri)
public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException
public List<String> send(String json) throws IOException
public AutoCloseable subscribe(String requestJson,
                               Consumer<String> messageListener,
                               Consumer<Throwable> errorListener,
                               Runnable closeListener) throws IOException
public void close() throws IOException
```

### `SpringWebSocketClient`
Wrapper that adds retry logic around a `WebSocketClientIF`.

```java
public List<String> send(BaseMessage eventMessage) throws IOException
public List<String> send(String json) throws IOException
public AutoCloseable subscribe(BaseMessage requestMessage,
                               Consumer<String> messageListener,
                               Consumer<Throwable> errorListener,
                               Runnable closeListener) throws IOException
public AutoCloseable subscribe(String json,
                               Consumer<String> messageListener,
                               Consumer<Throwable> errorListener,
                               Runnable closeListener) throws IOException
public List<String> recover(IOException ex, String json) throws IOException
public void close() throws IOException
```

### `NostrSpringWebSocketClient`
High level client coordinating multiple relay connections and signing.

```java
public NostrIF setRelays(Map<String,String> relays)
public List<String> sendEvent(IEvent event)
public List<String> sendRequest(List<Filters> filters, String subscriptionId)
public AutoCloseable subscribe(Filters filters, String subscriptionId, Consumer<String> listener)
public AutoCloseable subscribe(Filters filters,
                               String subscriptionId,
                               Consumer<String> listener,
                               Consumer<Throwable> errorListener)
public NostrIF sign(Identity identity, ISignable signable)
public boolean verify(GenericEvent event)
public Map<String,String> getRelays()
public void close()
```

`subscribe` opens a dedicated WebSocket per relay, returns immediately, and streams raw relay
messages to the provided listener. The returned `AutoCloseable` sends a `CLOSE` command and releases
resources when invoked. Because callbacks execute on the WebSocket thread, delegate heavy
processing to another executor to avoid stalling inbound traffic.

- How‑to guide: [../howto/streaming-subscriptions.md](../howto/streaming-subscriptions.md)
- Example: [../../nostr-java-examples/src/main/java/nostr/examples/SpringSubscriptionExample.java](../../nostr-java-examples/src/main/java/nostr/examples/SpringSubscriptionExample.java)

### Configuration
- `RetryConfig` – enables Spring Retry support.
- `RelaysProperties` – maps relay names to URLs via configuration properties.
- `RelayConfig` – loads `relays.properties` and exposes a `Map<String,String>` bean.

## Encryption and Cryptography

### `MessageCipher`
Strategy interface for message encryption.

```java
String encrypt(String message)
String decrypt(String message)
```

Implementations:
- `MessageCipher04` – NIP-04 direct message encryption.
- `MessageCipher44` – NIP-44 payload encryption.

### `Schnorr`
Utility for Schnorr signatures (BIP-340).

```java
static byte[] sign(byte[] msg, byte[] secKey, byte[] auxRand)
static boolean verify(byte[] msg, byte[] pubKey, byte[] sig)
static byte[] generatePrivateKey()
static byte[] genPubKey(byte[] secKey)
```

### `Bech32`
Utility for Bech32/Bech32m encoding used by [NIP-19](https://github.com/nostr-protocol/nips/blob/master/19.md).

```java
static String toBech32(Bech32Prefix hrp, byte[] hexKey)
static String fromBech32(String str)
```

## Utilities (`nostr-java-util`)

### `NostrUtil`
General helper functions.

```java
static String bytesToHex(byte[] bytes)
static byte[] hexToBytes(String hex)
static byte[] sha256(byte[] data)
static byte[] createRandomByteArray(int len)
```

### `NostrException`
Base checked exception for utility methods.

## Examples

### Send a Text Note (NIP-01)
```java
Identity id = Identity.generateRandomIdentity();
NIP01 nip01 = new NIP01(id).createTextNoteEvent("Hello Nostr");
NostrIF client = NostrSpringWebSocketClient.getInstance(id)
        .setRelays(Map.of("damus","wss://relay.damus.io"));
client.sendEvent(nip01.getEvent());
```

### Encrypted Direct Message (NIP-04)
```java
Identity alice = Identity.generateRandomIdentity();
Identity bob = Identity.generateRandomIdentity();
NIP04 dm = new NIP04(alice, bob.getPublicKey())
        .createDirectMessageEvent("secret");
String plaintext = NIP04.decrypt(bob, dm.getEvent());
```

### Subscription with Filters
```java
Filters filters = new Filters(new AuthorFilter(pubKey));
NostrIF client = NostrSpringWebSocketClient.getInstance(id);
List<String> events = client.sendRequest(filters, "sub-id");
```

---
This reference is a starting point; consult the source for complete details and additional NIP helpers.
