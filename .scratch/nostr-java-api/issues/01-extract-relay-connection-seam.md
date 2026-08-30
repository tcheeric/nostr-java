# 01: Extract the `RelayConnection` seam

**What to build:** A contributor can write a test that drives relay behaviour, accepting an
event, rejecting it with a reason, going silent, or dropping mid-stream, without opening a
WebSocket, running Docker, or mocking a `WebSocketSession`.

This is a prefactor: no user-visible behaviour changes. It exists because every later ticket
needs a place to substitute relay behaviour, and today the only way in is a package-private
constructor taking a mocked session, which is unreachable from other modules and does not scale
to several relays failing independently.

A narrow `RelayConnection` interface is extracted in `nostr-java-client`, exposing only what a
relay pool needs: connect, send, subscribe, connection state, close. It deliberately does not
mirror all of `NostrRelayClient`'s public surface. `NostrRelayClient` becomes its production
implementation, and a `RelayConnectionFactory` maps a relay URI to a connection.

**Blocked by:** None (can start immediately).

**Status:** ready-for-agent

- [ ] `RelayConnection` exposes connect, send, subscribe, connection state, and close, and
      nothing that only `NostrRelayClient` needs
- [ ] `NostrRelayClient` implements `RelayConnection` with no change to its existing behaviour
- [ ] `RelayConnectionFactory` resolves a relay URI to a `RelayConnection`
- [ ] A `FakeRelay` test fixture implements `RelayConnection` and can be scripted to accept,
      reject with a verbatim reason, never respond, drop mid-stream, emit a given event
      sequence, and delay or withhold `EOSE`
- [ ] At least one existing client test scenario is reproduced against `FakeRelay` with no
      Mockito, demonstrating the fixture is sufficient
- [ ] The interface carries no Spring types, so modules above can depend on it without Spring
- [ ] `mvn -q verify` passes
