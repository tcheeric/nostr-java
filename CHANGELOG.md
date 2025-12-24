# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by Keep a Changelog, and this project adheres to semantic versioning once 1.0.0 is released.

## [Unreleased]

No unreleased changes yet.

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
