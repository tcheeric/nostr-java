# 12: Guided prompts and the MCP host how-to

**What to build:** Someone who has never used this server can wire it into their MCP host and
get useful work out of it, and the agent is taught how to sequence the tools rather than
guessing.

A tool surface without guidance makes an agent explore by trial and error, which on a public,
irreversible medium is the wrong way to learn. Prompts encode the sequences that work.

The documentation leads with single-identity mode, because it is the safer default, matches how
MCP hosts are configured anyway, and removes the wrong-account risk rather than guarding it.
The multi-identity server is the advanced case, for the cross-identity queries a bound server
cannot answer.

**Blocked by:** 09 (Threads, contacts and direct messages), 11 (Container packaging).

**Status:** ready-for-agent

- [ ] Prompts ship for `compose-note`, `catch-up-feed` and `watch-mentions`
- [ ] Resources expose `nostr://identity/{alias}` and `nostr://relay/{name}` so an agent can
      read context without a tool call
- [ ] A how-to under `docs/howto` covers wiring the server into an MCP host, leading with
      single-identity mode and presenting multi-identity as the advanced case
- [ ] It is linked from `docs/README.md` per the repo's Diátaxis convention
- [ ] The inherited SDK limits are documented where a user meets them: per-relay throughput,
      windowed de-duplication, and the unauthenticated HTTP transport
- [ ] `CHANGELOG.md` records the new module
- [ ] `mvn -q verify` passes
