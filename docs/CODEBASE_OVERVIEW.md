# Codebase Overview

Navigation: [Docs index](README.md) · [Getting started](GETTING_STARTED.md) · [API how-to](howto/use-nostr-java-api.md) · [API reference](reference/nostr-java-api.md)

This document provides an overview of the project structure and instructions for building and testing the modules.

## Module layout

nostr-java 2.0 has 4 modules with a clear dependency chain:

```
nostr-java-core → nostr-java-event → nostr-java-identity → nostr-java-client
```

- **nostr-java-core** — Foundation utilities, BIP-340 Schnorr cryptography, Bech32 encoding, hex conversion (`java.util.HexFormat`), validators, and exception hierarchy. No dependencies on other project modules.
- **nostr-java-event** — `GenericEvent` (sole event class), `GenericTag` (sole tag class with `List<String>` params), `Kinds` constants, `EventFilter` builder, relay messages, JSON serialization, `PublicKey`/`PrivateKey`/`Signature` value objects, and `ISignable` contract.
- **nostr-java-identity** — `Identity` key management, event signing, and NIP-04/NIP-44 message encryption (`MessageCipher04`, `MessageCipher44`).
- **nostr-java-client** — `NostrRelayClient` WebSocket client with Spring Retry, Virtual Thread dispatch, async APIs (`connectAsync`, `sendAsync`, `subscribeAsync`), and connection state tracking.

## Building and testing

The project is built with Maven. Unit tests do not require a running relay, while integration tests use Testcontainers to start a relay in Docker.

### Unit-tested build
```bash
mvn clean test
mvn install -Dmaven.test.skip=true
```

### Integration-tested build (requires Docker)
```bash
mvn clean verify
```
Integration tests start a relay container automatically. The image used can be overridden in `src/test/resources/relay-container.properties` by setting `relay.container.image=<image>`.

## WebSocket configuration

`NostrRelayClient` waits for relay responses when sending messages. Configuration properties (values in milliseconds):

```
nostr.websocket.await-timeout-ms=60000
nostr.websocket.max-idle-timeout-ms=3600000
nostr.websocket.max-text-message-buffer-size=1048576
nostr.websocket.max-binary-message-buffer-size=1048576
```

If a relay response is not received before the timeout elapses, the client throws a `RelayTimeoutException`. The maximum number of events accumulated per request defaults to 10,000 to prevent unbounded memory growth.

## Retry behavior

`NostrRelayClient` uses Spring Retry (`@NostrRetryable`) so that failed send and subscribe operations are retried up to three times with exponential backoff starting at 500 ms.

## Virtual Threads

The client dispatches relay subscription callbacks on Virtual Threads, so expensive listener logic does not block inbound WebSocket I/O. Async APIs (`connectAsync`, `sendAsync`, `subscribeAsync`) also run on Virtual Threads via named thread factories.

## Examples

For practical usage examples, see:
- [API how-to](howto/use-nostr-java-api.md) — Create, sign, and send events
- [Custom events](howto/custom-events.md) — Working with custom event kinds
- [Streaming subscriptions](howto/streaming-subscriptions.md) — Long-lived subscriptions

## Contributing

Before submitting changes:

1. **Run verification**: `mvn -q verify` — ensure all tests pass
2. **Follow code style**: Use clear, descriptive names and remove unused imports
3. **Write tests**: Include unit tests and update relevant documentation
4. **Follow commit conventions**: Use conventional commits (see [CONTRIBUTING.md](../CONTRIBUTING.md))
5. **Submit PRs to develop branch**: All pull requests should target the `develop` branch

For detailed contribution guidelines, see [CONTRIBUTING.md](../CONTRIBUTING.md).
