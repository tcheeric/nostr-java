# nostr-java documentation

A Java implementation of the [Nostr protocol](https://github.com/nostr-protocol/nips): events,
signing, relays, encrypted messaging, and an MCP server that puts all of it in reach of an LLM
agent.

## Start here

| If you want to | Read |
| --- | --- |
| Add the library to a build and send your first note | [Getting started](GETTING_STARTED.md) |
| See worked examples of the common tasks | [API examples](howto/api-examples.md) |
| Understand how the modules fit together | [Architecture](explanation/architecture.md) |
| Look up a class or method | [API reference](reference/nostr-java-api.md) |
| Work out why something is failing | [Troubleshooting](TROUBLESHOOTING.md) |

The pages below are grouped by what they are for, following
[Diátaxis](https://diataxis.fr/): tutorials teach, how-to guides solve a problem, reference
describes the machinery, and explanation gives the reasoning.

## Tutorials

Learning-oriented, for a first encounter with the library.

- [Getting started](GETTING_STARTED.md) — install via Maven or Gradle, generate an identity,
  publish a note.

## How-to guides

Task-oriented, for someone who knows what they want to achieve.

**Using the library**

- [Create, sign and send events](howto/use-nostr-java-api.md) — the shortest path from a
  keypair to a published event.
- [API examples](howto/api-examples.md) — worked examples of the common tasks, in one place.
- [Publish and subscribe across many relays](howto/multi-relay-publishing.md) — `NostrClient`,
  partial failure, and reading results back.
- [Send private direct messages](howto/private-direct-messages.md) — NIP-17 gift wrapping and
  delivery to each recipient's own relays.
- [Stream long-lived subscriptions](howto/streaming-subscriptions.md) — staying connected and
  handling events as they arrive.
- [Work with custom event kinds](howto/custom-events.md) — events and tags the library does not
  model directly.
- [Run the MCP server](howto/run-the-mcp-server.md) — let an LLM agent use Nostr, with the
  safety model explained.

**Operating and diagnosing**

- [Diagnose relay failures](howto/diagnostics.md) — finding out which relay refused what, and
  why.
- [Configure the library](operations/configuration.md) — timeouts, retries, and connection
  settings.
- [Configure logging](operations/logging.md) — what is logged, at which level, and how to
  change it.
- [Collect metrics](operations/metrics.md) — the Micrometer metrics exposed and what they mean.

**Releasing and contributing**

- [Cut a release](howto/version-uplift-workflow.md) — tagging, publishing, and BOM alignment.
- [Configure release secrets](howto/configure-release-secrets.md) — Maven Central and GPG
  credentials.
- [Keep CI green](howto/ci-it-stability.md) — stabilising the Docker-backed integration tests.
- [Maintain the roadmap project](howto/manage-roadmap-project.md) — the GitHub project board.

## Reference

Information-oriented, for looking things up.

- [API reference](reference/nostr-java-api.md) — classes, methods and signatures across the
  modules.
- [Migration guide](MIGRATION.md) — what changed between major versions and how to move.
- [Troubleshooting](TROUBLESHOOTING.md) — symptoms, causes and fixes.
- [Operations index](operations/README.md) — configuration, logging and metrics at a glance.
- [Codebase overview](CODEBASE_OVERVIEW.md) — module layout, build and test commands.
- [Shared vocabulary](CONTEXT.md) — what this project means by pool, publish result, and
  delivery plan.

## Explanation

Understanding-oriented, for the reasoning behind the design.

- [Architecture](explanation/architecture.md) — the modules, their dependencies, and how data
  flows between them.
- [Working with events and tags](explanation/extending-events.md) — `GenericEvent`,
  `GenericTag` and the kind ranges.
- [Dependency alignment](explanation/dependency-alignment.md) — why versions are managed
  through a BOM.
- [Secure coding guidelines](developer/SECURE_CODING.md) — the rules this codebase follows
  around keys and encryption.

**Specifications**

- [MCP server specification](explanation/nostr-java-mcp-spec.md) — the design of
  `nostr-java-mcp`, including its safety model.
- [NIP-17 direct messages](explanation/nip-17-direct-messages-spec.md) — private messages and
  NIP-59 gift wrapping.

**Proposals and history**

These describe work that is proposed, in progress, or finished. They are kept because the
reasoning outlives the change.

- [Simplification proposal](developer/SIMPLIFICATION_PROPOSAL.md) — a proposed reduction of the
  2.0 design. Describes code that does not all exist yet.
- [1.0 roadmap](explanation/roadmap-1.0.md) — historical, kept for context.
- [Generic tag fragility](problems/GENERIC_TAG_GETCODE_FRAGILITY.md) — analysis of a
  `GenericTag.getCode()` failure and its downstream effects.
- [Integration test bug analysis](integration-test-bug-analysis.md) — why the relay container
  sometimes starts inert, and how the tests handle it.

## Decisions

Architecture decision records: what was decided, and what was given up.

- [0001 — Introduce `nostr-java-api`](decisions/0001-introduce-nostr-java-api-module.md) — why
  the module exists and what it deliberately is not.
- [0002 — Multi-relay failure semantics](decisions/0002-multi-relay-failure-semantics.md) —
  partial failure, pool construction, de-duplication.
- [0003 — API v1 service scope](decisions/0003-api-v1-service-scope.md) — which services ship,
  and where NIP-17 orchestration lives.
- [0004 — Pool concurrency and subscription lifecycle](decisions/0004-pool-concurrency-and-subscription-lifecycle.md)
  — per-relay serialisation, parsed payloads, auto-resubscribe.
- [0005 — Pool membership, EOSE and ownership](decisions/0005-pool-membership-eose-and-ownership.md)
  — mutable membership, synthetic EOSE, resource ownership.

## How this documentation is kept honest

`DocumentationAccuracyTest` in `nostr-java-api` runs with the ordinary build and fails when the
documentation drifts from the code: a guide naming a type or method that does not exist, a link
pointing at nothing, an install snippet quoting a version that is not the one being built, or a
page nobody links to from this index.

It found real problems when it was written, including a reference page teaching
`BaseMessage.read(json)`, a method that has never existed. Documentation rots quietly because
nothing fails when it does, which is exactly why it is worth a test.
