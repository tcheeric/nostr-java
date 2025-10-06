# nostr-java
[![CI](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/tcheeric/nostr-java/branch/main/graph/badge.svg)](https://codecov.io/gh/tcheeric/nostr-java)
[![GitHub release](https://img.shields.io/github/v/release/tcheeric/nostr-java)](https://github.com/tcheeric/nostr-java/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Qodana](https://github.com/tcheeric/nostr-java/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/tcheeric/nostr-java/actions/workflows/qodana_code_quality.yml)

`nostr-java` is a Java SDK for the [Nostr](https://github.com/nostr-protocol/nips) protocol. It provides utilities for creating, signing and publishing Nostr events to relays.

## Requirements
- Maven
- Java 21+

See [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md) for installation and usage instructions.

## Documentation

- Docs index: [docs/README.md](docs/README.md) — quick entry point to all guides and references.
- Getting started: [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md) — install via Maven/Gradle and build from source.
- API how‑to: [docs/howto/use-nostr-java-api.md](docs/howto/use-nostr-java-api.md) — create, sign, and publish basic events.
- Streaming subscriptions: [docs/howto/streaming-subscriptions.md](docs/howto/streaming-subscriptions.md) — open and manage long‑lived, non‑blocking subscriptions.
- Custom events how‑to: [docs/howto/custom-events.md](docs/howto/custom-events.md) — define, sign, and send custom event types.
- API reference: [docs/reference/nostr-java-api.md](docs/reference/nostr-java-api.md) — classes, key methods, and short examples.
- Extending events: [docs/explanation/extending-events.md](docs/explanation/extending-events.md) — guidance for extending the event model.
- Codebase overview and contributing: [docs/CODEBASE_OVERVIEW.md](docs/CODEBASE_OVERVIEW.md) — layout, testing, and contribution workflow.

## Examples

Examples are located in the [`nostr-java-examples`](./nostr-java-examples) module. See the [API Examples Guide](docs/howto/api-examples.md) for detailed walkthroughs.

### Key Examples

- [`NostrApiExamples`](nostr-java-examples/src/main/java/nostr/examples/NostrApiExamples.java) – Comprehensive examples covering 13+ use cases including text notes, encrypted DMs, reactions, channels, and more. See the [guide](docs/howto/api-examples.md) for details.

- [`SpringSubscriptionExample`](nostr-java-examples/src/main/java/nostr/examples/SpringSubscriptionExample.java) – Shows how to open a non-blocking `NostrSpringWebSocketClient` subscription and close it after a fixed duration.
 
## Supported NIPs
The API currently implements the following [NIPs](https://github.com/nostr-protocol/nips):
- [NIP-1](https://github.com/nostr-protocol/nips/blob/master/01.md) - Basic protocol flow description
- [NIP-2](https://github.com/nostr-protocol/nips/blob/master/02.md) - Follow List
- [NIP-3](https://github.com/nostr-protocol/nips/blob/master/03.md) - OpenTimestamps Attestations for Events
- [NIP-4](https://github.com/nostr-protocol/nips/blob/master/04.md) - Encrypted Direct Message
- [NIP-5](https://github.com/nostr-protocol/nips/blob/master/05.md) - Mapping Nostr keys to DNS-based internet identifiers
- [NIP-8](https://github.com/nostr-protocol/nips/blob/master/08.md) - Handling Mentions
- [NIP-9](https://github.com/nostr-protocol/nips/blob/master/09.md) - Event Deletion Request
- [NIP-12](https://github.com/nostr-protocol/nips/blob/master/12.md) - Generic Tag Queries
- [NIP-14](https://github.com/nostr-protocol/nips/blob/master/14.md) - Subject tag in Text events
- [NIP-15](https://github.com/nostr-protocol/nips/blob/master/15.md) - Nostr Marketplace
- [NIP-20](https://github.com/nostr-protocol/nips/blob/master/20.md) - Command Results
- [NIP-23](https://github.com/nostr-protocol/nips/blob/master/23.md) - Long-form Content
- [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md) - Reactions
- [NIP-28](https://github.com/nostr-protocol/nips/blob/master/28.md) - Public Chat
- [NIP-30](https://github.com/nostr-protocol/nips/blob/master/30.md) - Custom Emoji
- [NIP-32](https://github.com/nostr-protocol/nips/blob/master/32.md) - Labeling
- [NIP-40](https://github.com/nostr-protocol/nips/blob/master/40.md) - Expiration Timestamp
- [NIP-42](https://github.com/nostr-protocol/nips/blob/master/42.md) - Authentication of clients to relays
- [NIP-44](https://github.com/nostr-protocol/nips/blob/master/44.md) - Encrypted Payloads (Versioned)
- [NIP-46](https://github.com/nostr-protocol/nips/blob/master/46.md) - Nostr Remote Signing
- [NIP-57](https://github.com/nostr-protocol/nips/blob/master/57.md) - Lightning Zaps
- [NIP-60](https://github.com/nostr-protocol/nips/blob/master/60.md) - Cashu Wallets
- [NIP-61](https://github.com/nostr-protocol/nips/blob/master/61.md) - Nutzaps
- [NIP-99](https://github.com/nostr-protocol/nips/blob/master/99.md) - Classified Listings
