# 03: Per-relay serialization and connection health

**What to build:** A developer publishes from several threads at once without hitting
`IllegalStateException: A request is already in flight`, and can see which relays are actually
carrying traffic.

`NostrRelayClient` permits one request in flight per connection. The pool therefore serializes
operations per relay: concurrent publishes to the same relay queue behind a lock, while
fan-out across different relays stays concurrent. A slow relay delays only its own queue.

This is a known throughput ceiling, not a bug to be worked around by opening more sockets per
relay, which relays penalise. Making `NostrRelayClient` multiplex is separate follow-up work.

An operator can observe per-relay `ConnectionState`, and relays that are down are retried in
the background so they rejoin without an application restart.

See ADR-0004.

**Blocked by:** 02 (`RelayPool` with fan-out publishing).

**Status:** ready-for-agent

- [ ] Concurrent publishes to one relay queue rather than throwing
- [ ] Publishes to different relays still proceed concurrently
- [ ] Per-relay `ConnectionState` is observable from the pool
- [ ] Relays that are down are retried in the background and rejoin the pool when they recover
- [ ] A relay that recovers participates in subsequent publishes without a restart
- [ ] Tests cover concurrent publishes to one relay, concurrency preserved across relays, and
      a down relay rejoining
- [ ] `mvn -q verify` passes
