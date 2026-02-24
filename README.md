# nostr-java
[![CI](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml)
[![CI Matrix: docker + no-docker](https://img.shields.io/badge/CI%20Matrix-docker%20%2B%20no--docker-blue)](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/tcheeric/nostr-java/branch/main/graph/badge.svg)](https://codecov.io/gh/tcheeric/nostr-java)
[![GitHub release](https://img.shields.io/github/v/release/tcheeric/nostr-java)](https://github.com/tcheeric/nostr-java/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Qodana](https://github.com/tcheeric/nostr-java/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/tcheeric/nostr-java/actions/workflows/qodana_code_quality.yml)

`nostr-java` is a Java SDK for the [Nostr](https://github.com/nostr-protocol/nips) protocol. It provides utilities for creating, signing and publishing Nostr events to relays.

## Requirements
- Maven
- Java 21+

See [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md) for installation and usage instructions.

## Quick Start

```java
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
```

## Module Architecture

4 modules with a strict dependency chain:

```
nostr-java-core → nostr-java-event → nostr-java-identity → nostr-java-client
```

- **nostr-java-core** — Foundation utilities, BIP-340 Schnorr cryptography, Bech32 encoding, hex conversion
- **nostr-java-event** — `GenericEvent`, `GenericTag`, `Kinds` constants, `EventFilter` builder, messages, JSON serialization
- **nostr-java-identity** — `Identity` key management, event signing, NIP-04/NIP-44 encryption
- **nostr-java-client** — `NostrRelayClient` WebSocket client with retry, Virtual Threads, and async APIs

## Running Tests

- Full test suite (requires Docker for Testcontainers ITs):

  `mvn -q verify`

- Without Docker (skips Testcontainers-based integration tests via profile):

  `mvn -q -Pno-docker verify`

## Troubleshooting

For diagnosing relay send issues and capturing failure details, see the how-to guide: [docs/howto/diagnostics.md](docs/howto/diagnostics.md).

## Documentation

- Docs index: [docs/README.md](docs/README.md) — quick entry point to all guides and references.
- Getting started: [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md) — install via Maven/Gradle and build from source.
- API how-to: [docs/howto/use-nostr-java-api.md](docs/howto/use-nostr-java-api.md) — create, sign, and publish events.
- Streaming subscriptions: [docs/howto/streaming-subscriptions.md](docs/howto/streaming-subscriptions.md) — open and manage long-lived, non-blocking subscriptions.
- Custom events: [docs/howto/custom-events.md](docs/howto/custom-events.md) — working with custom event kinds.
- API reference: [docs/reference/nostr-java-api.md](docs/reference/nostr-java-api.md) — classes, key methods, and short examples.
- Events and tags: [docs/explanation/extending-events.md](docs/explanation/extending-events.md) — in-depth guide to GenericEvent and GenericTag.
- Architecture: [docs/explanation/architecture.md](docs/explanation/architecture.md) — module design and data flow.
- Codebase overview: [docs/CODEBASE_OVERVIEW.md](docs/CODEBASE_OVERVIEW.md) — layout, testing, and contribution workflow.
- Operations: [docs/operations/README.md](docs/operations/README.md) — logging, metrics, configuration, diagnostics.

## Features

- **Minimal API surface** — one event class (`GenericEvent`), one tag class (`GenericTag`), ~40 total classes
- **Protocol-aligned** — kinds are integers, tags are string arrays, no library-imposed type hierarchy
- **Virtual Thread concurrency** — relay I/O and listener dispatch on Java 21 Virtual Threads
- **Async APIs** — `connectAsync()`, `sendAsync()`, `subscribeAsync()` via `CompletableFuture`
- **Reliable connectivity** — Spring Retry, typed `RelayTimeoutException`, connection state tracking
- **NIP-04/NIP-44 encryption** — legacy and modern message encryption
- **BIP-340 Schnorr signatures** — event signing and verification
- **Well-documented** — architecture guides, how-to guides, and API reference

## v2.0.0 Highlights

- Simplified from 9 modules (~180 classes) to 4 modules (~40 classes)
- `GenericEvent` is the sole event class for all kinds — no subclasses
- `GenericTag` stores tags as `code` + `List<String>` — no `ElementAttribute`, no `TagRegistry`
- `Kinds` utility replaces the `Kind` enum — any integer is valid
- `EventFilter` builder replaces 14 thin filter wrapper classes
- `NostrRelayClient` with Virtual Thread dispatch and async APIs
- `RelayTimeoutException` replaces silent empty-list timeout returns
- `java.util.HexFormat` replaces hand-rolled hex encoding

See [CHANGELOG.md](CHANGELOG.md) for the full list of changes.

## NIP Support

The library is NIP-agnostic by design. Any current or future NIP can be implemented using `GenericEvent.builder().kind(kindNumber)` with appropriate tags via `GenericTag.of(code, params...)` — no library updates required. The `Kinds` utility class provides named constants for commonly used kind values.

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Coding standards and conventions
- Pull request guidelines
- Testing requirements

For architectural guidance, see [docs/explanation/architecture.md](docs/explanation/architecture.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
