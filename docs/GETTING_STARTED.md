# Getting started

This guide takes you from an empty project to a signed note published on a Nostr relay.

You need **Java 21 or later** and Maven. No Nostr account or API key: an identity is just a
keypair, and you generate one below.

Navigation: [Docs index](README.md) · [API examples](howto/api-examples.md) ·
[API reference](reference/nostr-java-api.md) · [Troubleshooting](TROUBLESHOOTING.md)

## 1. Add the dependency

Artifacts are published to `https://maven.398ja.xyz/releases`, with snapshots at
`https://maven.398ja.xyz/snapshots`.

Replace `X.Y.Z` below with the current release. The
[releases page](https://github.com/tcheeric/nostr-java/releases) and the badge at the top of the
[project README](../README.md) both show it. This guide deliberately does not hard-code a
version number, because a stale one here is worse than a placeholder: it looks copyable and
quietly gives you an old library.

Most applications want **`nostr-java-api`**. It is the client-facing entry point, and it pulls in
the transport, signing and event modules for you.

```xml
<repositories>
  <repository>
    <id>nostr-java</id>
    <url>https://maven.398ja.xyz/releases</url>
  </repository>
</repositories>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>xyz.tcheeric</groupId>
      <artifactId>nostr-java-bom</artifactId>
      <version>X.Y.Z</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-api</artifactId>
  </dependency>
</dependencies>
```

Importing the BOM is what lets you omit per-module versions. Every module then moves together,
which matters because they are released as a set.

**Gradle:**

```gradle
repositories {
    maven { url 'https://maven.398ja.xyz/releases' }
}

dependencies {
    implementation platform('xyz.tcheeric:nostr-java-bom:X.Y.Z')
    implementation 'xyz.tcheeric:nostr-java-api'
}
```

## 2. Publish your first note

```java
import nostr.api.NostrClient;
import nostr.client.relay.NoRelayAcceptedException;
import nostr.client.relay.PublishResult;
import nostr.id.Identity;

public class FirstNote {
  // NoRelayAcceptedException is checked, so you have to decide what to do when a note reaches
  // nobody. Declaring it here keeps the example short; a real application would catch it.
  public static void main(String[] args) throws NoRelayAcceptedException {
    // A Nostr identity is a keypair. Generating one is free and instant; there is nobody to
    // register with. Keep the private key if you want to post as this account again.
    Identity identity = Identity.generateRandomIdentity();
    System.out.println("posting as " + identity.getPublicKey().toBech32String());

    try (NostrClient nostr = NostrClient.builder()
        .identity(identity)
        .relays("wss://relay.398ja.xyz", "wss://nos.lol")
        .build()) {

      PublishResult result = nostr.publishTextNote("Hello from nostr-java");

      System.out.println("stored by: " + result.getAcceptingRelays());
      result.getFailures().forEach(failure ->
          System.out.println("refused by " + failure.relayUri() + ": " + failure.findReason().orElse("no reason given")));
    }
  }
}
```

Two things worth noticing, because they shape everything else in this library.

**Publishing is not all-or-nothing.** Nostr has no single source of truth, so a note goes to
several relays and each answers for itself. `PublishResult` reports every relay separately.
`publishTextNote` throws only when *no* relay accepted the event; if even one stored it, the note
is published and you should not send it again.

**The client owns connections.** It is `AutoCloseable`, so the try-with-resources block above
closes the relay sockets on exit. Outside a short example, build one client and keep it.

## 3. Read it back

```java
try (NostrClient nostr = NostrClient.builder()
    .identity(identity)
    .relays("wss://relay.398ja.xyz")
    .build()) {

  nostr.subscribe(
      List.of(EventFilter.builder().author(identity.getPublicKey().toHexString()).kind(1).build()),
      event -> System.out.println(event.getContent()));
}
```

Subscriptions de-duplicate across relays, so an event carried by three relays reaches your
listener once.

## Which module do I need?

`nostr-java-api` is the right answer unless you have a reason to go lower. Each module below it
is usable alone, and each drops what the one above it adds:

| Module | Use it when |
| --- | --- |
| `nostr-java-api` | You want to publish and subscribe. **Start here.** |
| `nostr-java-client` | You need direct control of relay connections and the pool |
| `nostr-java-identity` | You only need keys, signing and encryption, with no networking |
| `nostr-java-event` | You only need the event model and JSON, with no signing |
| `nostr-java-core` | You only need the cryptographic and encoding primitives |
| `nostr-java-mcp` | You are giving an LLM agent access, not writing Java against the API |

## Building from source

```bash
git clone https://github.com/tcheeric/nostr-java.git
cd nostr-java
mvn clean install
```

Running the full test suite needs Docker, because the integration tests publish to a real relay
in a container:

```bash
mvn verify              # everything
mvn -Pno-docker verify  # skips the container-backed tests
```

## Where next

- [API examples](howto/api-examples.md) — worked examples of the common tasks
- [Publishing across many relays](howto/multi-relay-publishing.md) — partial failure in depth
- [Private direct messages](howto/private-direct-messages.md) — NIP-17 encrypted messaging
- [Run the MCP server](howto/run-the-mcp-server.md) — give an LLM agent access
- [Troubleshooting](TROUBLESHOOTING.md) — when a relay will not accept your event
