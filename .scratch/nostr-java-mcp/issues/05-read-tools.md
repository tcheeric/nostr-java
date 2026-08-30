# 05: Read tools for querying, profiles and relay metadata

**What to build:** An agent can answer questions about Nostr without being able to change
anything: fetch events matching a filter, look up someone's profile, and read a relay's
capabilities.

This is the whole read path, and it is where the module's argument conventions are established,
so later tools inherit them rather than reinventing them. Public keys accept hex or `npub`,
event ids accept hex or `note`/`nevent`, and timestamps accept ISO-8601 or relative expressions
like `24h`. Decoding lives in one place; tools never parse bech32 inline.

A query is bounded: `limits.max-events-per-query` and `query-timeout` exist because a relay
serves one request at a time, so an unbounded query can stall every other tool call.

**Blocked by:** 02 (Module skeleton with MCP stdio transport).

**Status:** ready-for-agent

- [ ] `nostr_query_events` runs a one-shot query and returns matching events
- [ ] `nostr_get_profile` fetches and decodes kind-0 metadata, accepting a pubkey or a NIP-05
      address
- [ ] `nostr_relay_info` returns a relay's NIP-11 document
- [ ] `NostrIdentifier` centralises hex/bech32 decoding for keys and event ids
- [ ] Relative timestamps are normalised to Unix seconds at the boundary
- [ ] Queries respect the configured event limit and timeout
- [ ] Errors use the stable codes from the spec rather than stack traces
- [ ] `mvn -q verify` passes
