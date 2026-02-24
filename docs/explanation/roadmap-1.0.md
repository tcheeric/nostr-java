# 1.0 Roadmap (Historical)

> **Note:** This roadmap was completed with the 1.0.0 release. The library has since been simplified in 2.0.0 — see [SIMPLIFICATION_PROPOSAL.md](../developer/SIMPLIFICATION_PROPOSAL.md) and [CHANGELOG.md](../../CHANGELOG.md).

This explanation outlines the outstanding work that was required to promote `nostr-java` from the 0.6.x snapshots to a stable 1.0.0 release. Items are grouped by theme so maintainers can prioritize stabilization, hardening, and release-readiness tasks.

## Release-readiness snapshot

| Theme | Why it matters for 1.0 | Key tasks |
| --- | --- | --- |
| API stabilization | Cleanly removing deprecated entry points avoids breaking changes post-1.0. | Remove `Constants.Kind`, `Encoder.ENCODER_MAPPER_BLACKBIRD`, and other for-removal APIs. |
| Protocol coverage | Missing tests leave command handling and relay workflows unverified. | Complete message decoding/command mapping tests; resolve brittle relay integration tests. |
| Developer experience | Documentation gaps make migrations risky and hide release steps. | Populate the 1.0 migration guide and document dependency alignment/release chores. |

## API stabilization and breaking-change prep

- **Remove the deprecated constants facade.** `nostr.config.Constants.Kind` is still published even though every field is flagged `@Deprecated(forRemoval = true)`; delete the nested class (and migrate callers to `nostr.base.Kind`) before cutting 1.0.0.【F:nostr-java-api/src/main/java/nostr/config/Constants.java†L1-L194】
- **Retire the legacy encoder singleton.** The `Encoder.ENCODER_MAPPER_BLACKBIRD` field remains available despite a for-removal notice; the mapper should be removed after migrating callers to `EventJsonMapper` so the 1.0 interface stays minimal.【F:nostr-java-base/src/main/java/nostr/base/Encoder.java†L1-L34】
- **Drop redundant NIP facades.** The older overloads in `NIP01` and `NIP61` that still accept an explicit `Identity`/builder arguments contradict the new fluent API and are marked for removal; purge them together with any downstream usage when finalizing 1.0.【F:nostr-java-api/src/main/java/nostr/api/NIP01.java†L152-L195】【F:nostr-java-api/src/main/java/nostr/api/NIP61.java†L103-L156】
- **Remove deprecated tag constructors.** The ad-hoc `GenericTag` constructor (and similar helpers in `EntityFactory`) persist only for backward compatibility; deleting them tightens the surface area and enforces explicit sender metadata in example factories.【F:nostr-java-event/src/main/java/nostr/event/tag/GenericTag.java†L1-L44】【F:nostr-java-id/src/test/java/nostr/id/EntityFactory.java†L25-L133】

## Protocol coverage and quality gaps

- **Extend message decoding coverage.** Both `BaseMessageDecoderTest` and `BaseMessageCommandMapperTest` only cover the `REQ` flow and carry TODOs for the remaining relay commands (EVENT, NOTICE, EOSE, etc.); expand the fixtures so every command path is exercised before freezing APIs.【F:nostr-java-event/src/test/java/nostr/event/unit/BaseMessageDecoderTest.java†L16-L117】【F:nostr-java-event/src/test/java/nostr/event/unit/BaseMessageCommandMapperTest.java†L16-L74】
- **Stabilize calendar and classifieds integration tests.** The NIP-52 and NIP-99 integration suites currently comment out flaky assertions and note inconsistent relay responses (`EVENT` vs `EOSE`); diagnose the relay behavior, update expectations, and re-enable the assertions to guarantee end-to-end compatibility.【F:nostr-java-api/src/test/java/nostr/api/integration/ApiNIP52RequestIT.java†L82-L160】【F:nostr-java-api/src/test/java/nostr/api/integration/ApiNIP99RequestIT.java†L71-L165】

## Documentation and release engineering

- **Finish the migration checklist.** The `MIGRATION.md` entry for “Deprecated APIs Removed” still lacks the concrete removal list that integrators need; populate it with the APIs scheduled above so adopters can plan upgrades safely. See Migration Guide → Deprecated APIs Removed: ../../MIGRATION.md#deprecated-apis-removed
- **Record the dependency alignment plan.** The parent `pom.xml` imports the BOM and temporarily overrides module versions until the BOM includes the matching coordinates; see the plan to remove overrides post-1.0 in [Dependency Alignment](dependency-alignment.md).【F:pom.xml†L71-L119】
- **Plan the version uplift.** The aggregator POM still advertises a SNAPSHOT; outline the steps for bumping modules, tagging, publishing to Central, and updating the BOM in the how-to guide: ../howto/version-uplift-workflow.md.【F:pom.xml†L71-L119】

## Suggested next steps

1. Resolve the API deprecations and land refactors behind feature flags where necessary.
2. Stabilize the relay-facing integration tests (consider mocking relays for deterministic assertions if public relays differ).
3. Update `MIGRATION.md` alongside each removal so downstream consumers have a single source of truth.
4. When the backlog is green, coordinate the version bump, remove BOM overrides, and publish the 1.0.0 release notes.
