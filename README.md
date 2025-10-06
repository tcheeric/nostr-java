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
 
## Features

✅ **Clean Architecture** - Modular design following SOLID principles
✅ **Comprehensive NIP Support** - 25 NIPs implemented covering core protocol, encryption, payments, and more
✅ **Type-Safe API** - Strongly-typed events, tags, and messages with builder patterns
✅ **Non-Blocking Subscriptions** - Spring WebSocket client with reactive streaming support
✅ **Well-Documented** - Extensive JavaDoc, architecture guides, and code examples
✅ **Production-Ready** - High test coverage, CI/CD pipeline, code quality checks

## Recent Improvements (v0.6.2)

🎯 **Refactoring for Clean Code**
- Extracted god classes into focused utility classes (EventValidator, EventSerializer, EventTypeChecker)
- Improved Single Responsibility Principle compliance
- Enhanced logging practices following Clean Code guidelines
- Grade improvement: B → A-

📚 **Documentation Overhaul**
- Comprehensive architecture documentation with design patterns
- Complete JavaDoc coverage for core APIs
- Step-by-step guides for extending events and adding NIPs
- 15+ code examples throughout documentation

🔧 **API Improvements**
- Simplified NIP01 facade (sender configured at construction)
- BOM migration for consistent dependency management
- Deprecated methods marked for removal in 1.0.0
- Enhanced error messages with context

See [docs/explanation/architecture.md](docs/explanation/architecture.md) for detailed architecture overview.

## Supported NIPs

**25 NIPs implemented** - comprehensive coverage of core protocol, security, and advanced features.

### NIP Compliance Matrix

| Category | NIP | Description | Status |
|----------|-----|-------------|--------|
| **Core Protocol** | [NIP-01](https://github.com/nostr-protocol/nips/blob/master/01.md) | Basic protocol flow | ✅ Complete |
| | [NIP-02](https://github.com/nostr-protocol/nips/blob/master/02.md) | Follow List | ✅ Complete |
| | [NIP-12](https://github.com/nostr-protocol/nips/blob/master/12.md) | Generic Tag Queries | ✅ Complete |
| | [NIP-19](https://github.com/nostr-protocol/nips/blob/master/19.md) | Bech32 encoding | ✅ Complete |
| | [NIP-20](https://github.com/nostr-protocol/nips/blob/master/20.md) | Command Results | ✅ Complete |
| **Security & Identity** | [NIP-05](https://github.com/nostr-protocol/nips/blob/master/05.md) | DNS-based identifiers | ✅ Complete |
| | [NIP-42](https://github.com/nostr-protocol/nips/blob/master/42.md) | Client authentication | ✅ Complete |
| | [NIP-46](https://github.com/nostr-protocol/nips/blob/master/46.md) | Remote signing | ✅ Complete |
| **Encryption** | [NIP-04](https://github.com/nostr-protocol/nips/blob/master/04.md) | Encrypted DMs | ✅ Complete |
| | [NIP-44](https://github.com/nostr-protocol/nips/blob/master/44.md) | Versioned encryption | ✅ Complete |
| **Content Types** | [NIP-08](https://github.com/nostr-protocol/nips/blob/master/08.md) | Handling Mentions | ✅ Complete |
| | [NIP-09](https://github.com/nostr-protocol/nips/blob/master/09.md) | Event Deletion | ✅ Complete |
| | [NIP-14](https://github.com/nostr-protocol/nips/blob/master/14.md) | Subject tags | ✅ Complete |
| | [NIP-23](https://github.com/nostr-protocol/nips/blob/master/23.md) | Long-form content | ✅ Complete |
| | [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md) | Reactions | ✅ Complete |
| | [NIP-28](https://github.com/nostr-protocol/nips/blob/master/28.md) | Public Chat | ✅ Complete |
| | [NIP-30](https://github.com/nostr-protocol/nips/blob/master/30.md) | Custom Emoji | ✅ Complete |
| | [NIP-32](https://github.com/nostr-protocol/nips/blob/master/32.md) | Labeling | ✅ Complete |
| | [NIP-52](https://github.com/nostr-protocol/nips/blob/master/52.md) | Calendar Events | ✅ Complete |
| **Commerce & Payments** | [NIP-15](https://github.com/nostr-protocol/nips/blob/master/15.md) | Marketplace | ✅ Complete |
| | [NIP-57](https://github.com/nostr-protocol/nips/blob/master/57.md) | Lightning Zaps | ✅ Complete |
| | [NIP-60](https://github.com/nostr-protocol/nips/blob/master/60.md) | Cashu Wallets | ✅ Complete |
| | [NIP-61](https://github.com/nostr-protocol/nips/blob/master/61.md) | Nutzaps | ✅ Complete |
| | [NIP-99](https://github.com/nostr-protocol/nips/blob/master/99.md) | Classified Listings | ✅ Complete |
| **Utilities** | [NIP-03](https://github.com/nostr-protocol/nips/blob/master/03.md) | OpenTimestamps | ✅ Complete |
| | [NIP-40](https://github.com/nostr-protocol/nips/blob/master/40.md) | Expiration Timestamp | ✅ Complete |

**Coverage:** 25/100+ NIPs (core protocol + most commonly used extensions)

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Coding standards and conventions
- How to add new NIPs
- Pull request guidelines
- Testing requirements

For architectural guidance, see [docs/explanation/architecture.md](docs/explanation/architecture.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
