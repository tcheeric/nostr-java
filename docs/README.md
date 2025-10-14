# Documentation Index

Quick links to the most relevant guides and references.

## Getting Started

- [GETTING_STARTED.md](GETTING_STARTED.md) — Installation and setup via Maven/Gradle
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) — Common issues and solutions
- [MIGRATION.md](MIGRATION.md) — Upgrading between versions

## How‑to Guides

- [howto/use-nostr-java-api.md](howto/use-nostr-java-api.md) — Basic API usage
- [howto/api-examples.md](howto/api-examples.md) — Comprehensive examples with 13+ use cases
- [howto/streaming-subscriptions.md](howto/streaming-subscriptions.md) — Long-lived subscriptions
- [howto/custom-events.md](howto/custom-events.md) — Creating custom event types
- [howto/manage-roadmap-project.md](howto/manage-roadmap-project.md) — Sync the GitHub Project with the 1.0 backlog
- [howto/version-uplift-workflow.md](howto/version-uplift-workflow.md) — Tagging, publishing, and BOM alignment for releases
- [howto/configure-release-secrets.md](howto/configure-release-secrets.md) — Configure Maven Central and GPG secrets for releases
- [howto/ci-it-stability.md](howto/ci-it-stability.md) — Keep CI green and stabilize Docker-based ITs

## Operations

- [operations/README.md](operations/README.md) — Ops index (logging, metrics, config)
- [howto/diagnostics.md](howto/diagnostics.md) — Inspecting relay failures and troubleshooting

## Reference

- [reference/nostr-java-api.md](reference/nostr-java-api.md) — API classes, methods, and examples

## Explanation

- [explanation/extending-events.md](explanation/extending-events.md) — Extending the event model
- [explanation/roadmap-1.0.md](explanation/roadmap-1.0.md) — Outstanding work before the 1.0 release
- [explanation/dependency-alignment.md](explanation/dependency-alignment.md) — How versions are aligned and the 1.0 cleanup plan

## Project

- [CODEBASE_OVERVIEW.md](CODEBASE_OVERVIEW.md) — Codebase layout, testing, contributing

## Tests Overview

- API Client/Handler tests: `nostr-java-api/src/test/java/nostr/api/client/README.md` — logging, relays, handler send/close/request, dispatcher & subscription manager
- Client module (Spring WebSocket): `nostr-java-client/src/test/java/nostr/client/springwebsocket/README.md` — send/subscribe retries and timeout behavior
