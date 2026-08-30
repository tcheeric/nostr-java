# 03: `IdentityVault` and keystore backends

**What to build:** The server loads signing keys at startup from the platform keychain or an
encrypted file, and an agent can see which identities exist without ever being able to see a
key.

Keys are the one thing in this module that cannot be un-leaked, so the vault boundary is the
module's central security property: tools pass an alias to a signing service and receive a
signed event; `Identity` objects never reach the tool layer.

`os-keychain` is the default because it needs no passphrase and so does not block unattended
startup. `encrypted-file` is the portable fallback and the right choice in a container, where
there is no keychain to talk to.

**Blocked by:** 02 (Module skeleton with MCP stdio transport).

**Status:** ready-for-agent

- [ ] `KeySource` has `os-keychain`, `encrypted-file` and `env` implementations chosen by
      `keystore.type`, defaulting to `os-keychain`
- [ ] The `env` backend warns at startup that it is unsuitable outside development
- [ ] `encrypted-file` refuses to start on a world-readable keystore
- [ ] Decrypted keys are held as `byte[]`/`char[]` and zeroed on shutdown, never as `String`
- [ ] `nostr_list_identities` returns aliases and public keys only
- [ ] `IdentitySummary` has no field capable of holding a private key
- [ ] A test walks every registered tool and resource and asserts no response or error can
      contain a private key, and no input schema accepts one
- [ ] The startup banner prints public keys only
- [ ] `mvn -q verify` passes
