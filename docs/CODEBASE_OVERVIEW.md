# Codebase Overview

Navigation: [Docs index](README.md) · [Getting started](GETTING_STARTED.md) · [API how‑to](howto/use-nostr-java-api.md) · [API reference](reference/nostr-java-api.md)

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

## Examples

For practical usage examples, see:
- [API Examples Guide](howto/api-examples.md) – Comprehensive examples covering 13+ use cases
- [Custom Events How-To](howto/custom-events.md) – Creating custom event types
- [Streaming Subscriptions](howto/streaming-subscriptions.md) – Long-lived subscriptions
- [Extending Events](explanation/extending-events.md) – Extending the event model with custom tags

Example code is also available in the [`nostr-java-examples`](../nostr-java-examples) module.

## Contributing

Before submitting changes:

1. **Run verification**: `./mvnw -q verify` – ensure all tests pass
2. **Follow code style**: Use clear, descriptive names and remove unused imports
3. **Write tests**: Include unit tests and update relevant documentation
4. **Follow commit conventions**: Use conventional commits (see [CONTRIBUTING.md](../CONTRIBUTING.md))
5. **Submit PRs to develop branch**: All pull requests should target the `develop` branch

For detailed contribution guidelines, see [CONTRIBUTING.md](../CONTRIBUTING.md).
