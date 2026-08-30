# 10: HTTP transport with per-session identity binding

**What to build:** A hosted deployment can reach the server over HTTP rather than stdio, with
each session bound to one identity, and cannot accidentally expose it to the network.

The transport carries **no authentication of its own** in v1. That is a deliberate deferral:
a token in a config file protects little, and deployments that genuinely need remote access
need a reverse proxy with real credentials in front regardless. A deferral is only safe if the
constraint replacing it is visible, so the default binding is loopback and a non-loopback bind
warns at startup, the same way the `env` keystore backend warns.

Per-session binding is the middle ground between one shared server and one process per
identity: it guards against agent confusion within one heap, not against a compromised process.
That weaker guarantee must be stated where a deployer reads it, not implied.

**Blocked by:** 07 (Identity lifecycle tools).

**Status:** ready-for-agent

- [x] The server runs over streamable HTTP as well as stdio, selected by `transport`
- [x] `bind-address` defaults to `127.0.0.1`
- [x] A non-loopback bind logs a warning at startup naming the risk
- [~] A session can be bound to one identity, filtering the tool surface as single-identity
      mode does — deferred: the streamable transport builds one server for all sessions, so
      per-session filtering needs a session-scoped tool surface the SDK does not yet expose.
      Process-level binding (ticket 04) remains the supported and stronger isolation.
- [x] Documentation states plainly that the transport is unauthenticated and belongs behind a
      proxy if exposed
- [x] `mvn -q verify` passes
