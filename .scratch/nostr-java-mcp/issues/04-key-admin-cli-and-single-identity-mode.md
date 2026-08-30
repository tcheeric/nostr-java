# 04: Key-admin CLI and single-identity mode

**What to build:** A human can create and manage keys from the command line, and can bind a
server process to exactly one identity so an agent cannot post as the wrong account.

These belong together because they are two halves of one idea: a bound server operates a key
and does not administer a keystore, so binding presupposes some other way to create keys. The
CLI is that way, and it keeps key administration out of every agent's reach entirely.

Single-identity mode removes the wrong-account risk rather than guarding it. With the process
bound, the `identity` argument disappears from every signing tool's schema: there is nothing to
name, so nothing to name wrongly.

**Blocked by:** 03 (`IdentityVault` and keystore backends).

**Status:** ready-for-agent

- [ ] The same jar runs as an MCP server and as a CLI offering `keygen`, `import`, `list` and
      `remove`
- [ ] `nostr.mcp.identity` binds the process to one alias
- [ ] A bound process unlocks only that alias, leaving other entries undecrypted and absent
      from the heap
- [ ] A bound process omits the `identity` argument from signing tool schemas entirely
- [ ] A bound process registers no identity lifecycle tools
- [ ] `nostr_list_identities` on a bound server returns the single bound identity
- [ ] A server started with an empty keystore refuses to start and says how to create an
      identity, rather than generating one silently
- [ ] Golden-file tests pin the tool list separately for bound and unbound modes, so
      unregistration is asserted rather than assumed
- [ ] `mvn -q verify` passes
