# 07: Identity lifecycle tools behind `identity-policy`

**What to build:** An agent asked to "make me a throwaway account for this project" can do it,
and an agent that hallucinates "remove that key" cannot destroy an account.

An identity is the user's Nostr account: creating one is cheap, losing one is unrecoverable,
and exposing one is irreversible. The tools are shaped around that asymmetry rather than
treated uniformly. Create, rename and set-default are freely allowed. Export and remove are
two-step guarded. Import never accepts key material as an argument at all.

That last point is the one that matters most. If `nostr_import_identity` took an `nsec`, the
key would pass through the model's context, land in the host's conversation log, and very
likely reach a third-party inference API. Instead `source` names *where the server should read
the key from itself*: a file it then offers to shred, its own environment, or a terminal prompt
the agent cannot see.

**Blocked by:** 04 (Key-admin CLI and single-identity mode), 06 (`WriteGuard` and the publish
tools). It waits on 06 because identity mutations reuse that ticket's two-step confirmation
rather than introducing a second confirmation concept.

**Status:** ready-for-agent

- [x] `nostr_create_identity` generates a key in the keystore and returns only alias, public
      key and npub
- [x] `nostr_import_identity` accepts no key material as an argument; `source` names a file,
      an environment variable, or a prompt the agent cannot observe
- [x] `nostr_rename_identity` and `nostr_set_default_identity` change aliases and defaults
- [x] `nostr_export_identity_backup` writes an encrypted file and returns the path only, never
      the contents
- [x] `nostr_remove_identity` is two-step, and refuses without a prior backup unless
      `acknowledgeNoBackup` is set
- [x] Removal zeroes the in-memory key, removes the entry, and closes anything bound to it
- [x] `identity-policy` governs these tools separately from `write-policy`, defaulting to the
      more restrictive of the two; `write-policy: deny` implies no mutation
- [x] Signing fails with `IDENTITY_AMBIGUOUS` when several identities exist and no default is
      set, rather than guessing
- [x] Aliases are validated against `[a-z0-9-]{1,32}`, since they appear in resource URIs
- [x] Every keystore mutation is logged with alias and public key
- [x] `mvn -q verify` passes
