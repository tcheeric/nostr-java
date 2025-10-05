# API Examples Guide

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how‑to](use-nostr-java-api.md) · [API reference](../reference/nostr-java-api.md)

This guide walks through the comprehensive examples in [`NostrApiExamples.java`](../../nostr-java-examples/src/main/java/nostr/examples/NostrApiExamples.java), demonstrating 13+ common use cases for the nostr-java API.

## Table of Contents

1. [Setup](#setup)
2. [Metadata Events (NIP-01)](#metadata-events-nip-01)
3. [Text Notes (NIP-01)](#text-notes-nip-01)
4. [Encrypted Direct Messages (NIP-04)](#encrypted-direct-messages-nip-04)
5. [Event Deletion (NIP-09)](#event-deletion-nip-09)
6. [Ephemeral Events](#ephemeral-events)
7. [Reactions (NIP-25)](#reactions-nip-25)
8. [Replaceable Events](#replaceable-events)
9. [Internet Identifiers (NIP-05)](#internet-identifiers-nip-05)
10. [Filters and Subscriptions](#filters-and-subscriptions)
11. [Public Channels (NIP-28)](#public-channels-nip-28)
12. [Running the Examples](#running-the-examples)

---

## Setup

All examples use two identities (sender and recipient) and a local relay:

```java
private static final Identity RECIPIENT = Identity.generateRandomIdentity();
private static final Identity SENDER = Identity.generateRandomIdentity();
private static final Map<String, String> RELAYS = Map.of("local", "localhost:5555");
```

**For testing**, you can:
- Use a local relay (e.g., [nostr-rs-relay](https://github.com/scsibug/nostr-rs-relay))
- Replace with public relays: `Map.of("damus", "wss://relay.damus.io")`

---

## Metadata Events (NIP-01)

**Purpose**: Publish user profile information (name, picture, about, NIP-05 identifier)

**Example**:
```java
private static GenericEvent metaDataEvent() {
    // Create a user profile
    UserProfile profile = new UserProfile(
        SENDER.getPublicKey(),
        "Nostr Guy",                    // name
        "guy@nostr-java.io",           // nip05 identifier
        "It's me!",                     // about/bio
        null                            // lud16 (Lightning address)
    );

    // Set profile picture
    profile.setPicture(
        new URI("https://example.com/avatar.jpg").toURL()
    );

    // Create and send metadata event
    var nip01 = new NIP01(SENDER);
    nip01.createMetadataEvent(profile)
         .sign()
         .send(RELAYS);

    return nip01.getEvent();
}
```

**What it does**:
- Creates a kind `0` (metadata) event
- Encodes profile data as JSON in event content
- Signs and publishes to configured relays

**Use case**: User profile updates, onboarding new users

---

## Text Notes (NIP-01)

**Purpose**: Post public text messages (tweets/notes)

**Example**:
```java
private static GenericEvent sendTextNoteEvent() {
    // Create tags (e.g., mention another user)
    List<BaseTag> tags = List.of(
        new PubKeyTag(RECIPIENT.getPublicKey())
    );

    // Create and send text note
    var nip01 = new NIP01(SENDER);
    nip01.createTextNoteEvent(tags, "Hello world, I'm here on nostr-java API!")
         .sign()
         .send(RELAYS);

    return nip01.getEvent();
}
```

**What it does**:
- Creates a kind `1` (text note) event
- Adds tags (mentions, hashtags, etc.)
- Signs and broadcasts to relays

**Use case**: Social media posts, announcements, public messages

**Variations**:
```java
// Simple note without tags
nip01.createTextNoteEvent("Hello Nostr!")
     .sign()
     .send(RELAYS);

// With multiple tags
List<BaseTag> tags = List.of(
    new PubKeyTag(user1PublicKey),
    new HashtagTag("nostr"),
    new ExpirationTag((int) (System.currentTimeMillis() / 1000) + 3600) // 1 hour
);
nip01.createTextNoteEvent(tags, "Check out #nostr!")
     .sign()
     .send(RELAYS);
```

---

## Encrypted Direct Messages (NIP-04)

**Purpose**: Send private encrypted messages between users

**Example**:
```java
private static void sendEncryptedDirectMessage() {
    // Create NIP-04 instance with sender and recipient
    var nip04 = new NIP04(SENDER, RECIPIENT.getPublicKey());

    // Create and send encrypted DM
    nip04.createDirectMessageEvent("Hello Nakamoto!")
         .sign()
         .send(RELAYS);
}
```

**Decryption**:
```java
// Recipient decrypts the message
NIP04Event dmEvent = /* received event */;
String plaintext = NIP04.decrypt(RECIPIENT, dmEvent);
System.out.println("Decrypted: " + plaintext);
```

**What it does**:
- Encrypts message using NIP-04 encryption (sender private key + recipient public key)
- Creates a kind `4` event with encrypted content
- Only sender and recipient can decrypt

**Security note**: NIP-04 is considered legacy. For new applications, consider NIP-44 encryption:
```java
MessageCipher44 cipher = new MessageCipher44(
    SENDER.getPrivateKey(),
    RECIPIENT.getPublicKey()
);
String encrypted = cipher.encrypt("Secret message");
```

---

## Event Deletion (NIP-09)

**Purpose**: Request deletion of previously published events

**Example**:
```java
private static void deletionEvent() {
    // Create an event to delete
    var event = sendTextNoteEvent();

    // Create deletion request
    var nip09 = new NIP09(SENDER);
    nip09.createDeletionEvent(event)
         .sign()
         .send();
}
```

**What it does**:
- Creates a kind `5` (deletion) event
- References the event to delete via `e` tag
- Relays may or may not honor deletion requests

**Important**:
- Only the event author can request deletion
- Relays decide whether to honor the request
- No guarantee the event will be deleted from all relays

**Deleting multiple events**:
```java
List<GenericEvent> eventsToDelete = List.of(event1, event2, event3);
nip09.createDeletionEvent(eventsToDelete)
     .sign()
     .send();
```

---

## Ephemeral Events

**Purpose**: Create events that relays should not persist

**Example**:
```java
private static void ephemeralEvent() {
    var nip01 = new NIP01(SENDER);
    nip01.createEphemeralEvent(
            Kind.EPHEMEREAL_EVENT.getValue(),  // kind: 20000-29999
            "An ephemeral event"
         )
         .sign()
         .send(RELAYS);
}
```

**What it does**:
- Creates an ephemeral event (kind 20000-29999 per NIP-16)
- Relays forward but don't store these events
- Useful for real-time, transient data

**Use cases**:
- Typing indicators
- Online presence status
- Temporary notifications
- Real-time collaborative editing

```java
// Typing indicator
nip01.createEphemeralEvent(20001, "{\"typing\":true}")
     .sign()
     .send(RELAYS);

// Online status
nip01.createEphemeralEvent(20002, "{\"status\":\"online\"}")
     .sign()
     .send(RELAYS);
```

---

## Reactions (NIP-25)

**Purpose**: React to events with likes, emoji, or custom reactions

**Example**:
```java
private static void reactionEvent() {
    // 1. Create a post to react to
    List<BaseTag> tags = List.of(
        NIP30.createEmojiTag(
            "soapbox",
            "https://gleasonator.com/emoji/Gleasonator/soapbox.png"
        )
    );

    var nip01 = new NIP01(SENDER);
    var event = nip01.createTextNoteEvent(
        tags,
        "Hello Astral, Please like me! :soapbox:"
    );
    event.signAndSend(RELAYS);

    // 2. Like reaction
    var nip25 = new NIP25(RECIPIENT);
    nip25.createReactionEvent(
            event.getEvent(),
            Reaction.LIKE,  // "+"
            new Relay("localhost:5555")
         )
         .signAndSend(RELAYS);

    // 3. Emoji reaction
    nip25.createReactionEvent(
            event.getEvent(),
            "💩",  // Any emoji
            new Relay("localhost:5555")
         )
         .signAndSend();

    // 4. Custom emoji reaction (using NIP-30)
    BaseTag eventTag = NIP01.createEventTag(event.getEvent().getId());
    nip25.createReactionEvent(
            eventTag,
            NIP30.createEmojiTag(
                "ablobcatrainbow",
                "https://gleasonator.com/emoji/blobcat/ablobcatrainbow.png"
            )
         )
         .signAndSend();
}
```

**Reaction types**:
- `Reaction.LIKE` → `"+"`
- `Reaction.DISLIKE` → `"-"`
- Any Unicode emoji: `"❤️"`, `"🔥"`, `"👍"`
- Custom emoji via NIP-30

---

## Replaceable Events

**Purpose**: Create events that replace previous events of the same kind

**Example**:
```java
private static void replaceableEvent() {
    var nip01 = new NIP01(SENDER);

    // Create initial event
    var event = nip01.createTextNoteEvent("Hello Astral, Please replace me!");
    event.signAndSend(RELAYS);

    // Create replaceable event (kind 10000-19999)
    nip01.createReplaceableEvent(
            List.of(NIP01.createEventTag(event.getEvent().getId())),
            Kind.REPLACEABLE_EVENT.getValue(),  // kind: 10000-19999
            "New content"
         )
         .signAndSend();
}
```

**What it does**:
- Replaceable events (kind 10000-19999) replace older events by the same author
- Relays keep only the most recent event of each kind per pubkey
- Useful for settings, profiles, status updates

**Use cases**:
```java
// User preferences (kind 10000)
nip01.createReplaceableEvent(
    List.of(),
    10000,
    "{\"theme\":\"dark\",\"language\":\"en\"}"
).signAndSend(RELAYS);

// Contact list (kind 3)
List<BaseTag> contacts = List.of(
    new PubKeyTag(friend1PubKey),
    new PubKeyTag(friend2PubKey)
);
nip01.createReplaceableEvent(contacts, 3, "").signAndSend(RELAYS);
```

---

## Internet Identifiers (NIP-05)

**Purpose**: Link Nostr public key to a DNS-based identifier (name@domain.com)

**Example**:
```java
private static void internetIdMetadata() {
    var profile = UserProfile.builder()
        .name("Guilherme Gps")
        .publicKey(new PublicKey(
            "21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144"
        ))
        .nip05("me@guilhermegps.com.br")  // NIP-05 identifier
        .build();

    var nip05 = new NIP05(SENDER);
    nip05.createInternetIdentifierMetadataEvent(profile)
         .sign()
         .send(RELAYS);
}
```

**What it does**:
- Creates a kind `0` metadata event with NIP-05 identifier
- Links public key to human-readable identifier
- Clients can verify the link via `.well-known/nostr.json`

**Verification** (server-side):
Create `https://yourdomain.com/.well-known/nostr.json`:
```json
{
  "names": {
    "username": "21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144"
  }
}
```

---

## Filters and Subscriptions

**Purpose**: Query relays for specific events

**Example**:
```java
private static void filters() throws InterruptedException {
    var date = Calendar.getInstance();
    date.add(Calendar.DAY_OF_MONTH, -5);  // 5 days ago

    var nip01 = NIP01.getInstance();
    nip01.setRelays(RELAYS)
         .sendRequest(
             new Filters(
                 new KindFilter<>(Kind.EPHEMEREAL_EVENT),
                 new KindFilter<>(Kind.TEXT_NOTE),
                 new AuthorFilter<>(new PublicKey(
                     "21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144"
                 )),
                 new SinceFilter(date.getTimeInMillis() / 1000)
             ),
             "subId" + System.currentTimeMillis()
         );

    Thread.sleep(5000);  // Wait for responses
}
```

**Filter types**:
- `KindFilter` – Filter by event kind
- `AuthorFilter` – Filter by author public key
- `SinceFilter` – Events since timestamp
- `UntilFilter` – Events until timestamp
- `IdsFilter` – Specific event IDs
- `LimitFilter` – Limit number of results

**Advanced filtering**:
```java
Filters filters = new Filters(
    new KindFilter<>(Kind.TEXT_NOTE),
    new AuthorFilter<>(authorPubKey)
);
filters.setLimit(50);  // Return max 50 events

// Multiple authors
Filters multiAuthor = new Filters(
    new AuthorFilter<>(author1, author2, author3),
    new KindFilter<>(Kind.TEXT_NOTE)
);
```

**Non-blocking subscriptions**:
For long-lived subscriptions, see [streaming-subscriptions.md](streaming-subscriptions.md):
```java
AutoCloseable subscription = client.subscribe(
    filters,
    "my-subscription",
    message -> System.out.println("Received: " + message),
    error -> System.err.println("Error: " + error)
);
```

---

## Public Channels (NIP-28)

NIP-28 provides IRC-like public channels.

### Create a Channel

```java
private static GenericEvent createChannel() {
    var channel = new ChannelProfile(
        "JNostr Channel",
        "This is a channel to test NIP28 in nostr-java",
        "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg"
    );

    var nip28 = new NIP28(SENDER);
    nip28.createChannelCreateEvent(channel)
         .sign()
         .send();

    return nip28.getEvent();
}
```

### Update Channel Metadata

```java
private static void updateChannelMetadata() {
    var channelCreateEvent = createChannel();

    var updatedChannel = new ChannelProfile(
        "Updated Channel Name",
        "Updated description",
        "https://example.com/new-image.jpg"
    );

    var nip28 = new NIP28(SENDER);
    nip28.updateChannelMetadataEvent(
            channelCreateEvent,
            updatedChannel,
            null  // relay recommendations
         )
         .sign()
         .send();
}
```

### Send Channel Message

```java
private static GenericEvent sendChannelMessage() {
    var channelCreateEvent = createChannel();

    var nip28 = new NIP28(SENDER);
    nip28.createChannelMessageEvent(
            channelCreateEvent,
            new Relay("localhost:5555"),
            "Hello everybody!"
         )
         .sign()
         .send();

    return nip28.getEvent();
}
```

### Hide Message

```java
private static void hideMessage() {
    var channelMessageEvent = sendChannelMessage();

    var nip28 = new NIP28(SENDER);
    nip28.createHideMessageEvent(
            channelMessageEvent,
            "Spam"  // reason
         )
         .sign()
         .send();
}
```

### Mute User

```java
private static void muteUser() {
    var nip28 = new NIP28(SENDER);
    nip28.createMuteUserEvent(
            RECIPIENT.getPublicKey(),
            "Posting spam"  // reason
         )
         .sign()
         .send();
}
```

**Channel operations**:
- `createChannelCreateEvent` – Create new channel (kind 40)
- `updateChannelMetadataEvent` – Update channel info (kind 41)
- `createChannelMessageEvent` – Post to channel (kind 42)
- `createHideMessageEvent` – Hide message (kind 43)
- `createMuteUserEvent` – Mute user (kind 44)

---

## Running the Examples

### Prerequisites

1. **Java 21+**
2. **Local relay** (optional but recommended):
   ```bash
   # Using Docker
   docker run -p 5555:8080 scsibug/nostr-rs-relay

   # Or use public relays (update RELAYS constant)
   ```

### Run All Examples

```bash
# Clone the repository
git clone https://github.com/tcheeric/nostr-java.git
cd nostr-java

# Build the project
./mvnw clean install

# Run the examples
cd nostr-java-examples
mvn exec:java -Dexec.mainClass="nostr.examples.NostrApiExamples"
```

### Run Specific Examples

Modify `NostrApiExamples.java` to run only specific examples:

```java
public void run() throws Exception {
    logAccountsData();

    // Comment out examples you don't want to run
    // metaDataEvent();
    sendTextNoteEvent();
    // sendEncryptedDirectMessage();
    // ...
}
```

### Expected Output

```
################################ ACCOUNTS BEGINNING ################################
*** RECEIVER ***

* PrivateKey: nsec1...
* PublicKey: npub1...

*** SENDER ***

* PrivateKey: nsec1...
* PublicKey: npub1...
################################ ACCOUNTS END ################################

##############################
	sendTextNoteEvent
##############################
[Event sent output...]

##############################
	sendEncryptedDirectMessage
##############################
[DM sent output...]

...
```

### Using with Public Relays

Replace the relay constant:

```java
// Instead of local relay
private static final Map<String, String> RELAYS =
    Map.of("local", "localhost:5555");

// Use public relays
private static final Map<String, String> RELAYS = Map.of(
    "damus", "wss://relay.damus.io",
    "nos", "wss://nos.lol"
);
```

---

## Example Variations

### Batch Operations

Send multiple events:

```java
var nip01 = new NIP01(SENDER);
List.of("Message 1", "Message 2", "Message 3")
    .forEach(content ->
        nip01.createTextNoteEvent(content)
             .sign()
             .send(RELAYS)
    );
```

### Error Handling

Handle failures gracefully:

```java
try {
    var nip01 = new NIP01(SENDER);
    nip01.createTextNoteEvent("Hello Nostr!")
         .sign()
         .send(RELAYS);
} catch (IOException e) {
    System.err.println("Failed to send event: " + e.getMessage());
    // Retry logic or queue for later
}
```

### Async Publishing

Send events asynchronously:

```java
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    var nip01 = new NIP01(SENDER);
    nip01.createTextNoteEvent("Async message")
         .sign()
         .send(RELAYS);
});

future.thenRun(() -> System.out.println("Event sent!"));
```

---

## See Also

- [API How-To](use-nostr-java-api.md) – Basic API usage
- [Streaming Subscriptions](streaming-subscriptions.md) – Long-lived subscriptions
- [Custom Events](custom-events.md) – Creating custom event types
- [API Reference](../reference/nostr-java-api.md) – Complete API documentation
- [NostrApiExamples.java source](../../nostr-java-examples/src/main/java/nostr/examples/NostrApiExamples.java) – Full example code

## Related NIPs

- [NIP-01](https://github.com/nostr-protocol/nips/blob/master/01.md) – Basic protocol
- [NIP-04](https://github.com/nostr-protocol/nips/blob/master/04.md) – Encrypted direct messages
- [NIP-05](https://github.com/nostr-protocol/nips/blob/master/05.md) – DNS identifiers
- [NIP-09](https://github.com/nostr-protocol/nips/blob/master/09.md) – Event deletion
- [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md) – Event kinds
- [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md) – Reactions
- [NIP-28](https://github.com/nostr-protocol/nips/blob/master/28.md) – Public channels
- [NIP-30](https://github.com/nostr-protocol/nips/blob/master/30.md) – Custom emoji
