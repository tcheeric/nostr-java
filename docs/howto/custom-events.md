# Custom Nostr Events

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how-to](use-nostr-java-api.md) · [Streaming subscriptions](streaming-subscriptions.md) · [API reference](../reference/nostr-java-api.md)

This guide shows how to construct and publish a Nostr event with a non-standard `kind` using **nostr-java**.

## Background

Every Nostr event must include the fields defined in [NIP-01](https://github.com/nostr-protocol/nips/blob/master/01.md):

- `id`
- `pubkey`
- `created_at`
- `kind`
- `tags`
- `content`
- `sig`

Kinds that are not defined by existing NIPs may still be used. [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md) describes how kind numbers are grouped (regular, replaceable, ephemeral and parameterized replaceable). Choose a value that does not collide with other applications.

| Range | Type | Description |
|-------|------|-------------|
| 0-9999 | Regular | Standard events, can be deleted |
| 10000-19999 | Replaceable | Newer event replaces older (by pubkey) |
| 20000-29999 | Ephemeral | Not stored by relays |
| 30000-39999 | Parameterized Replaceable | Replaceable with `d` tag parameter |

## Example

```java
import java.util.List;

import nostr.client.springwebsocket.NostrRelayClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

public class CustomEventExample {
    public static void main(String[] args) throws Exception {
        Identity identity = Identity.generateRandomIdentity();

        int CUSTOM_KIND = 9000;  // Non-standard kind
        GenericEvent event = GenericEvent.builder()
            .pubKey(identity.getPublicKey())
            .kind(CUSTOM_KIND)
            .content("Hello from a custom kind!")
            .tags(List.of(
                GenericTag.of("d", "my-identifier"),
                GenericTag.of("t", "custom")
            ))
            .build();

        // id and sig are populated when signing
        identity.sign(event);

        try (NostrRelayClient client = new NostrRelayClient("wss://relay.398ja.xyz")) {
            client.send(new EventMessage(event));
        }
    }
}
```

## Steps Explained

1. **Construct the event** — Use `GenericEvent.builder()` with any `int` kind, content, and tags. The builder fills in `created_at` automatically.
2. **Add tags** — Use `GenericTag.of(code, params...)` to create tags. Tags are just a code string and a list of string parameters.
3. **Sign** — `Identity.sign(event)` computes the event `id` and `sig` using the private key. Relays verify these fields against the serialized event bytes as defined in NIP-01.
4. **Send to a relay** — Send the event using an `EVENT` message via `NostrRelayClient`.

## Async alternative

```java
NostrRelayClient.connectAsync("wss://relay.398ja.xyz")
    .thenCompose(client -> client.sendAsync(new EventMessage(event)))
    .thenAccept(responses -> System.out.println("Responses: " + responses))
    .join();
```

## Kind range checks

Use `Kinds` utility methods to classify kinds:

```java
import nostr.base.Kinds;

Kinds.isReplaceable(CUSTOM_KIND)   // true if 10000-19999
Kinds.isEphemeral(CUSTOM_KIND)    // true if 20000-29999
Kinds.isAddressable(CUSTOM_KIND)  // true if 30000-39999
Kinds.isValid(CUSTOM_KIND)        // true if 0-65535
```

For more information about event structure and relay communication, consult [NIP-01](https://github.com/nostr-protocol/nips/blob/master/01.md) and [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md).
