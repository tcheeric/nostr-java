# Project Context

Shared vocabulary for `nostr-java`. Terms here mean exactly what this file says they mean, in
conversation, in ADRs, and in code. When a term is fuzzy or overloaded, sharpen it here first.

## Modules

The dependency chain is strict and acyclic:

```
core → event → identity → client → api
```

- **core** — Schnorr cryptography, Bech32, hex, validators. No Nostr domain types.
- **event** — `GenericEvent`, `GenericTag`, `Kinds`, `EventFilter`, messages, JSON codecs.
- **identity** — `Identity` key management, signing, NIP-04/NIP-44 encryption, NIP-17 policy.
  **Transport-free by design**: it produces plans, it never sends.
- **client** — WebSocket transport. `NostrRelayClient` (one relay) and `RelayPool` (many).
- **api** — Client-facing capability layer. See [ADR-0001](decisions/0001-introduce-nostr-java-api-module.md).

## Terms

**Relay pool** — A mutable, live set of relay connections, owning per-relay health,
reconnection, and the fan-out/fan-in of operations across members. Lives in `client`, not
`api`, because it is a transport concern. Not to be confused with a *relay list*.

**Relay list** — A user's published set of relays, as event kinds 10002 (general) and 10050
(direct messages). Data, not connections. A relay list is one input used to decide what goes
into a relay pool.

**Fan-out** — Sending one operation to every relay in the pool. Applies to publishing.

**Fan-in** — Merging inbound events from every relay in the pool into one stream, de-duplicated
by event `id`. Applies to subscriptions.

**Publish result** — The per-relay outcome record returned by a publish: for each relay,
accepted, rejected with the relay's reason string, or timed out. Partial failure is ordinary
data; *total* failure throws. See [ADR-0002](decisions/0002-multi-relay-failure-semantics.md).

**Delivery plan** — A `List<MessageDelivery>` produced by `Nip17DirectMessageService`: which
gift wrap goes to which recipient, over which relays. A plan is inert. Executing it is the
`api` module's job, which is what keeps `identity` transport-free.

**Gift wrap** — The NIP-59 outer event that conceals a NIP-17 direct message. A **rumor** is the
unsigned inner event; a **seal** is the middle layer.

**Synthetic EOSE** — One end-of-stored-events signal the pool emits after every participating
relay has sent its own `EOSE`, or the timeout expires. It marks the transition from stored
events to live events. Individual relays' `EOSE` frames are pool internals and are not exposed.

**Capability vs convenience** — The test the `api` module applies to every proposed method. A
**capability** is behaviour no lower module has, such as fan-out or delivery execution.
A **convenience** merely reorders calls the caller could already make. Convenience alone does
not justify a method. See [ADR-0003](decisions/0003-api-v1-service-scope.md).

**In-flight ceiling** — `NostrRelayClient` permits one request in flight per connection, so the
pool serializes operations per relay. A known throughput limit, not a bug to be worked around
by opening more sockets. See [ADR-0004](decisions/0004-pool-concurrency-and-subscription-lifecycle.md).

## Decisions

Architecture decision records live in [docs/decisions/](decisions/).
