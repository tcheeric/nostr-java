# ADR-0004: Relay pool concurrency, payload types, and subscription lifecycle

- **Status**: Accepted
- **Date**: 2026-08-30
- **Extends**: [ADR-0002](0002-multi-relay-failure-semantics.md)

## Context

Two properties of `NostrRelayClient` constrain any pool built on it.

**One request in flight per connection.** `send` throws
`IllegalStateException: A request is already in flight` when a second call overlaps the first.
A pool cannot simply fan out concurrently across shared connections.

**Raw JSON at the boundary.** `send` returns `List<String>` and `subscribe` takes a
`Consumer<String>`. Nothing above the wire format is parsed for the caller.

## Decision

### Concurrency: serialize per relay

The pool **serializes operations per relay**. Concurrent publishes to the same relay queue
behind a lock. Fan-out remains concurrent *across* relays.

Opening several connections per relay URI would multiply sockets and relays penalise that.
The correct fix is to make `NostrRelayClient` multiplex, routing responses by subscription and
event id, but that is a rewrite of its request/response core and would swallow this effort.

**This is a known ceiling**: throughput to any single relay is bounded by round-trip latency.
Multiplexing is the follow-up, tracked separately.

### Payload types: the API deals in parsed events

Subscription callbacks receive **`GenericEvent`**, not raw JSON, and `PublishResult` carries
parsed `OK` outcomes.

This is capability rather than convenience. De-duplication by event `id` (ADR-0002) forces the
pool to parse inbound payloads regardless, and per-relay publish outcomes require parsing `OK`
messages for the accepted flag and reason string. Once parsed, handing back the string would be
perverse.

### Subscription lifecycle: auto-resubscribe and notify

When a relay drops mid-stream the pool **notifies an error callback and re-subscribes when that
relay reconnects**, replaying the stored filter.

A long-lived subscription that silently degrades from five relays to one, with the caller never
informed, is the characteristic multi-relay failure. Auto-resubscription is the main reason the
pool owns reconnection policy at all.

### Publishing waits for all relays, bounded by a timeout

`publish` waits for every relay's `OK` up to a pool-level timeout. Relays that do not answer in
time are recorded as **timed out** in the `PublishResult`.

Returning on first acceptance would leave most `PublishResult` entries unresolved. A
configurable quorum is a knob that will not be tuned correctly. A timeout is the honest bound
and makes slowness a first-class, visible outcome instead of a hidden race.

## Consequences

- The pool stores each subscription's filter for replay, so subscriptions are stateful objects
  rather than fire-and-forget handles.
- Publish latency is the slowest responding relay, capped by the timeout. Callers needing lower
  latency must publish asynchronously.
- Per-relay serialization means a slow relay delays only its own queue, not the fan-out.
- Parsing at the pool boundary means malformed relay payloads must be handled there, and
  reported without killing the subscription.
