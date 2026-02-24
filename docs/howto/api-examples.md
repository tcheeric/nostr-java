# API Examples Guide

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how-to](use-nostr-java-api.md) · [API reference](../reference/nostr-java-api.md)

This guide demonstrates common use cases for the nostr-java library using `GenericEvent`, `GenericTag`, and `NostrRelayClient`.

## Table of Contents

1. [Setup](#setup)
2. [Text Notes (NIP-01)](#text-notes-nip-01)
3. [Metadata Events (NIP-01)](#metadata-events-nip-01)
4. [Encrypted Direct Messages (NIP-04/44)](#encrypted-direct-messages-nip-0444)
5. [Event Deletion (NIP-09)](#event-deletion-nip-09)
6. [Reactions (NIP-25)](#reactions-nip-25)
7. [Replaceable Events](#replaceable-events)
8. [Ephemeral Events](#ephemeral-events)
9. [Filters and Subscriptions](#filters-and-subscriptions)
10. [Async Operations](#async-operations)

---

## Setup

All examples use two identities (sender and recipient) and a relay:

```java
import nostr.base.Kinds;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

private static final Identity SENDER = Identity.generateRandomIdentity();
private static final Identity RECIPIENT = Identity.generateRandomIdentity();
private static final String RELAY = "wss://relay.398ja.xyz";
```

---

## Text Notes (NIP-01)

**Purpose**: Post public text messages.

```java
GenericEvent note = GenericEvent.builder()
    .pubKey(SENDER.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello world, I'm here on nostr-java!")
    .tags(List.of(
        GenericTag.of("p", RECIPIENT.getPublicKey().toString())
    ))
    .build();

SENDER.sign(note);

try (NostrRelayClient client = new NostrRelayClient(RELAY)) {
    client.send(new EventMessage(note));
}
```

**With hashtags:**
```java
GenericEvent tagged = GenericEvent.builder()
    .pubKey(SENDER.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Check out #nostr! cc @someone")
    .tags(List.of(
        GenericTag.of("t", "nostr"),
        GenericTag.of("p", somePublicKeyHex)
    ))
    .build();
```

---

## Metadata Events (NIP-01)

**Purpose**: Publish user profile information.

```java
String profileJson = """
    {
        "name": "Nostr Guy",
        "about": "It's me!",
        "picture": "https://example.com/avatar.jpg",
        "nip05": "guy@nostr-java.io"
    }
    """;

GenericEvent metadata = GenericEvent.builder()
    .pubKey(SENDER.getPublicKey())
    .kind(Kinds.SET_METADATA)
    .content(profileJson)
    .build();

SENDER.sign(metadata);
```

---

## Encrypted Direct Messages (NIP-04/44)

**NIP-04 (legacy):**
```java
import nostr.encryption.MessageCipher04;

MessageCipher04 cipher = new MessageCipher04(
    SENDER.getPrivateKey(),
    RECIPIENT.getPublicKey()
);

String encrypted = cipher.encrypt("Hello Nakamoto!");

GenericEvent dm = GenericEvent.builder()
    .pubKey(SENDER.getPublicKey())
    .kind(Kinds.ENCRYPTED_DIRECT_MESSAGE)
    .content(encrypted)
    .tags(List.of(
        GenericTag.of("p", RECIPIENT.getPublicKey().toString())
    ))
    .build();

SENDER.sign(dm);
```

**NIP-44 (recommended):**
```java
import nostr.encryption.MessageCipher44;

MessageCipher44 cipher = new MessageCipher44(
    SENDER.getPrivateKey(),
    RECIPIENT.getPublicKey()
);

String encrypted = cipher.encrypt("Secret message");
String decrypted = cipher.decrypt(encrypted);
```

---

## Event Deletion (NIP-09)

**Purpose**: Request deletion of previously published events.

```java
GenericEvent deletion = GenericEvent.builder()
    .pubKey(SENDER.getPublicKey())
    .kind(Kinds.DELETION)
    .content("Deleting old posts")
    .tags(List.of(
        GenericTag.of("e", eventIdToDelete1),
        GenericTag.of("e", eventIdToDelete2)
    ))
    .build();

SENDER.sign(deletion);
```

---

## Reactions (NIP-25)

**Purpose**: React to events with likes or emoji.

```java
// Like reaction
GenericEvent like = GenericEvent.builder()
    .pubKey(RECIPIENT.getPublicKey())
    .kind(Kinds.REACTION)
    .content("+")
    .tags(List.of(
        GenericTag.of("e", targetEventId),
        GenericTag.of("p", targetAuthorPubKey)
    ))
    .build();

RECIPIENT.sign(like);

// Emoji reaction
GenericEvent emoji = GenericEvent.builder()
    .pubKey(RECIPIENT.getPublicKey())
    .kind(Kinds.REACTION)
    .content("\uD83D\uDD25")  // fire emoji
    .tags(List.of(
        GenericTag.of("e", targetEventId),
        GenericTag.of("p", targetAuthorPubKey)
    ))
    .build();

RECIPIENT.sign(emoji);
```

---

## Replaceable Events

**Purpose**: Events that replace previous events of the same kind per pubkey.

```java
// Contact list (kind 3) — only the latest is kept
GenericEvent contactList = GenericEvent.builder()
    .pubKey(SENDER.getPublicKey())
    .kind(Kinds.CONTACT_LIST)
    .content("")
    .tags(List.of(
        GenericTag.of("p", friend1PubKey, "wss://relay1.example.com"),
        GenericTag.of("p", friend2PubKey, "wss://relay2.example.com")
    ))
    .build();

SENDER.sign(contactList);
```

---

## Ephemeral Events

**Purpose**: Events that relays should not persist (kind 20000-29999).

```java
GenericEvent typing = GenericEvent.builder()
    .pubKey(SENDER.getPublicKey())
    .kind(20001)  // ephemeral range
    .content("{\"typing\":true}")
    .build();

SENDER.sign(typing);
```

---

## Filters and Subscriptions

**Purpose**: Query relays for specific events.

```java
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.message.ReqMessage;

// Build filters
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE, Kinds.REACTION))
    .authors(List.of(pubKeyHex))
    .since(System.currentTimeMillis() / 1000 - 86400)  // last 24 hours
    .limit(50)
    .build();

Filters filters = new Filters(filter);
String subId = "my-sub-" + System.currentTimeMillis();
ReqMessage req = new ReqMessage(subId, filters);

// Blocking request
try (NostrRelayClient client = new NostrRelayClient(RELAY)) {
    List<String> responses = client.send(req);
    responses.forEach(System.out::println);
}

// Non-blocking subscription
try (NostrRelayClient client = new NostrRelayClient(RELAY)) {
    AutoCloseable subscription = client.subscribe(
        req,
        message -> System.out.println("Event: " + message),
        error -> System.err.println("Error: " + error.getMessage()),
        () -> System.out.println("Closed")
    );

    Thread.sleep(10_000);  // listen for 10 seconds
    subscription.close();
}
```

---

## Async Operations

**Purpose**: Non-blocking relay operations using Virtual Threads.

```java
// Connect and send asynchronously
NostrRelayClient.connectAsync(RELAY)
    .thenCompose(client -> client.sendAsync(new EventMessage(event)))
    .thenAccept(responses -> {
        System.out.println("Sent! Responses: " + responses);
    })
    .exceptionally(ex -> {
        System.err.println("Failed: " + ex.getMessage());
        return null;
    })
    .join();

// Async subscription
NostrRelayClient.connectAsync(RELAY)
    .thenCompose(client -> client.subscribeAsync(
        req.encode(),
        message -> System.out.println("Event: " + message),
        error -> System.err.println("Error: " + error),
        () -> System.out.println("Done")
    ))
    .thenAccept(subscription -> {
        // Close when done: subscription.close()
    });
```

---

## See Also

- [API how-to](use-nostr-java-api.md) — Minimal setup and quick start
- [Streaming subscriptions](streaming-subscriptions.md) — Long-lived subscriptions
- [Custom events](custom-events.md) — Working with custom event kinds
- [Events and tags](../explanation/extending-events.md) — In-depth guide to GenericEvent and GenericTag
- [API reference](../reference/nostr-java-api.md) — Full class and method reference
