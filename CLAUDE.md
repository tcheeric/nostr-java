# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`nostr-java` is a Java SDK for the Nostr protocol. It provides utilities for creating, signing, and publishing Nostr events to relays. The project implements 20+ Nostr Implementation Possibilities (NIPs).

- **Language**: Java 21+
- **Build Tool**: Maven
- **Architecture**: Multi-module Maven project with 9 modules

## Module Architecture

The codebase follows a layered dependency structure. Understanding this hierarchy is essential for making changes:

1. **nostr-java-util** – Foundation utilities (no dependencies on other modules)
2. **nostr-java-crypto** – BIP340 Schnorr signatures (depends on util)
3. **nostr-java-base** – Common model classes (depends on crypto, util)
4. **nostr-java-event** – Event and tag definitions (depends on base, crypto, util)
5. **nostr-java-id** – Identity and key handling (depends on base, crypto)
6. **nostr-java-encryption** – Message encryption (depends on base, crypto, id)
7. **nostr-java-client** – WebSocket relay client (depends on event, base)
8. **nostr-java-api** – High-level API (depends on all above)
9. **nostr-java-examples** – Sample applications (depends on api)

**Key principle**: Lower-level modules cannot depend on higher-level ones. When adding features, place code at the lowest appropriate level.

## Common Development Commands

### Building and Testing

```bash
# Run all unit tests (no Docker required)
mvn clean test

# Run integration tests (requires Docker for Testcontainers)
mvn clean verify

# Run integration tests with verbose output
mvn -q verify

# Install artifacts without tests
mvn install -Dmaven.test.skip=true

# Run a specific test class
mvn -q test -Dtest=GenericEventBuilderTest

# Run a specific test method
mvn -q test -Dtest=GenericEventBuilderTest#testSpecificMethod
```

### Code Quality

```bash
# Verify code quality and run all checks
mvn -q verify

# Generate code coverage report (Jacoco)
mvn verify
# Reports: target/site/jacoco/index.html in each module
```

## Key Architectural Patterns

### Event System

- **GenericEvent** (`nostr-java-event/src/main/java/nostr/event/impl/GenericEvent.java`) is the core event class
- Events can be built using:
  - Direct constructors with `PublicKey` and `Kind`/`Integer`
  - Static `GenericEvent.builder()` for flexible construction
- All events must be signed before sending to relays
- Events support both NIP-defined kinds (via `Kind` enum) and custom kinds (via `Integer`)

### Client Architecture

Two WebSocket client implementations:

1. **StandardWebSocketClient** – Blocking, waits for relay responses with configurable timeout
2. **NostrSpringWebSocketClient** – Non-blocking with Spring WebSocket and retry support (3 retries, exponential backoff from 500ms)

Configuration properties:
- `nostr.websocket.await-timeout-ms=60000`
- `nostr.websocket.poll-interval-ms=500`

### Tag System

- Tags are represented by `BaseTag` and subclasses
- Custom tags can be registered via `TagRegistry`
- Serialization/deserialization handled by Jackson with custom serializers in `nostr.event.json.serializer`

### Identity and Signing

- `Identity` class manages key pairs
- Events implement `ISignable` interface
- Signing uses Schnorr signatures (BIP340)
- Public keys use Bech32 encoding (npub prefix)

## NIPs Implementation

The codebase implements NIPs through dedicated classes in `nostr-java-api`:
- NIP classes (e.g., `NIP01`, `NIP04`, `NIP25`) provide builder methods and utilities
- Event implementations in `nostr-java-event/src/main/java/nostr/event/impl/`
- Refer to `.github/copilot-instructions.md` for the full NIP specification links

When implementing new NIP support:
1. Add event class in `nostr-java-event` if needed
2. Create NIP helper class in `nostr-java-api`
3. Add tests in both modules
4. Update README.md with NIP reference
5. Add example in `nostr-java-examples`

## Testing Strategy

- **Unit tests** (`*Test.java`): No external dependencies, use mocks
- **Integration tests** (`*IT.java`): Use Testcontainers to start `nostr-rs-relay`
- Relay container image can be overridden in `src/test/resources/relay-container.properties`
- Integration tests may be retried once on failure (configured in failsafe plugin)

## Code Standards

- **Commit messages**: Must follow conventional commits format: `type(scope): description`
  - Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
  - See `commit_instructions.md` for full guidelines
- **PR target**: All PRs should target the `develop` branch
- **Code formatting**: Google Java Format (enforced by CI)
- **Test coverage**: Jacoco generates reports (enforced by CI)
- **Required**: All changes must include unit tests and documentation updates

## Dependency Management

- **BOM**: `nostr-java-bom` (version 1.1.1) manages all dependency versions
- Root `pom.xml` includes temporary module version overrides until next BOM release
- Never add version numbers to dependencies in child modules – let the BOM manage versions

## Documentation

Comprehensive documentation in `docs/`:
- `docs/GETTING_STARTED.md` – Installation and setup
- `docs/howto/use-nostr-java-api.md` – API usage guide
- `docs/howto/streaming-subscriptions.md` – Subscription management
- `docs/howto/custom-events.md` – Creating custom event types
- `docs/reference/nostr-java-api.md` – API reference
- `docs/CODEBASE_OVERVIEW.md` – Module layout and build instructions

## Common Patterns and Gotchas

### Event Building
```java
// Using builder for custom kinds
GenericEvent event = GenericEvent.builder()
    .kind(customKindInteger)
    .content("content")
    .pubKey(publicKey)
    .build();

// Using constructor for standard kinds
GenericEvent event = new GenericEvent(pubKey, Kind.TEXT_NOTE);
```

### Signing and Sending
```java
// Sign and send pattern
EventNostr nostr = new NIP01(identity);
nostr.createTextNote("Hello Nostr!")
    .sign()
    .send(relays);
```

### Custom Tags
Register custom tags in `TagRegistry` before deserializing events that contain them.

### WebSocket Sessions
Spring WebSocket client maintains persistent connections. Always close subscriptions properly to avoid resource leaks.
