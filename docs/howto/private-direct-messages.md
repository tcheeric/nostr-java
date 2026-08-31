# Send Private Direct Messages

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how-to](use-nostr-java-api.md) · [Streaming subscriptions](streaming-subscriptions.md) · [Custom events](custom-events.md)

This guide shows how to send and read private direct messages with **nostr-java**, using
[NIP-17](https://github.com/nostr-protocol/nips/blob/master/17.md) gift-wrapped messaging.

## What NIP-17 hides

A NIP-04 direct message hides only its text. The sender, the recipient, the exact time, and
the number of messages exchanged are all public on every relay that carries the event, so an
observer learns who talks to whom and when.

NIP-17 hides all of it, using three layers defined by
[NIP-59](https://github.com/nostr-protocol/nips/blob/master/59.md):

| Layer | Kind | Signed by | What it reveals |
| --- | --- | --- | --- |
| Rumor | 14 | nobody | the message, once decrypted |
| Seal | 13 | the real sender | who wrote it, to the recipient only |
| Gift wrap | 1059 | a single-use key | that *someone* sent *something* to a recipient |

The SDK builds and opens all three. You work with a `ChatMessage`, and never handle a seal,
an ephemeral key, or a conversation key yourself.

## Prerequisites

```xml
<dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-identity</artifactId>
</dependency>
```

## Publish where you receive messages

Before anyone can message you, publish a kind-10050 list naming the relays you read. NIP-17
says a sender **must not** deliver to any other relay, and **must not** send at all to
someone who has published no list. Without this, nobody can reach you.

```java
Identity alice = Identity.create(privateKeyHex);

DirectMessageRelayList inbox = new DirectMessageRelayList(
    alice.getPublicKey(),
    List.of(new Relay("wss://inbox.nostr.wine")),
    Instant.now().getEpochSecond());

GenericEvent inboxEvent = inbox.toEvent();
alice.sign(inboxEvent);
// publish inboxEvent through your relay client
```

Keep the list short, one to three relays, and publish it to as many relays as you can so
senders can find it.

## Send a message

```java
Nip17DirectMessageService messages = new Nip17DirectMessageService(alice);

ChatMessage message = messages.message()
    .to(bobPublicKey)
    .subject("Dinner")
    .content("Are you going to the party tonight?")
    .build();

List<GenericEvent> wraps = messages.compose(message);
```

`compose` returns **several events, not one**. There is no shared envelope in NIP-17: each
participant gets their own separately encrypted copy, which is what keeps the conversation's
membership private.

> **One of those copies is addressed to you.** A sender who publishes only their recipients'
> copies can never read the conversation back, because they cannot decrypt a wrap addressed
> to someone else. Publish every event `compose` returns, including your own.

## Send to the right relays

`compose` gives you events but not destinations. Use `planDelivery` to pair each copy with
the relays its recipient nominated:

```java
DirectMessageRelayLookup relayLists = pubkey -> lookUpKind10050For(pubkey);

for (MessageDelivery delivery : messages.planDelivery(message, relayLists)) {
    if (delivery.isDeliverable()) {
        publish(delivery.giftWrap(), delivery.relays());
    } else {
        log.info("{} is not accepting private messages", delivery.recipient());
    }
}
```

An unreachable recipient still appears in the plan, carrying no event. That is deliberate:
omitting them silently is how a message goes half-delivered without anyone noticing, and it
distinguishes "this person does not accept private messages" from "the relay was down".

You supply the lookup, backed by a relay query or a cache. Message composition itself never
touches the network.

## Read your messages

Subscribe to kind 1059 events tagged with your public key, then open each one:

```java
Nip17DirectMessageService messages = new Nip17DirectMessageService(bob);

for (GenericEvent giftWrap : incomingEvents) {
    try {
        ChatMessage received = messages.read(giftWrap);
        System.out.printf("%s: %s%n", received.getSender(), received.getContent());
    } catch (GiftWrapException notForUs) {
        // Expected: a kind-1059 subscription also delivers wraps we cannot open.
    }
}
```

**Catch and continue.** A kind-1059 subscription delivers wraps addressed to other people,
and possibly malformed ones. Abandoning the batch on the first failure lets one unopenable
event stall an entire conversation.

The sender reported by `read` is authenticated: the seal's signature is verified and its
author is checked against the rumor's before the message is returned. Because a rumor is
unsigned, that seal signature is the only evidence of who wrote the message.

## Reply to a message

```java
ChatMessage reply = messages.message()
    .to(received.getSender())
    .inReplyTo(receivedEventId)
    .content("Yes, see you at eight")
    .build();
```

## Group conversations

Add more recipients. The participants define the conversation, so adding or removing one
starts a *different* conversation with its own history.

```java
ChatMessage groupMessage = messages.message()
    .to(bobPublicKey)
    .to(carolPublicKey)
    .content("Dinner at eight?")
    .build();
```

Every participant needs their own encrypted copy, so cost grows with group size. NIP-17
advises finding another scheme beyond about ten participants.

## Ephemeral messages

For real-time chat that relays should not store, wrap in kind 21059 instead:

```java
DirectMessageService liveChat = new Nip17DirectMessageService(
    alice, new Nip59GiftWrapper(alice, Kinds.EPHEMERAL_GIFT_WRAP));
```

## What the SDK does not do

- **Publishing and subscribing.** These types produce and consume events; routing them is
  the caller's job, using `NostrRelayClient`.
- **Storing messages.** There is no inbox. Decide what to keep.
- **NIP-42 AUTH.** Relays are advised to serve kind-1059 events only to their addressee,
  behind authentication. Delivery from such relays needs AUTH support in your client.

## Related

- [NIP-17](https://github.com/nostr-protocol/nips/blob/master/17.md) — private direct messages
- [NIP-59](https://github.com/nostr-protocol/nips/blob/master/59.md) — gift wrap
- [NIP-44](https://github.com/nostr-protocol/nips/blob/master/44.md) — the encryption underneath
- [Streaming subscriptions](streaming-subscriptions.md) — receiving events as they arrive
- [NIP-17 implementation spec](../explanation/nip-17-direct-messages-spec.md) — design notes
