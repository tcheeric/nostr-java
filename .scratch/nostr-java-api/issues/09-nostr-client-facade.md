# 09: `NostrClient` facade and module wiring

**What to build:** A developer configures their identity and relays once, then writes code in
terms of intent:

    try (NostrClient nostr = NostrClient.builder()
            .identity(identity)
            .relays("wss://relay.398ja.xyz", "wss://nos.lol")
            .build()) {
        nostr.publish().textNote("Hello Nostr!");
        nostr.directMessages().send(recipient, "hi");
    }

This ticket is last because it is only an assembly of parts already proven. It creates the
`nostr-java-api` module itself, extending the chain to
`core → event → identity → client → api`, with nothing depending on `api`.

The facade knows the caller's identity, which is what collapses build-sign-publish into one
step, with a per-call override for bots acting for several keys. It returns core types such as
`GenericEvent`, so no parallel event model appears and callers may still drop down to
`RelayPool` or build events by hand.

Ownership follows construction: a pool built from URIs is closed by `NostrClient`, a pool
passed in by the caller is not. Construction must work in a plain `main()` with no Spring
application context, even though `nostr-java-client` uses Spring internally.

See ADR-0001 and ADR-0005.

**Blocked by:** 05 (Synthetic EOSE and mid-stream recovery), 08 (NIP-17 direct message
delivery).

**Status:** ready-for-agent

- [ ] `nostr-java-api` module is added to the build with the correct dependency direction
- [ ] `NostrClient.builder()` accepts an identity and either relay URIs or an existing pool
- [ ] `publish()`, `subscriptions()`, `directMessages()`, and relay-list lookup are exposed,
      each behind an interface
- [ ] Events are signed with the configured identity, with a per-call identity override
- [ ] A pool built from URIs is closed by `NostrClient`; a supplied pool is not
- [ ] The ownership rule is documented on the builder method itself
- [ ] The client is constructible in a plain `main()` with no Spring application context
- [ ] Facade methods return core types, introducing no parallel event model
- [ ] Tests cover signing, per-call override, both ownership cases, and Spring-free construction
- [ ] `mvn -q verify` passes
