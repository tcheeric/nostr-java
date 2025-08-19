# Using the nostr-java API

This guide shows how to set up the library and publish a basic [Nostr](https://github.com/nostr-protocol/nips) event.

## Minimal setup

Add the API module to your project:

```xml
<dependency>
  <groupId>xyz.tcheeric</groupId>
  <artifactId>nostr-java-api</artifactId>
  <version>[VERSION]</version>
</dependency>
```

Replace `[VERSION]` with the latest release number.

## Create, sign, and publish an event

```java
import nostr.api.NIP01;
import nostr.id.Identity;

import java.util.Map;

public class QuickStart {
    public static void main(String[] args) {
        Identity identity = Identity.generateRandomIdentity();
        Map<String, String> relays = Map.of("local", "wss://nostr.example");

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
