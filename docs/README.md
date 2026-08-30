# Documentation Index

Quick links to the most relevant guides and references.

## Getting Started

- [GETTING_STARTED.md](GETTING_STARTED.md) — Installation and setup via Maven/Gradle
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) — Common issues and solutions

## How-to Guides

- [howto/use-nostr-java-api.md](howto/use-nostr-java-api.md) — Quick start: create, sign, and send events
- [howto/api-examples.md](howto/api-examples.md) — Comprehensive examples for common use cases
- [howto/multi-relay-publishing.md](howto/multi-relay-publishing.md) — Publish and subscribe across many relays with `NostrClient`
- [howto/private-direct-messages.md](howto/private-direct-messages.md) — Send and read NIP-17 private direct messages
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
- [explanation/nostr-java-mcp-spec.md](explanation/nostr-java-mcp-spec.md) — Draft spec for the `nostr-java-mcp` MCP server module
- [explanation/nip-17-direct-messages-spec.md](explanation/nip-17-direct-messages-spec.md) — Draft spec for NIP-17 private direct messages and NIP-59 gift wrapping
- [explanation/dependency-alignment.md](explanation/dependency-alignment.md) — How versions are aligned via BOM

## Developer

- [developer/SIMPLIFICATION_PROPOSAL.md](developer/SIMPLIFICATION_PROPOSAL.md) — 2.0 design simplification proposal

## Decisions

- [CONTEXT.md](CONTEXT.md) — Shared vocabulary: modules, relay pool, publish result, delivery plan
- [decisions/0001-introduce-nostr-java-api-module.md](decisions/0001-introduce-nostr-java-api-module.md) — Why `nostr-java-api` exists and what it is not
- [decisions/0002-multi-relay-failure-semantics.md](decisions/0002-multi-relay-failure-semantics.md) — Partial failure, pool construction, de-duplication
- [decisions/0003-api-v1-service-scope.md](decisions/0003-api-v1-service-scope.md) — Which services ship in v1, and where NIP-17 orchestration lives
- [decisions/0004-pool-concurrency-and-subscription-lifecycle.md](decisions/0004-pool-concurrency-and-subscription-lifecycle.md) — Per-relay serialization, parsed payloads, auto-resubscribe
- [decisions/0005-pool-membership-eose-and-ownership.md](decisions/0005-pool-membership-eose-and-ownership.md) — Mutable membership, synthetic EOSE, resource ownership

## Project

- [CODEBASE_OVERVIEW.md](CODEBASE_OVERVIEW.md) — Codebase layout, testing, contributing

## Tests Overview

- Client module (Spring WebSocket): `nostr-java-client/src/test/java/nostr/client/springwebsocket/README.md` — send/subscribe retries and timeout behavior
