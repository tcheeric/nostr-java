# Documentation Index

Quick links to the most relevant guides and references.

## Getting Started

- [GETTING_STARTED.md](GETTING_STARTED.md) — Installation and setup via Maven/Gradle
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) — Common issues and solutions

## How-to Guides

- [howto/use-nostr-java-api.md](howto/use-nostr-java-api.md) — Quick start: create, sign, and send events
- [howto/api-examples.md](howto/api-examples.md) — Comprehensive examples for common use cases
- [howto/streaming-subscriptions.md](howto/streaming-subscriptions.md) — Long-lived subscriptions with NostrRelayClient
- [howto/custom-events.md](howto/custom-events.md) — Working with custom event kinds
- [howto/diagnostics.md](howto/diagnostics.md) — Inspecting relay failures and troubleshooting
- [howto/version-uplift-workflow.md](howto/version-uplift-workflow.md) — Tagging, publishing, and BOM alignment for releases
- [howto/configure-release-secrets.md](howto/configure-release-secrets.md) — Configure Maven Central and GPG secrets for releases
- [howto/ci-it-stability.md](howto/ci-it-stability.md) — Keep CI green and stabilize Docker-based ITs

## Operations

- [operations/README.md](operations/README.md) — Ops index (logging, metrics, config)

## Reference

- [reference/nostr-java-api.md](reference/nostr-java-api.md) — API classes, methods, and examples

## Explanation

- [explanation/extending-events.md](explanation/extending-events.md) — Working with events and tags (GenericEvent, GenericTag, Kinds)
- [explanation/architecture.md](explanation/architecture.md) — Module architecture and data flow
- [explanation/dependency-alignment.md](explanation/dependency-alignment.md) — How versions are aligned via BOM

## Developer

- [developer/SIMPLIFICATION_PROPOSAL.md](developer/SIMPLIFICATION_PROPOSAL.md) — 2.0 design simplification proposal

## Project

- [CODEBASE_OVERVIEW.md](CODEBASE_OVERVIEW.md) — Codebase layout, testing, contributing

## Tests Overview

- Client module (Spring WebSocket): `nostr-java-client/src/test/java/nostr/client/springwebsocket/README.md` — send/subscribe retries and timeout behavior
