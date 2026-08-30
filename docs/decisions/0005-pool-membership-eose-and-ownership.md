# ADR-0005: Pool membership, EOSE aggregation, and resource ownership

- **Status**: Accepted
- **Date**: 2026-08-30
- **Extends**: [ADR-0004](0004-pool-concurrency-and-subscription-lifecycle.md)

## Context

Three lifecycle questions remained after the pool's failure semantics were settled.

NIP-17 delivery is addressed to **the recipient's** relays, discovered from their kind 10050
relay list. Those are not the relays the caller configured, so a delivery plan routinely names
relays the pool has never heard of.

A REQ returns stored events, then `EOSE`, then live events. Fanned across five relays there are
five separate `EOSE` frames, and applications still need one answer to "is the backlog drained".

## Decision

### Pool membership is mutable

`RelayPool` supports adding and removing relays at runtime. NIP-17 delivery adds the
recipient's relays on demand; connections are released by reference counting or idle eviction.

An immutable pool would force DM delivery to open ad-hoc connections outside it, creating a
second code path for connection handling, health tracking, and shutdown. Separate persistent
and transient pools would double the lifecycle for the same reason. One mutable pool covers
both cases with one mechanism.

### Subscriptions emit a single synthetic EOSE

The pool aggregates per-relay `EOSE` frames and signals **one** end-of-stored-events to the
caller once every participating relay has reported, bounded by the same kind of timeout that
bounds publishing.

"The backlog is drained, we are live now" drives spinners and initial-render decisions, so
hiding it entirely is not viable, and exposing per-relay `EOSE` leaks pool internals. The
timeout matters: an unresponsive relay must not stall the synthetic `EOSE` indefinitely.

### Resource ownership follows construction

`NostrClient` closes the pool it built from relay URIs. A pool passed in by the caller is not
closed by `NostrClient`.

This is the conventional Java ownership rule, and it lets the facade be used in a container
where the pool outlives it.

## Consequences

- The pool tracks per-relay usage so transiently added DM relays are eventually evicted.
- A relay added for DM delivery is available to other operations while it remains connected,
  which is a benign but real sharing of state.
- Subscriptions have three phases (stored, synthetic EOSE, live) that the API must express.
- Callers who pass their own pool are responsible for closing it, which must be documented
  prominently on the builder.
