# 07: Relay list lookup (kind 10050)

**What to build:** A developer can discover which relays a given user reads direct messages
from, resolved from that user's published kind 10050 relay list.

`DirectMessageRelayLookup` exists in `nostr-java-identity` as a seam with no implementation,
because a real one must fetch events, and `identity` is deliberately transport-free. This
ticket provides that implementation in the new `nostr-java-api` module, fetching relay lists
through the pool and injecting it into `identity`'s interface, so the dependency arrow points
from `api` to `identity` and never back.

A relay list is *data*, distinct from the relay pool, which is a set of live connections. See
`docs/CONTEXT.md`.

**Blocked by:** 04 (Fan-in subscriptions with de-duplication).

**Status:** done

- [x] Given a public key, the lookup returns the relays from that user's kind 10050 event
- [x] The lookup implements `DirectMessageRelayLookup` without adding any dependency to
      `nostr-java-identity`
- [x] A user with no published relay list yields an empty result rather than an error
- [x] Results are fetched through the relay pool, not through a bespoke connection
- [x] Tests cover a user with a relay list, a user without one, and a user whose list is
      served by only some of the queried relays
- [x] `mvn -q verify` passes
