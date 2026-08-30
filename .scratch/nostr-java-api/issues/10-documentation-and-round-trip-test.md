# 10: Documentation and round-trip integration test

**What to build:** A developer discovering the SDK finds the multi-relay path documented as the
normal way to use it, and the project has end-to-end proof it works against a real relay.

Everything before this ticket is verified against `FakeRelay`, which is the right seam for
failure scenarios but never touches a socket. One round trip against a live or containerised
relay closes that gap: publish an event, read it back through a subscription.

**Blocked by:** 09 (`NostrClient` facade and module wiring).

**Status:** ready-for-agent

- [ ] An integration test publishes an event and reads it back via a subscription against a
      real relay, following the repo's existing Docker / no-docker profile split
- [ ] A how-to guide covers multi-relay publishing, subscribing, and direct messages, filed
      under `docs/howto` per Diátaxis and linked from `docs/README.md`
- [ ] The API reference documents `NostrClient`, `RelayPool`, and `PublishResult`
- [ ] The README module table and architecture section include `nostr-java-api`
- [ ] `docs/explanation/architecture.md` reflects the five-module chain
- [ ] `CHANGELOG.md` records the new module under `Added`
- [ ] The known in-flight ceiling and the transient-relay sharing note are documented so they
      are not later mistaken for bugs
- [ ] `mvn -q verify` passes
