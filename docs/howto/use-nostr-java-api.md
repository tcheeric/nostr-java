# Using the nostr-java API

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [Streaming subscriptions](streaming-subscriptions.md) · [Custom events](custom-events.md) · [API reference](../reference/nostr-java-api.md)

This guide shows how to set up the library and publish a basic [Nostr](https://github.com/nostr-protocol/nips) event.

## Minimal setup

Add the API module to your project:

```xml
<dependency>
  <groupId>xyz.tcheeric</groupId>
  <artifactId>nostr-java-api</artifactId>
  <version>0.6.0</version>
</dependency>
```

The current version is `0.5.1`. Check the [releases page](https://github.com/tcheeric/nostr-java/releases) for the latest version.

## Create, sign, and publish an event

```java
import nostr.api.NIP01;
import nostr.id.Identity;

import java.util.Map;

public class QuickStart {
    public static void main(String[] args) {
        Identity identity = Identity.generateRandomIdentity();
        Map<String, String> relays = Map.of("damus", "wss://relay.398ja.xyz");

        new NIP01(identity)
            .createTextNoteEvent("Hello nostr")
            .sign()
            .send(relays);
    }
}
```

### Reference
- [`Identity.generateRandomIdentity`](../../nostr-java-id/src/main/java/nostr/id/Identity.java)
- [`NIP01.createTextNoteEvent`](../../nostr-java-api/src/main/java/nostr/api/NIP01.java)
- [`EventNostr.sign`](../../nostr-java-api/src/main/java/nostr/api/EventNostr.java)
- [`EventNostr.send`](../../nostr-java-api/src/main/java/nostr/api/EventNostr.java)

### Next steps
- Streaming, lifecycle, and backpressure: [streaming-subscriptions.md](streaming-subscriptions.md)
