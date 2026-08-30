# ADR-0002: Multi-relay failure semantics

- **Status**: Accepted
- **Date**: 2026-08-30
- **Extends**: [ADR-0001](0001-introduce-nostr-java-api-module.md)

## Context

Fanning one operation out to many relays makes **partial failure the normal case**. Publishing
to five relays can plausibly yield three acceptances, one protocol-level rejection
(`blocked: pubkey banned`), and one timeout, all in a single call. A single-relay client has no
vocabulary for this: `NostrRelayClient.send` either returns or throws.

Two prior lessons constrain the answer. v2.0.0 replaced silent empty-list timeout returns with
a typed `RelayTimeoutException`, so total failure must not be reported as an ordinary value.
And a long-lived firehose subscription receives the same event from every connected relay, so
de-duplication cannot be optional.

## Decision

### Publishing

`publish` returns a **`PublishResult`** recording each relay's outcome, and **throws when zero
relays accepted the event**.

Partial failure is data the caller inspects; total failure is exceptional. Returning `void` and
throwing only on total failure would hide which relays rejected and why. Never throwing would
reintroduce the silent-failure bug v2.0.0 removed.

### Pool construction

`RelayPool` is **best-effort**: it connects the relays it can, marks unreachable ones as down,
and retries them lazily. Construction does not fail because a member relay is unreachable.

Relays are unreliable by nature, and all-or-nothing construction makes the pool as brittle as
its worst member. The cost is that connection health becomes the pool's responsibility:
per-relay `ConnectionState` must be observable, and reconnection policy is owned by the pool.

### Subscription de-duplication

Fan-in subscriptions de-duplicate by event `id` through a **bounded LRU window**, sized by
configuration with a default in the low thousands.

An unbounded set leaks memory on exactly the long-lived subscriptions that need it most.
Pushing de-duplication to callers would offload the one behaviour every multi-relay consumer
needs.

## Consequences

- `PublishResult` is a new public value type: per-relay accepted/rejected/timed-out plus the
  relay's reason string.
- The pool exposes per-relay health; callers can distinguish "rejected" from "never asked".
- Events arriving after eviction from the LRU window can be re-delivered. The window default
  must exceed any realistic cross-relay arrival spread.
- Callers who want strict all-relay delivery must check `PublishResult` themselves; the API
  does not offer an all-or-nothing publish mode.
