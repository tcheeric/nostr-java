# 06: Runtime pool membership

**What to build:** A developer adds and removes relays while the client is running, so the
relay set can follow user preferences without a restart, and connections opened for a single
operation are released again afterwards.

This is what makes direct message delivery possible with one connection mechanism. NIP-17
delivery targets the *recipient's* relays, which the pool has typically never heard of. An
immutable pool would force delivery to open ad-hoc connections outside it, creating a second
code path for connection handling, health, and shutdown.

Transient connections are released by reference counting or idle eviction, so relays added for
one delivery do not accumulate over a long-running process.

See ADR-0005.

**Blocked by:** 03 (Per-relay serialization and connection health).

**Status:** done

- [x] Relays can be added to a running pool and immediately participate in operations
- [x] Relays can be removed from a running pool, closing their connection
- [x] A relay added for a single operation is released once no longer in use, by reference
      counting or idle eviction
- [x] Adding a relay already in the pool does not open a second connection
- [x] Removing a relay does not disturb in-flight operations on other relays
- [x] Tests cover add, remove, duplicate add, and release of a transiently added relay
- [x] `mvn -q verify` passes
