# 02: `RelayPool` with fan-out publishing and `PublishResult`

**What to build:** A developer names several relays and publishes one event to all of them in a
single call, then inspects exactly what each relay did with it.

Publishing to five relays plausibly yields three acceptances, one rejection with a reason such
as `blocked: pubkey banned`, and one timeout. That partial outcome is ordinary data the caller
reads from a `PublishResult`, not an exception. Total failure, where no relay accepted at all,
throws: this is the lesson v2.0.0 learned when silent empty-list timeout returns were replaced
by `RelayTimeoutException`.

The pool is best-effort on construction. A client with five configured relays, two of them
unreachable, still starts and publishes to the other three.

See ADR-0002 for the failure semantics and `docs/CONTEXT.md` for the vocabulary.

**Blocked by:** 01 (Extract the `RelayConnection` seam).

**Status:** done

- [x] `RelayPool` is constructed from relay URIs via `RelayConnectionFactory` and lives in
      `nostr-java-client`
- [x] Publishing fans out concurrently across relays on Virtual Threads
- [x] `PublishResult` records, per relay, accepted, rejected with the relay's verbatim reason,
      or timed out
- [x] Publishing waits for every relay's `OK` up to a pool-level timeout; relays that miss it
      are recorded as timed out rather than left unresolved
- [x] Publishing throws when zero relays accepted the event
- [x] Publishing returns normally when at least one relay accepted, however many failed
- [x] Construction succeeds when some relays are unreachable, marking them down
- [x] Tests cover three-accept/one-reject/one-timeout, zero acceptances throwing, and
      construction with an unreachable member, all against `FakeRelay`
- [x] `mvn -q verify` passes
