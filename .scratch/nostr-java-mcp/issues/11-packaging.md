# 11: Container packaging and the bound-container pattern

**What to build:** An operator can run the server from a container, including one container per
identity, without hand-assembling the configuration or accidentally publishing an
unauthenticated port to the network.

The container is where the HTTP transport's lack of authentication becomes dangerous, because
publishing a port reaches every interface by default. The compose file therefore binds to the
host loopback explicitly rather than relying on the server's own default.

It is also where the keystore default changes: `os-keychain` has nothing to talk to inside a
container, so the compose file selects `encrypted-file` explicitly.

**Blocked by:** 10 (HTTP transport with per-session identity binding).

**Status:** ready-for-agent

- [x] A `Dockerfile` builds on a distroless JRE 21 base and runs as a non-root user
- [x] `docker-compose.yml` runs the server in HTTP mode alongside the test relay container
- [x] Its port mapping binds to the host loopback (`127.0.0.1:PORT:PORT`), not every interface
- [x] It sets `keystore.type: encrypted-file` explicitly, with the keystore mounted read-only
      and the passphrase supplied as a secret
- [x] A profile demonstrates one bound container per identity, each reading only its own key
- [x] `docker-compose build` runs in CI, per repo convention
- [x] `mvn -q verify` passes
