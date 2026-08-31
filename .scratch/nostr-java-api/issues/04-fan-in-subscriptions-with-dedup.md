# 04: Fan-in subscriptions with de-duplication

**What to build:** A developer opens one subscription covering every relay in the pool and
receives each matching event exactly once, already parsed.

The same event arrives from every relay that has it, so without de-duplication a five-relay
subscription shows every note five times. The pool de-duplicates by event `id` through a
bounded LRU window: an unbounded set would leak memory on precisely the long-lived firehose
subscriptions that need de-duplication most.

Callbacks receive `GenericEvent` rather than raw JSON. This is capability, not convenience:
de-duplicating by event id forces the pool to parse inbound payloads anyway, so handing back
the undecoded string would be perverse.

Closing the subscription handle unsubscribes from every relay at once.

See ADR-0002 and ADR-0004.

**Blocked by:** 02 (`RelayPool` with fan-out publishing).

**Status:** done

- [x] One subscribe call registers the filter with every relay in the pool
- [x] Callbacks receive parsed `GenericEvent` values
- [x] An event arriving from several relays is delivered to the caller once
- [x] De-duplication uses a bounded LRU window whose size is configurable, defaulting to the
      low thousands
- [x] Memory does not grow without bound over a long-lived subscription
- [x] Closing the subscription handle stops delivery from every relay
- [x] Tests cover a duplicate event from four relays delivered once, window eviction staying
      bounded, and closing the handle stopping all relays
- [x] `mvn -q verify` passes
