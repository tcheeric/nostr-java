# ADR-0001: Introduce a `nostr-java-api` module

- **Status**: Accepted
- **Date**: 2026-08-30

## Context

The SDK exposes four modules in a strict chain: `core → event → identity → client`.
Clients assemble events by hand, sign them with an `Identity`, and publish them through a
`NostrRelayClient` bound to a **single** relay URI.

Three capability gaps follow from that shape:

- **No relay pool.** Nostr is inherently multi-relay: publish to N relays, subscribe across
  N relays with de-duplication. No module owns this.
- **No delivery execution.** `Nip17DirectMessageService.planDelivery` returns a
  `List<MessageDelivery>` that nothing executes. The seam dangles.
- **No relay-list lookup.** `DirectMessageRelayLookup` has no implementation, because a real
  one must fetch kind 10050 events, which requires a relay pool.

## Decision

Add `nostr-java-api` as a **capability layer**, not a wrapper. It exposes a `NostrClient`
facade over services that add behaviour the lower modules deliberately do not have.

1. **Purpose**: capability layer first; convenience is a consequence, not the goal.
   `nostr-java-api` is *not* a sealed public API boundary: callers may still import
   `GenericEvent`, `GenericTag` and `EventFilter` directly, preserving the protocol-aligned,
   no-imposed-hierarchy design.
2. **Relay pool lives in `nostr-java-client`.** A pool is a transport concern, usable without
   buying into the facade. This keeps `api` thin and avoids a fifth module for one class.
3. **`NostrClient` owns an `Identity`**, supplied at build time so build → sign → publish
   collapses into one call. A per-call identity override supports signing bots and
   multi-account hosts.
4. **Plain-Java construction.** `NostrClient.builder()` must work in a `main()` method with no
   Spring application context, even though `nostr-java-client` depends on Spring internally.
   A `nostr-java-spring-boot-starter` is a later, separate release decision.
5. **Sync-first API.** Fan-out across relays blocks cheaply on Virtual Threads, and the sync
   signature reads better. Async variants are added only where fan-out blocking is material.
   Subscriptions remain callback-based and are unaffected.

## Consequences

- The module graph becomes `core → event → identity → client → api`; nothing depends on `api`.
- `nostr-java-client` grows a `RelayPool` alongside `NostrRelayClient`.
- `DirectMessageRelayLookup` gains a relay-backed implementation in `api`.
- The facade returns core types (`GenericEvent`), so no parallel event model is introduced.
- Rejecting the sealed-boundary option means `api` must justify itself by capability; any
  method that only reorders existing calls does not belong in it.
