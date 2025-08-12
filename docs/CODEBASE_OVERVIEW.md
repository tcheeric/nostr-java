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
The project is built with Maven. Unit tests do not require a running relay, while integration tests use Testcontainers to start a relay in Docker.

### Unit-tested build
```bash
./mvnw clean test
./mvnw install -Dmaven.test.skip=true
```

### Integration-tested build (requires Docker)
```bash
./mvnw clean install
```
Integration tests start a `nostr-rs-relay` container automatically. The image used can be overridden in `src/test/resources/relay-container.properties` by setting `relay.container.image=<image>`.

## WebSocket configuration
`StandardWebSocketClient` waits for relay responses when sending messages. The timeout and polling interval are configured with the following properties (values in milliseconds):
```
nostr.websocket.await-timeout-ms=60000
nostr.websocket.poll-interval-ms=500
```
If a relay response is not received before the timeout elapses, the client logs the failure, closes the WebSocket session, and returns an empty list of events.

## Retry behavior
`SpringWebSocketClient` leverages Spring Retry so that failed send operations are retried up to three times with an exponential backoff starting at 500 ms.

## Expiration events (NIP-40)
The `ExpirationEventExample` demonstrates how to build a NIP-40 expiration event with `GenericEvent` and send it using both the `StandardWebSocketClient` and the `SpringWebSocketClient`:

```java
BaseTag expirationTag = new GenericTag("expiration",
        new ElementAttribute("param0", String.valueOf(expiration)));
GenericEvent event = new GenericEvent(identity.getPublicKey(), Kind.TEXT_NOTE,
        List.of(expirationTag),
        "This message will expire at the specified timestamp and be deleted by relays.\n");
identity.sign(event);
```

## Sending text events with NostrSpringWebSocketClient
The `SpringClientTextEventExample` demonstrates using the `NIP01` helper class to
publish a simple text note via `NostrSpringWebSocketClient`:

```java
Identity sender = Identity.generateRandomIdentity();
NIP01 client = new NIP01(sender);
client.setRelays(Map.of("local", "ws://localhost:5555"));
client.createTextNoteEvent("Hello from NostrSpringWebSocketClient!\n")
      .signAndSend();
```

## Creating custom events and tags
Custom tag types can be introduced without modifying existing core code by
registering them with the `TagRegistry`. The registry maps tag codes to factory
functions responsible for creating concrete `BaseTag` implementations from a
`GenericTag` representation.
