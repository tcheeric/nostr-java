# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by Keep a Changelog, and this project adheres to semantic versioning once 1.0.0 is released.

## [Unreleased]

### Removed
- Dead code cleanup — deleted unused classes: `IContent`, `JsonContent`, `Reaction` enum, `Response`, `Nip05Content`, `Nip05ContentDecoder`, `BaseAuthMessage`, `GenericMessage`, `IKey`, `GenericEventConverter`, `GenericEventTypeClassifier`, `GenericEventDecoder`, `FiltersDecoder`, `BaseTagDecoder`, `GenericEventValidator`, `GenericEventSerializer`, `GenericEventUpdater`, `GenericTagQuery`, `HttpClientProvider`, `DefaultHttpClientProvider`.
- `testAuthMessage` test and `GenericEventSupportTest` removed (tested deleted classes).
- `createGenericTagQuery()` removed from `EntityFactory` (only consumer of deleted `GenericTagQuery`).

### Changed
- `RelayAuthenticationMessage` and `CanonicalAuthenticationMessage` now extend `BaseMessage` directly (previously extended the now-deleted `BaseAuthMessage`).
- `BaseKey` now directly implements `Serializable` (previously implemented the now-deleted `IKey` interface).
- `Nip05Validator` now creates `HttpClient` instances directly via a `Function<Duration, HttpClient>` factory (previously used deleted `HttpClientProvider`/`DefaultHttpClientProvider` interface).

## [2.0.0] - 2026-02-24

This is a major release that implements the full design simplification described in `docs/developer/SIMPLIFICATION_PROPOSAL.md`, reducing the library from 9 modules with ~180 classes to 4 modules with ~40 classes.

### Added
- `Kinds` utility class with static `int` constants for common Nostr event kinds (`TEXT_NOTE`, `SET_METADATA`, `CONTACT_LIST`, etc.) and range-check methods (`isReplaceable()`, `isEphemeral()`, `isAddressable()`, `isValid()`).
- `GenericTag.of(String code, String... params)` factory method for concise tag creation.
- `GenericTag.toArray()` returning the NIP-01 wire format `["code", "param0", "param1", ...]`.
- `GenericTag` now stores tag values as `List<String>` (replacing `List<ElementAttribute>`), providing direct access via `getParams()`.
- `EventFilter` builder API for composable relay filters: `.kinds()`, `.authors()`, `.since()`, `.until()`, `.addTagFilter()`, `.limit()`, `.ids()`.
- `RelayTimeoutException` — typed exception replacing silent empty-list returns on relay timeout.
- `ConnectionState` enum (`CONNECTING`, `CONNECTED`, `RECONNECTING`, `CLOSED`) for WebSocket connection state tracking.
- `NostrRelayClient` async Virtual Thread APIs: `connectAsync(...)`, `sendAsync(...)`, and `subscribeAsync(...)`.
- `Nip05Validator.validateAsync()` and `Nip05Validator.validateBatch(...)` for parallel NIP-05 validation workloads.
- Spring Retry support (`@NostrRetryable`, `@Recover`) consolidated directly into `NostrRelayClient`.

### Changed
- **Module consolidation** — merged 9 modules into 4:
  - `nostr-java-util` + `nostr-java-crypto` → `nostr-java-core`
  - `nostr-java-base` + `nostr-java-event` → `nostr-java-event`
  - `nostr-java-id` + `nostr-java-encryption` → `nostr-java-identity`
  - `nostr-java-client` (unchanged)
- **`GenericEvent`** is now the sole event class. All 39 concrete event subclasses removed. Events are differentiated by `int kind` instead of Java type.
- **`GenericTag`** is now the sole tag class. All 17 concrete tag subclasses removed. Tags are a simple `code` + `List<String> params`.
- `GenericEvent.kind` changed from `Kind` enum to plain `int`. Builder simplified to `.kind(int)` only.
- `GenericEvent.tags` changed from `List<BaseTag>` to `List<GenericTag>`.
- `GenericEvent` implements `ISignable` directly (no longer extends `BaseEvent`).
- `EventMessage` now references `GenericEvent` directly instead of `IEvent`.
- `IDecoder<T>` type bound changed from `IDecoder<T extends IElement>` to unbounded `IDecoder<T>`.
- `TagDeserializer` now always produces `GenericTag` with `List<String>` params — no registry dispatch.
- `GenericTagSerializer` simplified to output `[code, param0, param1, ...]` directly from `List<String>`.
- `GenericEventDeserializer` simplified — no subclass dispatch to concrete event types.
- `NostrUtil.bytesToHex()` now uses `java.util.HexFormat` instead of hand-rolled hex encoding.
- `NostrUtil.hexToBytes()` family of methods now uses `java.util.HexFormat.parseHex()` — fails fast on invalid hex instead of silently producing corrupt bytes.
- WebSocket client termination detection now uses proper JSON parsing instead of brittle string-prefix matching.
- `StandardWebSocketClient` renamed to `NostrRelayClient`.
- `SpringWebSocketClient` absorbed into `NostrRelayClient` (single client class with retry support).
- Relay subscription callbacks are now dispatched on Virtual Threads to avoid blocking inbound WebSocket processing.
- `DefaultHttpClientProvider` now uses a shared Virtual Thread executor instead of creating a new executor per `HttpClient`.
- All `synchronized` blocks in `NostrRelayClient` replaced with `ReentrantLock` to avoid Virtual Thread pinning.
- Configurable max events per request limit (default 10,000) to prevent unbounded memory accumulation.

### Fixed
- **`GenericTag.getCode()` NPE** — structurally eliminated by removing the dual-path tag architecture. `getCode()` is now a trivial field accessor with zero NPE risk.
- Removed the remaining `synchronized` cleanup block from `NostrRelayClient.send(...)`, using `ReentrantLock` consistently to avoid VT pinning risk.
- Relay timeout now throws `RelayTimeoutException` instead of silently returning an empty list, allowing callers to distinguish "no results" from "timed out".

### Removed
- **`nostr-java-api` module** — all 26 NIP classes (NIP01–NIP99), `EventNostr`, factory classes, client managers, service layer, and configuration classes.
- **`nostr-java-examples` module** — all 6 example classes.
- **39 concrete event subclasses** — `TextNoteEvent`, `DirectMessageEvent`, `ContactListEvent`, `ReactionEvent`, `DeletionEvent`, `EphemeralEvent`, `ReplaceableEvent`, `AddressableEvent`, all Calendar/Marketplace/Channel/NostrConnect events, and more. Use `GenericEvent` with the appropriate `int kind`.
- **17 concrete tag subclasses** — `EventTag`, `PubKeyTag`, `AddressTag`, `IdentifierTag`, `ReferenceTag`, `HashtagTag`, `ExpirationTag`, `UrlTag`, `SubjectTag`, `DelegationTag`, `RelaysTag`, `NonceTag`, `PriceTag`, `EmojiTag`, `GeohashTag`, `LabelTag`, `LabelNamespaceTag`, `VoteTag`. Use `GenericTag.of(code, params...)`.
- **27 entity classes** — `UserProfile`, `Profile`, `ChannelProfile`, `ZapRequest`, `ZapReceipt`, `Reaction`, all Cashu entities, all marketplace entities, and more.
- **`Kind` enum** — replaced by `Kinds` utility class with static `int` constants.
- **`ElementAttribute`** — replaced by `List<String>` in `GenericTag`.
- **`TagRegistry`** — no longer needed with a single tag class.
- **Interfaces and abstract classes**: `IElement`, `ITag`, `IEvent`, `IGenericElement`, `IBech32Encodable`, `Deleteable`, `BaseEvent`, `BaseTag`.
- **Annotations**: `@Tag`, `@Event`, `@Key`.
- **14 filter classes** — `AbstractFilterable`, `KindFilter`, `AuthorFilter`, `SinceFilter`, `UntilFilter`, `HashtagTagFilter`, `AddressTagFilter`, `GeohashTagFilter`, `IdentifierTagFilter`, `ReferencedEventFilter`, `ReferencedPublicKeyFilter`, `UrlTagFilter`, `VoteTagFilter`, `GenericTagQueryFilter`. Use `EventFilter.builder()`.
- **Concrete serializers/deserializers** — `AddressTagSerializer`, `ReferenceTagSerializer`, `ExpirationTagSerializer`, `IdentifierTagSerializer`, `RelaysTagSerializer`, `BaseTagSerializer`, `AbstractTagSerializer`, `CalendarEventDeserializer`, `ClassifiedListingEventDeserializer`, `CashuTokenSerializer`.
- **Client classes** — `WebSocketClientIF`, `WebSocketClientFactory`, `SpringWebSocketClientFactory`, `SpringWebSocketClient`. Use `NostrRelayClient` directly.
- **Dead code** — `Marker` enum, `RelayUri` value object.
- **5 old modules** — `nostr-java-util`, `nostr-java-crypto`, `nostr-java-base`, `nostr-java-id`, `nostr-java-encryption` (merged into the 4 remaining modules).
- Dead `pollIntervalMs` parameter from WebSocket client constructors.

## [1.3.0] - 2026-01-25

### Added
- Configurable WebSocket buffer sizes for handling large Nostr events via `nostr.websocket.max-text-message-buffer-size` and `nostr.websocket.max-binary-message-buffer-size` properties.

### Changed
- No additional behavior changes in this release; Kind APIs and WebSocket concurrency improvements were introduced in 1.2.1.

### Fixed
- No new fixes beyond 1.2.1; this release focuses on configurable WebSocket buffer sizes.
## [1.2.1] - 2026-01-21

### Fixed
- NIP-44 now correctly uses HKDF-Extract for conversation key derivation, ensuring proper cryptographic key generation.
- WebSocket client now correctly accumulates all relay responses (EVENT messages) before completing, waiting for termination signals (EOSE, OK, NOTICE, CLOSED) instead of returning after the first message.
- WebSocket client thread-safety improved by encapsulating pending request state, preventing potential race conditions when multiple threads call send() concurrently.
- WebSocket client now rejects concurrent send() calls with IllegalStateException instead of silently orphaning the previous request's future.
- KindFilter and ClassifiedListingEventDeserializer now use Kind.valueOfStrict() for fail-fast deserialization of unknown kind values.

### Changed
- Kind.valueOf(int) now returns null for unknown kind values instead of throwing, allowing graceful handling of custom or future NIP kinds during JSON deserialization.
- Added Kind.valueOfStrict(int) for callers who need fail-fast behavior on unknown kinds.
- Added Kind.findByValue(int) returning Optional<Kind> for safe, explicit handling of unknown kinds.

## [1.2.0] - 2025-12-26

### Fixed
- NIP-44 encryption now correctly uses HKDF instead of PBKDF2 for key derivation, as required by the specification. This fix enables DM interoperability between Java backend and JavaScript frontend implementations (e.g., nostr-tools).

### Changed
- Switched integration tests to use strfry relay for improved robustness.

### Removed
- Removed AGENTS.md and CLAUDE.md documentation files from the repository.

## [1.1.1] - 2025-12-24

### Fixed
- StandardWebSocketClient now configures WebSocketContainer with a 1-hour idle timeout (configurable via `nostr.websocket.max-idle-timeout-ms`) to prevent premature connection closures when relays have periods of inactivity.

## [1.1.0] - 2025-12-23

### Added
- Public constructor `StandardWebSocketClient(String relayUri, long awaitTimeoutMs, long pollIntervalMs)` for programmatic timeout configuration outside Spring DI context.

### Changed
- Enhanced diagnostic logging for timeout configuration in StandardWebSocketClient.
- Simplified WebSocket client initialization and retry logic in tests.

### Fixed
- Updated `JsonDeserialize` builder reference in API module.

## [1.0.1] - 2025-12-20

### Changed
- Updated project version and added artifact names in POM files.
- Added Sonatype Central server credentials configuration.
- Updated Maven command for central publishing.

## [1.0.0] - 2025-10-13

### Added
- Release automation script `scripts/release.sh` with bump/tag/verify/publish/next-snapshot commands (supports `--no-docker`, `--skip-tests`, and `--dry-run`).
- GitHub Actions:
  - CI workflow `.github/workflows/ci.yml` with Java 21 build and Java 17 POM validation; separate Docker-based integration job; uploads reports/artifacts.
  - Release workflow `.github/workflows/release.yml` publishing to Maven Central, validating tag vs POM version, and creating GitHub releases.
- Documentation:
  - `docs/explanation/dependency-alignment.md` — BOM alignment and post-1.0 override removal plan.
  - `docs/howto/version-uplift-workflow.md` — step-by-step release process; wired to `scripts/release.sh`.

### Changed
- Roadmap project helper `scripts/create-roadmap-project.sh` now adds tasks for:
  - Release workflow secrets setup (Central + GPG)
  - Enforcing tag/version parity during releases
  - Updating docs version references to latest
  - CI + Docker IT stability and triage plan
- Expanded decoder and mapping tests to cover all implemented relay commands (EVENT, CLOSE, EOSE, NOTICE, OK, AUTH).
- Stabilized NIP-52 (calendar) and NIP-99 (classifieds) integration tests for deterministic relay behavior.
- Docs updates to prefer BOM usage:
  - `docs/GETTING_STARTED.md` updated with Maven/Gradle BOM examples
  - `docs/howto/use-nostr-java-api.md` updated to import BOM and omit per-module versions
  - Cross-links added from the roadmap to migration and dependency alignment docs
- README cleanup: removed maintainer-only roadmap automation and moved troubleshooting to `docs/howto/diagnostics.md`.

### Removed
- Deprecated APIs finalized for 1.0.0:
  - `nostr.config.Constants.Kind` facade — use `nostr.base.Kind`
  - `nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD` — use `nostr.event.json.EventJsonMapper#getMapper()`
  - `nostr.api.NIP01#createTextNoteEvent(Identity, String)` and related Identity-based overloads — use instance-configured sender
  - `nostr.api.NIP61#createNutzapEvent(Amount, List<CashuProof>, URL, List<EventTag>, PublicKey, String)` — use slimmer overload and add amount/unit via `NIP60`
  - `nostr.event.tag.GenericTag(String, Integer)` compatibility ctor
  - `nostr.id.EntityFactory.Events#createGenericTag(PublicKey, IEvent, Integer)`

### Notes
- Integration tests require Docker (Testcontainers). CI runs a separate job for them on push; PRs use the no-Docker profile.
- See `MIGRATION.md` for complete guidance on deprecated API replacements.
