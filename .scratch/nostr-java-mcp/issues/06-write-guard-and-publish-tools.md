# 06: `WriteGuard` and the publish tools

**What to build:** An agent can publish to Nostr, and a hallucinated post is a no-op rather
than a public, permanent mistake.

Publishing is irreversible: an event accepted by a relay cannot be reliably deleted, since
NIP-09 is advisory. So `write-policy: confirm` is the default, and a write tool returns a
preview of the signed event with a `confirmationToken` that the agent must present on a second
call. `deny` unregisters the write tools altogether, because a tool an agent cannot see is a
tool it cannot misuse.

Results follow the SDK rather than inventing a convention: a `PublishResult` with any
acceptance is a success carrying the per-relay list, and only `NoRelayAcceptedException` is an
error. Reporting partial success as failure would push agents to retry writes that already
landed.

**Blocked by:** 03 (`IdentityVault` and keystore backends), 05 (Read tools).

**Status:** ready-for-agent

- [ ] `nostr_publish_note` publishes a kind-1 note, signed by the configured identity
- [ ] `nostr_publish_event` publishes an arbitrary kind as the escape hatch
- [ ] `nostr_update_profile` publishes kind-0 metadata
- [ ] Under `confirm`, the first call previews and the second call with the token publishes
- [ ] Confirmation tokens stay valid until used or until restart; they do not expire on a timer
- [ ] Under `deny`, no write tool is registered at all
- [ ] Partial success returns the per-relay list; only a total failure is an error
- [ ] Rate limits are enforced per identity and per relay
- [ ] Every write is logged with event id, kind, identity pubkey and target relays
- [ ] A golden-file test pins the tool list under `write-policy: deny`
- [ ] `mvn -q verify` passes
