# Codebase Overview

This document provides an overview of the project structure and instructions for building and testing the modules.

## Module layout
- **nostr-java-base** – common model classes and utilities used across the project.
- **nostr-java-crypto** – pure Java implementation of BIP340 Schnorr signature test vectors.
- **nostr-java-event** – definitions of Nostr events and tags.
- **nostr-java-id** – identity generation and handling of keys.
- **nostr-java-util** – helper utilities used by other modules.
- **nostr-java-client** – WebSocket client used to communicate with relays.
- **nostr-java-api** – high level API wrapping event creation and relay communication.
- **nostr-java-encryption** – optional encryption support for messages.
- **nostr-java-examples** – sample applications demonstrating how to use the API.

## Building and testing
The project can be built with Maven or Gradle. Unit tests do not require a running relay, while integration tests use Testcontainers to start a relay in Docker.

### Unit-tested build
```bash
# Maven
./mvnw clean test
./mvnw install -Dmaven.test.skip=true

# Gradle
./gradlew clean test
./gradlew publishToMavenLocal
```

### Integration-tested build (requires Docker)
```bash
# Maven
./mvnw clean install

# Gradle
./gradlew clean check
./gradlew publishToMavenLocal
```
Integration tests start a `nostr-rs-relay` container automatically. The image used can be overridden in `src/test/resources/relay-container.properties` by setting `relay.container.image=<image>`.

## WebSocket configuration
`StandardWebSocketClient` waits for relay responses when sending messages. The timeout and polling interval are configured with the following properties (values in milliseconds):
```
nostr.websocket.await-timeout-ms=60000
nostr.websocket.poll-interval-ms=500
```

## Retry behavior
`SpringWebSocketClient` leverages Spring Retry so that failed send operations are retried up to three times with an exponential backoff starting at 500 ms.

## Creating and sending events
The examples module shows how to create built-in and custom events. Below is an excerpt from the examples illustrating the creation of a `TextNoteEvent`:
```java
    private static final Identity RECIPIENT = Identity.generateRandomIdentity();
    private static final Identity SENDER = Identity.generateRandomIdentity();

    private static GenericEvent sendTextNoteEvent() {
 
        List<BaseTag> tags = new ArrayList<>(List.of(new PubKeyTag(RECIPIENT.getPublicKey())));

        var nip01 = new NIP01(SENDER);
        nip01.createTextNoteEvent(tags, "Hello world, I'm here on nostr-java API!")
                .sign()
                .send(RELAYS);

        return nip01.getEvent();
    }
```
## Creating custom events and tags
Custom tag types can be introduced without modifying existing core code by
registering them with the `TagRegistry`. The registry maps tag codes to factory
functions responsible for creating concrete `BaseTag` implementations from a
`GenericTag` representation.