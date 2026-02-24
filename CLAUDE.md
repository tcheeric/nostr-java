# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`nostr-java` is a Java SDK for the Nostr protocol. It provides utilities for creating, signing, and publishing Nostr events to relays.

- **Language**: Java 21+
- **Build Tool**: Maven
- **Architecture**: Multi-module Maven project with 4 modules

## Module Architecture

The codebase follows a layered dependency structure:

1. **nostr-java-core** – Foundation utilities and BIP340 Schnorr cryptography (packages: `nostr.util`, `nostr.crypto`)
2. **nostr-java-event** – Event model, tags, filters, serialization, and base types (packages: `nostr.event`, `nostr.base`)
3. **nostr-java-identity** – Identity/key management and encryption (packages: `nostr.id`, `nostr.encryption`)
4. **nostr-java-client** – WebSocket relay client with Spring support (packages: `nostr.client`)

**Dependency chain**: `core → event → identity → client`

**Key principle**: Lower-level modules cannot depend on higher-level ones. When adding features, place code at the lowest appropriate level.

## Common Development Commands

### Building and Testing

```bash
# Run all unit tests (no Docker required)
mvn clean test

# Run integration tests (requires Docker for Testcontainers)
mvn clean verify

# Install artifacts without tests
mvn install -Dmaven.test.skip=true

# Run a specific test class
mvn -q test -Dtest=GenericEventBuilderTest
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

- **GenericEvent** is the single event class for all Nostr event kinds
- Events use `int kind` values; common kinds defined as constants in `Kinds` utility class
- Events can be built using:
  - Direct constructors with `PublicKey` and `int kind`
  - Static `GenericEvent.builder()` for flexible construction
- All events must be signed before sending to relays

### Tag System

- **GenericTag** is the single tag class, holding `code` + `List<String> params`
- Factory: `GenericTag.of("p", pubkeyHex, relayUrl)` or `BaseTag.create("e", eventId)`
- Serialized as JSON arrays: `["code", "param0", "param1", ...]`

### Filter System

- **EventFilter** with builder pattern for composable query filters
- Supports `ids`, `authors`, `kinds`, `since`, `until`, `limit`, and tag filters via `.addTagFilter()`
- **Filters** holds a `List<EventFilter>` for REQ messages

### Client Architecture

- **NostrRelayClient** – Blocking send with configurable timeout, streaming subscribe, Spring Retry (3 attempts, exponential backoff)
- Throws `RelayTimeoutException` on timeout (instead of returning empty list)
- Tracks `ConnectionState` (CONNECTING, CONNECTED, RECONNECTING, CLOSED)

Configuration properties:
- `nostr.websocket.await-timeout-ms=60000`
- `nostr.websocket.max-idle-timeout-ms=3600000`
- `nostr.websocket.max-events-per-request=10000`

### Identity and Signing

- `Identity` class manages key pairs
- Events implement `ISignable` interface
- Signing uses Schnorr signatures (BIP340)
- Public keys use Bech32 encoding (npub prefix)

## Testing Strategy

- **Unit tests** (`*Test.java`): No external dependencies, use mocks
- **Integration tests** (`*IT.java`): Use Testcontainers to start `nostr-rs-relay`
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

- **BOM**: `nostr-java-bom` manages all dependency versions
- Root `pom.xml` includes temporary module version overrides until next BOM release
- Never add version numbers to dependencies in child modules – let the BOM manage versions

## Common Patterns and Gotchas

### Event Building
```java
// Using builder
GenericEvent event = GenericEvent.builder()
    .kind(Kinds.TEXT_NOTE)
    .content("content")
    .pubKey(publicKey)
    .build();

// Using constructor
GenericEvent event = new GenericEvent(pubKey, Kinds.TEXT_NOTE);
```

### Tags
```java
// Create tags
GenericTag tag = GenericTag.of("p", pubkeyHex, "wss://relay.example.com");
GenericTag hashtag = GenericTag.of("t", "nostr");
```

### Filters
```java
// Build a filter
EventFilter filter = EventFilter.builder()
    .kind(Kinds.TEXT_NOTE)
    .author(pubkeyHex)
    .since(timestamp)
    .limit(100)
    .build();
```

### WebSocket Sessions
Spring WebSocket client maintains persistent connections. Always close subscriptions properly to avoid resource leaks.

### Use Virtual Threads for Concurrency

This project uses Java 21 Virtual Threads (Project Loom) for efficient concurrency. Virtual Threads are enabled by default via `spring.threads.virtual.enabled=true` in the gateway. **Always prefer Virtual Threads over platform threads for I/O-bound work.**

#### When to Use Virtual Threads

| Scenario | Use Virtual Threads? | Pattern |
|----------|---------------------|---------|
| Mint API calls (mint, melt, swap) | Yes | `CompletableFuture` with VT executor |
| Database queries (gateway) | Yes | Parallel queries with VT executor |
| Nostr relay operations | Yes | Parallel publish/fetch across relays |
| Nostrdb queries | Yes | VT handles LMDB blocking efficiently |
| SSE event delivery | Yes | Spring WebFlux handles this automatically |
| File I/O (wallet storage) | Yes | VT handles blocking efficiently |
| Cryptographic operations (signing) | No | CPU-bound, use parallel streams |
| Quick in-memory operations | No | Overhead not justified |

#### Patterns and Examples

**1. Parallel I/O with CompletableFuture and VT Executor (Preferred)**

Use when you need results from multiple independent I/O operations:

```java
import java.util.concurrent.*;

// Parallel mint API queries with Virtual Threads
private List<QuoteStatus> fetchQuoteStatuses(List<String> quoteIds, MintClient mintClient) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        List<CompletableFuture<QuoteStatus>> futures = quoteIds.stream()
            .map(id -> CompletableFuture.supplyAsync(() -> {
                return mintClient.getMintQuoteStatus(id);
            }, executor))
            .toList();

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
            .map(f -> f.getNow(null))
            .filter(Objects::nonNull)
            .toList();
    }
}
```

**2. Parallel Nostr Relay Operations**

Use when publishing or fetching from multiple relays:

```java
// Publish event to multiple relays in parallel
private Map<String, Boolean> publishToRelays(NostrEvent event, List<String> relayUrls) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        Map<String, CompletableFuture<Boolean>> futures = new ConcurrentHashMap<>();

        for (String relayUrl : relayUrls) {
            futures.put(relayUrl, CompletableFuture.supplyAsync(() -> {
                try {
                    return nostrClient.publish(relayUrl, event);
                } catch (Exception e) {
                    log.warn("Failed to publish to {}: {}", relayUrl, e.getMessage());
                    return false;
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();

        return futures.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getNow(false)));
    }
}
```

**3. Fire-and-Forget with @Async**

Use for event handlers that shouldn't block the caller:

```java
@Async  // Runs on VT via AsyncConfig
@EventListener
public void onWalletUpdate(WalletUpdateEvent event) {
    // Sync to Nostr in background
    nostrSyncService.syncWalletState(event.getWalletId());
}
```

**4. Parallel Database Queries in Gateway**

Use for fetching related entities:

```java
// Parallel fetch of user data from multiple tables
private UserProfile loadFullProfile(String pubkey) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        var profileFuture = CompletableFuture.supplyAsync(
            () -> profileRepository.findByPubkey(pubkey), executor);
        var walletsFuture = CompletableFuture.supplyAsync(
            () -> walletRepository.findByOwnerPubkey(pubkey), executor);
        var settingsFuture = CompletableFuture.supplyAsync(
            () -> settingsRepository.findByPubkey(pubkey), executor);

        CompletableFuture.allOf(profileFuture, walletsFuture, settingsFuture).join();

        return UserProfile.builder()
            .profile(profileFuture.getNow(null))
            .wallets(walletsFuture.getNow(List.of()))
            .settings(settingsFuture.getNow(null))
            .build();
    }
}
```

**5. Parallel Mint Swaps**

Use when swapping tokens across multiple mints:

```java
// Parallel swaps when consolidating tokens from multiple mints
private List<SwapResult> parallelSwap(List<SwapRequest> requests) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        List<CompletableFuture<SwapResult>> futures = requests.stream()
            .map(req -> CompletableFuture.supplyAsync(() -> {
                try {
                    return mintClient.swap(req.getMintUrl(), req.getProofs(), req.getOutputs());
                } catch (Exception e) {
                    return SwapResult.failed(req.getMintUrl(), e.getMessage());
                }
            }, executor))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
            .map(f -> f.getNow(SwapResult.failed("unknown", "timeout")))
            .toList();
    }
}
```

#### Anti-Patterns to Avoid

**❌ Sequential I/O in loops when items are independent:**
```java
// BAD: Sequential blocking calls
for (String mintUrl : mintUrls) {
    var keysets = mintClient.getKeysets(mintUrl);  // Blocks
    // ...
}
```

**❌ Using synchronized for I/O operations (causes VT pinning):**
```java
// BAD: Pins virtual thread to carrier thread
synchronized (lock) {
    database.query(...);  // Pinned during entire I/O!
}

// GOOD: Use ReentrantLock instead
private final ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    database.query(...);  // VT can unmount during I/O
} finally {
    lock.unlock();
}
```

**❌ Creating platform thread pools for I/O work:**
```java
// BAD: Wastes platform threads on I/O
ExecutorService pool = Executors.newFixedThreadPool(10);

// GOOD: Use virtual thread executor
ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
```

**❌ Blocking the SSE thread:**
```java
// BAD: Blocks SSE connection during mint call
@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Event> events() {
    var status = mintClient.checkStatus(...);  // Blocks!
    return Flux.just(Event.of(status));
}

// GOOD: Use reactive operators
@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Event> events() {
    return Mono.fromCallable(() -> mintClient.checkStatus(...))
        .subscribeOn(Schedulers.boundedElastic())
        .map(Event::of)
        .flux();
}
```

#### VT Configuration Reference

| Component | Configuration | Purpose |
|-----------|--------------|---------|
| Spring Boot | `spring.threads.virtual.enabled=true` | Use VT for request handling |
| Tomcat | `server.tomcat.threads.max=50` | Reduced (VTs handle concurrency) |
| `@Async` | `AsyncConfig` bean | VT executor for async methods |
| HTTP Client | `JdkClientHttpRequestFactory` | VT-friendly HTTP client |

#### Debugging Virtual Threads

```bash
# Enable VT debugging output
-Djdk.tracePinnedThreads=full

# Check for pinning in logs
grep -i "pinned" logs/application.log

# Monitor virtual thread count
jcmd <pid> Thread.dump_to_file -format=json threads.json
```
