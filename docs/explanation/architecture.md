# Architecture

This document explains the overall architecture of nostr-java and how its modules collaborate to implement the Nostr protocol.

**Purpose:** Provide a high-level mental model for contributors and integrators.
**Audience:** Developers extending or integrating the library.
**Last Updated:** 2026-02-24 (v2.0.0 simplification)

---

## Table of Contents

1. [Design Philosophy](#design-philosophy)
2. [Module Overview](#modules)
3. [Data Flow](#data-flow)
4. [Event Lifecycle](#event-lifecycle)
5. [Core Classes](#core-classes)
6. [Design Patterns](#design-patterns)
7. [Error Handling](#error-handling)
8. [Security](#security-notes)

---

## Design Philosophy

nostr-java 2.0 follows a **minimalist, protocol-aligned** design:

- **One event class** — `GenericEvent` handles every Nostr event kind via `int kind`. No subclasses.
- **One tag class** — `GenericTag` stores a code and `List<String>` params. No subclasses, no `ElementAttribute`.
- **Integer kinds** — `Kinds` utility provides named constants (`Kinds.TEXT_NOTE`, `Kinds.CONTACT_LIST`) and range checks. Any integer is valid — no enum gating.
- **4 modules** — each with a clear, focused purpose and a strict dependency chain.
- **Virtual Threads** — relay I/O and listener dispatch use Java 21 Virtual Threads for lightweight concurrency.

This design reduced the library from ~180 classes across 9 modules to ~40 classes across 4 modules, eliminating all NIP-specific concrete types, entity DTOs, factory hierarchies, and annotation-driven tag registration.

---

## Modules

```
nostr-java-core → nostr-java-event → nostr-java-identity → nostr-java-client
```

### `nostr-java-core`
**Purpose:** Foundation utilities and cryptographic primitives.

**Key classes:**
- `NostrUtil` — SHA-256 hashing, hex encoding via `java.util.HexFormat`, random byte generation
- `Schnorr` — BIP-340 Schnorr signature signing and verification
- `Bech32` / `Bech32Prefix` — Bech32 encoding/decoding (NIP-19)
- `Nip05Validator` — DNS-based identity validation with async/batch support
- `DefaultHttpClientProvider` — HTTP client with shared Virtual Thread executor
- Exception hierarchy: `NostrException`, `NostrCryptoException`, `NostrEncodingException`, `NostrNetworkException`

**Dependencies:** None (foundation layer). External: BouncyCastle, commons-lang3, Jackson.

### `nostr-java-event`
**Purpose:** Event model, tag model, filters, messages, and JSON serialization.

**Key classes:**
- `GenericEvent` — The sole event class. Supports any kind via `int`. Implements `ISignable`.
- `GenericTag` — The sole tag class. `code` + `List<String> params`. Factory: `GenericTag.of("e", "eventId", "relay")`.
- `Kinds` — Static `int` constants for common kinds plus range-check methods (`isReplaceable()`, `isEphemeral()`, `isAddressable()`).
- `EventFilter` — Builder-based composable filter for relay REQ messages.
- `Filters` — Container for multiple `EventFilter` instances (OR logic).
- `PublicKey`, `PrivateKey`, `Signature` — Value objects with Bech32 encoding.
- `ISignable` — Signing contract implemented by `GenericEvent`.
- `BaseMessage` and subclasses — `EventMessage`, `ReqMessage`, `CloseMessage`, `OkMessage`, `EoseMessage`, `NoticeMessage`.
- Serialization: `GenericEventSerializer`, `GenericEventDeserializer`, `GenericTagSerializer`, `TagDeserializer`, `EventSerializer` (canonical NIP-01), `EventJsonMapper`.

**Dependencies:** `nostr-java-core`.

### `nostr-java-identity`
**Purpose:** Identity management, signing, and message encryption.

**Key classes:**
- `Identity` — Key pair management, event signing via `identity.sign(event)`.
- `MessageCipher04` — NIP-04 encrypted direct messages (legacy).
- `MessageCipher44` — NIP-44 versioned encryption (recommended).

**Dependencies:** `nostr-java-event`.

### `nostr-java-client`
**Purpose:** WebSocket relay communication with retry, Virtual Threads, and async support.

**Key classes:**
- `NostrRelayClient` — Spring `TextWebSocketHandler`-based WebSocket client. Blocking `send()`, non-blocking `subscribe()`, async `connectAsync()`/`sendAsync()`/`subscribeAsync()`.
- `RelayTimeoutException` — Typed exception for relay timeouts (replaces silent empty-list returns).
- `ConnectionState` — Enum: `CONNECTING`, `CONNECTED`, `RECONNECTING`, `CLOSED`.
- `NostrRetryable` / `RetryConfig` — Spring Retry annotation and configuration.

**Dependencies:** `nostr-java-identity`, Spring WebSocket, Spring Retry.

---

## Data Flow

```
Application
    │
    ▼
GenericEvent.builder()          ← build event with kind, content, tags
    │
    ▼
Identity.sign(event)            ← compute ID (SHA-256) + Schnorr signature
    │
    ▼
NostrRelayClient.send(          ← send over WebSocket
    new EventMessage(event))
    │
    ▼
Relay                           ← receives ["EVENT", {...}]
```

1. The application builds a `GenericEvent` using the builder or constructor.
2. `Identity.sign()` computes the canonical NIP-01 JSON, hashes it (SHA-256), and signs with BIP-340 Schnorr.
3. The signed event is wrapped in an `EventMessage` and sent to relays via `NostrRelayClient`.
4. Relay responses (OK, EOSE, NOTICE, EVENT) are parsed and returned or dispatched to listeners.

## Event Lifecycle

```
Build → Sign → Send → Receive response
```

```java
// 1. Build
GenericEvent event = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello Nostr!")
    .tags(List.of(GenericTag.of("t", "nostr")))
    .build();

// 2. Sign
identity.sign(event);

// 3. Send
try (NostrRelayClient client = new NostrRelayClient("wss://relay.example.com")) {
    List<String> responses = client.send(new EventMessage(event));
}
```

---

## Core Classes

### GenericEvent

The sole event class. All Nostr events are represented as `GenericEvent` regardless of kind.

```java
@Data
public class GenericEvent implements ISignable {
    private String id;
    private PublicKey pubKey;
    private Long createdAt;
    private int kind;
    private List<GenericTag> tags;
    private String content;
    private Signature signature;
}
```

Key methods:
- `GenericEvent.builder()` — fluent builder
- `isReplaceable()`, `isEphemeral()`, `isAddressable()` — kind range checks
- `toBech32()` — NIP-19 encoding

### GenericTag

The sole tag class. A tag is a code and a list of string parameters — exactly what the Nostr protocol specifies.

```java
GenericTag.of("e", "eventId123", "wss://relay.example.com", "reply")
// Serializes to: ["e", "eventId123", "wss://relay.example.com", "reply"]

tag.getCode()       // "e"
tag.getParams()     // ["eventId123", "wss://relay.example.com", "reply"]
tag.toArray()       // ["e", "eventId123", "wss://relay.example.com", "reply"]
```

### Kinds

Static `int` constants for common event kinds. Users can use any integer — these are convenience constants, not a gating mechanism.

```java
Kinds.TEXT_NOTE          // 1
Kinds.SET_METADATA       // 0
Kinds.CONTACT_LIST       // 3
Kinds.ENCRYPTED_DIRECT_MESSAGE  // 4

Kinds.isReplaceable(10002)  // true (10000-19999)
Kinds.isEphemeral(20001)    // true (20000-29999)
Kinds.isAddressable(30023)  // true (30000-39999)
```

### EventFilter

Builder-based filter for relay subscriptions:

```java
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE, Kinds.REACTION))
    .authors(List.of("pubkey_hex"))
    .since(timestamp)
    .until(timestamp)
    .addTagFilter("t", List.of("nostr"))
    .limit(100)
    .build();
```

---

## Design Patterns

### Builder Pattern
`GenericEvent.builder()` and `EventFilter.builder()` provide fluent, readable construction of complex objects.

### Value Object Pattern
`PublicKey`, `PrivateKey`, `Signature`, `SubscriptionId` — immutable objects that compare by value, encapsulate validation, and provide Bech32 encoding.

### Delegation Pattern
`GenericEvent` delegates to `EventValidator` for validation, `EventSerializer` for canonical serialization, and `Kinds` for kind range classification.

### Factory Method Pattern
`GenericTag.of(code, params...)` provides a concise way to create tags.

---

## Error Handling

### Exception Hierarchy

```
NostrRuntimeException (base)
├── NostrProtocolException (NIP violations)
├── NostrCryptoException (signing, encryption)
│   ├── SigningException
│   └── SchnorrException
├── NostrEncodingException (serialization)
│   ├── KeyEncodingException
│   ├── EventEncodingException
│   └── Bech32EncodingException
└── NostrNetworkException (relay communication)
    └── RelayTimeoutException (timeout waiting for relay)
```

### Principles

1. **Validate early** — constructors and builders validate input
2. **Fail fast** — `HexFormat.parseHex()` throws on invalid hex; `RelayTimeoutException` replaces silent empty returns
3. **Use domain exceptions** — specific exceptions with context, not generic `RuntimeException`

---

## Security Notes

### Key Management
- Private keys never leave the process — signing is in-memory only
- Uses `SecureRandom` with BouncyCastle for key generation
- Never reuse nonces or IVs

### Signing
- BIP-340 Schnorr signatures on secp256k1
- Deterministic (RFC 6979) — same message produces same signature
- Verifiable by public key

### Encryption
- **NIP-04** (legacy) — AES-256-CBC. Use NIP-44 for new applications.
- **NIP-44** (recommended) — HKDF key derivation, ChaCha20-Poly1305 AEAD.

---

## Summary

nostr-java 2.0 provides:

- **Minimal API surface** — one event class, one tag class, ~40 total classes across 4 modules
- **Protocol-aligned design** — kinds are integers, tags are string arrays, no library-imposed type hierarchy
- **Virtual Thread concurrency** — relay I/O and listener dispatch on lightweight threads
- **Reliable connectivity** — typed timeout exceptions, connection state tracking, Spring Retry
- **Strong cryptography** — BIP-340 Schnorr, NIP-44 AEAD, BouncyCastle provider
