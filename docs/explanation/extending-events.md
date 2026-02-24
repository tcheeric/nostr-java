# Working with Events and Tags

Navigation: [Docs index](../README.md) · [API how-to](../howto/use-nostr-java-api.md) · [Custom events](../howto/custom-events.md) · [API reference](../reference/nostr-java-api.md)

This guide explains how to create Nostr events and tags using nostr-java 2.0. The library uses a single event class (`GenericEvent`) and a single tag class (`GenericTag`) for all event kinds — no subclasses, no factories, no registries.

---

## Core Concepts

### One event class: `GenericEvent`

Every Nostr event is a `GenericEvent`, differentiated by its `int kind`:

```java
// Text note (kind 1)
GenericEvent note = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello Nostr!")
    .build();

// Metadata (kind 0)
GenericEvent metadata = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.SET_METADATA)
    .content("{\"name\":\"Alice\",\"about\":\"Nostr user\"}")
    .build();

// Any custom kind
GenericEvent custom = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(30078)  // any integer
    .content("custom content")
    .build();
```

### One tag class: `GenericTag`

Tags are a code string and a list of string parameters — exactly what the Nostr protocol specifies:

```java
// Event reference: ["e", "eventId", "relay", "marker"]
GenericTag.of("e", "abc123", "wss://relay.example.com", "reply")

// Public key reference: ["p", "pubkey"]
GenericTag.of("p", "deadbeef1234...")

// Hashtag: ["t", "nostr"]
GenericTag.of("t", "nostr")

// Any custom tag
GenericTag.of("custom", "value1", "value2")
```

Access tag data positionally:
```java
GenericTag tag = GenericTag.of("e", "abc123", "wss://relay.example.com", "reply");
tag.getCode()           // "e"
tag.getParams()         // ["abc123", "wss://relay.example.com", "reply"]
tag.getParams().get(0)  // "abc123"
tag.toArray()           // ["e", "abc123", "wss://relay.example.com", "reply"]
```

### Kind constants: `Kinds`

Common kind values are available as static `int` constants. Any integer is a valid kind — these are convenience constants for discoverability:

```java
Kinds.SET_METADATA       // 0
Kinds.TEXT_NOTE          // 1
Kinds.CONTACT_LIST       // 3
Kinds.ENCRYPTED_DIRECT_MESSAGE  // 4
Kinds.DELETION           // 5
Kinds.REPOST             // 6
Kinds.REACTION           // 7
Kinds.ZAP_REQUEST        // 9734
Kinds.ZAP_RECEIPT        // 9735

// Range checks
Kinds.isReplaceable(10002)  // true (10000-19999)
Kinds.isEphemeral(20001)    // true (20000-29999)
Kinds.isAddressable(30023)  // true (30000-39999)
Kinds.isValid(65536)        // false (must be 0-65535)
```

---

## Common Event Patterns

### Text note with tags

```java
GenericEvent note = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Check out #nostr! cc @someone")
    .tags(List.of(
        GenericTag.of("t", "nostr"),
        GenericTag.of("p", recipientPubKeyHex)
    ))
    .build();

identity.sign(note);
```

### Reply to an event

```java
GenericEvent reply = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Great post!")
    .tags(List.of(
        GenericTag.of("e", originalEventId, "wss://relay.example.com", "reply"),
        GenericTag.of("p", originalAuthorPubKey)
    ))
    .build();

identity.sign(reply);
```

### Reaction

```java
GenericEvent reaction = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.REACTION)
    .content("+")  // or any emoji
    .tags(List.of(
        GenericTag.of("e", targetEventId),
        GenericTag.of("p", targetAuthorPubKey)
    ))
    .build();

identity.sign(reaction);
```

### Replaceable event

```java
// Contact list (kind 3) — only the latest per pubkey is kept
GenericEvent contactList = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.CONTACT_LIST)
    .content("")
    .tags(List.of(
        GenericTag.of("p", friend1PubKey, "wss://relay1.example.com"),
        GenericTag.of("p", friend2PubKey, "wss://relay2.example.com")
    ))
    .build();

identity.sign(contactList);
```

### Ephemeral event

```java
// Typing indicator (kind 20001) — relays forward but don't store
GenericEvent typing = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(20001)
    .content("{\"typing\":true}")
    .build();

identity.sign(typing);
```

### Addressable event with `d` tag

```java
// Long-form content (kind 30023) — replaceable by pubkey + d-tag
GenericEvent article = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(30023)
    .content("# My Article\n\nFull content here...")
    .tags(List.of(
        GenericTag.of("d", "my-article-slug"),
        GenericTag.of("title", "My Article"),
        GenericTag.of("t", "blog")
    ))
    .build();

identity.sign(article);
```

---

## Encryption

### NIP-04 (legacy)

```java
MessageCipher04 cipher = new MessageCipher04(
    senderIdentity.getPrivateKey(),
    recipientPublicKey
);

String encrypted = cipher.encrypt("Secret message");
String decrypted = cipher.decrypt(encrypted);
```

### NIP-44 (recommended)

```java
MessageCipher44 cipher = new MessageCipher44(
    senderIdentity.getPrivateKey(),
    recipientPublicKey
);

String encrypted = cipher.encrypt("Secret message");
String decrypted = cipher.decrypt(encrypted);
```

---

## Filters

Query relays for specific events using `EventFilter`:

```java
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE, Kinds.REACTION))
    .authors(List.of(pubKeyHex))
    .since(timestampSeconds)
    .limit(50)
    .build();

Filters filters = new Filters(filter);
```

Tag-based filtering:

```java
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .addTagFilter("t", List.of("nostr", "bitcoin"))
    .addTagFilter("p", List.of(specificPubKey))
    .build();
```

---

## Testing

```java
@Test
void testEventCreation() {
    Identity identity = Identity.generateRandomIdentity();

    GenericEvent event = GenericEvent.builder()
        .pubKey(identity.getPublicKey())
        .kind(Kinds.TEXT_NOTE)
        .content("Test content")
        .tags(List.of(GenericTag.of("t", "test")))
        .build();

    identity.sign(event);

    assertNotNull(event.getId());
    assertNotNull(event.getSignature());
    assertEquals(Kinds.TEXT_NOTE, event.getKind());
    assertEquals("t", event.getTags().get(0).getCode());
    assertEquals("test", event.getTags().get(0).getParams().get(0));
}

@Test
void testSerialization() throws Exception {
    GenericEvent event = createAndSignEvent();

    String json = new EventMessage(event).encode();
    BaseMessage decoded = BaseMessage.read(json);

    assertTrue(decoded instanceof EventMessage);
    GenericEvent deserialized = ((EventMessage) decoded).getEvent();
    assertEquals(event.getId(), deserialized.getId());
}
```

---

## See Also

- [Custom events how-to](../howto/custom-events.md) — Sending custom event kinds
- [Streaming subscriptions](../howto/streaming-subscriptions.md) — Long-lived relay subscriptions
- [API reference](../reference/nostr-java-api.md) — Full class and method reference
- [NIP-01](https://github.com/nostr-protocol/nips/blob/master/01.md) — Basic protocol
- [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md) — Event kind ranges
