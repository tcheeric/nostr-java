# Using nostr-java

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [Streaming subscriptions](streaming-subscriptions.md) · [Custom events](custom-events.md) · [API reference](../reference/nostr-java-api.md)

This guide shows how to set up the library and publish a basic [Nostr](https://github.com/nostr-protocol/nips) event.

## Minimal setup

Add the client module to your project (with the BOM):

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>xyz.tcheeric</groupId>
      <artifactId>nostr-java-bom</artifactId>
      <version><!-- X.Y.Z --></version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-client</artifactId>
  </dependency>
</dependencies>
```

The `nostr-java-client` module transitively brings in all other modules (`identity`, `event`, `core`).

Check the [releases page](https://github.com/tcheeric/nostr-java/releases) for the latest BOM version.

## Create, sign, and publish an event

```java
import nostr.base.Kinds;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

import java.util.List;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        Identity identity = Identity.generateRandomIdentity();

        GenericEvent event = GenericEvent.builder()
            .pubKey(identity.getPublicKey())
            .kind(Kinds.TEXT_NOTE)
            .content("Hello Nostr!")
            .tags(List.of(GenericTag.of("t", "nostr-java")))
            .build();

        identity.sign(event);

        try (NostrRelayClient client = new NostrRelayClient("wss://relay.398ja.xyz")) {
            client.send(new EventMessage(event));
        }
    }
}
```

### Async alternative (Virtual Threads)

```java
NostrRelayClient.connectAsync("wss://relay.398ja.xyz")
    .thenCompose(client -> client.sendAsync(new EventMessage(event)))
    .thenAccept(responses -> System.out.println("Sent! Responses: " + responses))
    .join();
```

### Reference
- [`Identity.generateRandomIdentity`](../../nostr-java-identity/src/main/java/nostr/id/Identity.java)
- [`GenericEvent.builder`](../../nostr-java-event/src/main/java/nostr/event/impl/GenericEvent.java)
- [`NostrRelayClient`](../../nostr-java-client/src/main/java/nostr/client/springwebsocket/NostrRelayClient.java)

### Next steps
- Streaming, lifecycle, and backpressure: [streaming-subscriptions.md](streaming-subscriptions.md)
- Working with custom kinds: [custom-events.md](custom-events.md)
- Events and tags in depth: [../explanation/extending-events.md](../explanation/extending-events.md)
