# 01: `ContactList` type over kind-3 in `nostr-java-event`

**What to build:** A developer can read and write a Nostr contact list without parsing tags
themselves, the same way `DirectMessageRelayList` handles kind-10050 today.

This is the MCP module's only SDK prerequisite. `Kinds.CONTACT_LIST = 3` exists but nothing
models the event, so `nostr_get_contacts` has nothing to call. The type belongs here rather
than in the MCP module because every consumer of the SDK benefits, and an MCP server parsing
`p` tags inline would be the only place in the codebase that knows how a contact list is
shaped.

The design is already proven: a prototype following the `DirectMessageRelayList` pattern
round-tripped through a live relay, published and recovered with its contacts intact, and its
kind guard rejected a kind-1 event. See `McpSpecAssumptionsIT`.

**Blocked by:** None (can start immediately).

**Status:** ready-for-agent

- [ ] `ContactList.from(GenericEvent)` reads `p` tags into public keys
- [ ] `toEvent()` renders the list back as an unsigned kind-3 event
- [ ] A wrong-kind event is rejected with a message naming the expected and actual kinds
- [ ] An event with no `p` tags yields an empty list rather than failing
- [ ] Tags that are not contacts are ignored rather than misread
- [ ] Round-trip is covered, including through a relay in an integration test
- [ ] `CHANGELOG.md` records the addition
- [ ] `mvn -q verify` passes
