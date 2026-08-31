# nostr-java
[![CI](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml)
[![CI Matrix: docker + no-docker](https://img.shields.io/badge/CI%20Matrix-docker%20%2B%20no--docker-blue)](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/tcheeric/nostr-java/branch/main/graph/badge.svg)](https://codecov.io/gh/tcheeric/nostr-java)
[![GitHub release](https://img.shields.io/github/v/release/tcheeric/nostr-java)](https://github.com/tcheeric/nostr-java/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Qodana](https://github.com/tcheeric/nostr-java/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/tcheeric/nostr-java/actions/workflows/qodana_code_quality.yml)

A Java SDK for the [Nostr protocol](https://github.com/nostr-protocol/nips). Create, sign and
publish events; talk to many relays at once; send encrypted direct messages; and expose all of
it to an LLM agent through a Model Context Protocol server.

Requires **Java 21** and Maven.

## Quick start

```java
Identity identity = Identity.generateRandomIdentity();

try (NostrClient nostr = NostrClient.builder()
        .identity(identity)
        .relays("wss://relay.398ja.xyz", "wss://nos.lol")
        .build()) {

    PublishResult result = nostr.publishTextNote("Hello Nostr!");

    System.out.println("stored by " + result.getAcceptingRelays());
    result.getFailures().forEach(failure ->
        System.out.println("refused by " + failure.relayUri()));
}
```

Publishing reports what each relay did rather than collapsing the answer to a boolean, and
throws `NoRelayAcceptedException` only when no relay accepted the event at all. A note that
reached three relays out of five has been published, and the caller needs to know which two
missed it rather than being told the whole thing failed. See
[publishing across many relays](docs/howto/multi-relay-publishing.md).

Installation, including Gradle and BOM coordinates, is in
[Getting started](docs/GETTING_STARTED.md).

## Give an LLM agent access to Nostr

`nostr-java-mcp` runs the SDK as a Model Context Protocol server, so an agent in Claude Desktop
or an IDE can read and publish without any Nostr-specific code.

```bash
java -jar nostr-java-mcp.jar keygen personal   # create a key; the private half is never printed
java -jar nostr-java-mcp.jar -Dnostr.mcp.identity=personal
```

Publishing to Nostr is public and cannot be reliably undone, so writes are confirmed by default:
the agent gets a preview and a token, and nothing is published until it calls again with that
token. A hallucinated post therefore becomes a no-op. See
[running the MCP server](docs/howto/run-the-mcp-server.md).

## Modules

Six modules with a strict dependency chain, each usable on its own:

```
nostr-java-core → nostr-java-event → nostr-java-identity → nostr-java-client → nostr-java-api → nostr-java-mcp
```

| Module | What it gives you |
| --- | --- |
| **core** | BIP-340 Schnorr signatures, Bech32 encoding, hex conversion |
| **event** | `GenericEvent`, `GenericTag`, `Kinds`, `EventFilter`, JSON serialisation |
| **identity** | `Identity` key management, signing, NIP-04 and NIP-44 encryption, NIP-59 gift wrapping |
| **client** | `NostrRelayClient` websocket transport with retry; `RelayPool` for fan-out and fan-in |
| **api** | `NostrClient`: multi-relay publishing with per-relay outcomes, de-duplicated subscriptions, NIP-17 delivery |
| **mcp** | An MCP server exposing the SDK to LLM agents over stdio or HTTP |

Most applications want `nostr-java-api`. Reach further down only when you need something it
does not expose.

## Design

- **One event class.** `GenericEvent` covers every kind, and `GenericTag` holds a code plus its
  parameters. Nostr's own model is integers and string arrays, so a type hierarchy on top would
  be a second model to keep in step with the first.
- **NIP-agnostic.** Any current or future NIP works through
  `GenericEvent.builder().kind(n)` with the right tags. Supporting a new NIP needs no library
  release. `Kinds` names the common values without restricting the rest.
- **Multi-relay by default.** Nostr has no single source of truth, so publishing fans out and
  subscribing fans in with de-duplication.
- **Virtual threads.** Relay I/O and listener dispatch run on Java 21 virtual threads; the
  async surface is `CompletableFuture`.
- **Failures are reported, not swallowed.** Per-relay outcomes, typed
  `RelayTimeoutException`, and connection state you can inspect.

## Documentation

Start at the [documentation index](docs/README.md), which is organised by what you are trying to
do. The most common destinations:

- [Getting started](docs/GETTING_STARTED.md) — install and publish a first note
- [API examples](docs/howto/api-examples.md) — worked examples of the common tasks
- [Private direct messages](docs/howto/private-direct-messages.md) — NIP-17 gift wrapping
- [Run the MCP server](docs/howto/run-the-mcp-server.md) — LLM agent access
- [API reference](docs/reference/nostr-java-api.md) — classes and methods
- [Architecture](docs/explanation/architecture.md) — how the modules fit together
- [Troubleshooting](docs/TROUBLESHOOTING.md) — when something is not working

## Building and testing

```bash
mvn verify              # full suite, including Testcontainers integration tests (needs Docker)
mvn -Pno-docker verify   # unit tests and non-Docker integration tests only
```

Integration tests run against a real relay in a container rather than a stand-in, because the
failures worth catching, such as frame ordering and relay-side validation, are precisely the
ones a fake reproduces incorrectly.

## Contributing

See the [codebase overview](docs/CODEBASE_OVERVIEW.md) for the module layout, build commands,
and the commit and pull request conventions, and
[the architecture guide](docs/explanation/architecture.md) for how the pieces fit together.
Release notes are in [CHANGELOG.md](CHANGELOG.md), and
[the migration guide](docs/MIGRATION.md) covers moving between major versions.

## License

MIT. See [LICENSE](LICENSE).
