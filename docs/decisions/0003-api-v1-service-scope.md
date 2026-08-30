# ADR-0003: `nostr-java-api` v1 service scope

- **Status**: Accepted
- **Date**: 2026-08-30
- **Extends**: [ADR-0001](0001-introduce-nostr-java-api-module.md)

## Context

A facade attracts convenience methods. ADR-0001 committed the module to being a **capability
layer**, so each service must add behaviour that no existing module has, rather than reordering
calls the caller could already make.

## Decision

v1 ships four services behind `NostrClient`:

| Service | Justification |
| --- | --- |
| `publish` | Fan-out across the pool and per-relay result aggregation exist nowhere today. |
| `subscriptions` | Fan-in across relays with de-duplication exists nowhere today. |
| `directMessages` | Executes the NIP-17 delivery plan that `identity` can only produce. |
| relay-list lookup | Required to implement `DirectMessageRelayLookup` (kind 10050). |

**Deferred**: general profile handling (kind 0 metadata, kind 3 contacts, kind 10002 relay
lists beyond the DM case). These are convenience over capability, and are cheap to add once
the pool has proven itself.

### NIP-17 orchestration lives in `api`

`Nip17DirectMessageService.planDelivery` returns a `List<MessageDelivery>` precisely so that
`nostr-java-identity` stays transport-free. The API module calls `planDelivery`, then feeds the
resulting plan to the relay pool.

Pushing an executor down into `identity` behind an inverted transport interface would give that
module a transport concern it was deliberately designed to avoid. Orchestrating across a
policy module and a transport module is what a capability layer is for.

## Consequences

- `identity` gains no new dependencies; the `MessageDelivery` seam is finally consumed.
- The relay-list lookup is implemented in `api` and injected into `identity`'s
  `DirectMessageRelayLookup` interface, keeping the dependency arrow pointing the right way.
- `NostrClient` grows a `profiles()` accessor only when the deferred work lands, so its absence
  in v1 is not a breaking gap.
- Any proposed method that merely reorders existing calls is rejected by this ADR's test.
