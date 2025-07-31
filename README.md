# nostr-java

`nostr-java` is a Java SDK for the [Nostr](https://github.com/nostr-protocol/nips) protocol. It provides utilities for creating, signing and publishing Nostr events to relays.

## Requirements
- Maven
- Java 21+

## Getting Started
### Using JitPack
Add the dependency and repository to your `pom.xml`:

```xml
<properties>
    <nostr-java.version>v0.007.2-alpha</nostr-java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>nostr-java</groupId>
        <artifactId>nostr-java-api</artifactId>
        <version>${nostr-java.version}</version>
    </dependency>
</dependencies>
```

### Building from source
Clone the repository and build the modules:

```bash
$ git clone https://github.com/tcheeric/nostr-java.git
$ cd nostr-java
$ ./mvnw clean install
```

See [`docs/CODEBASE_OVERVIEW.md`](docs/CODEBASE_OVERVIEW.md) for details about running tests and contributing.

## Examples
Example usages are located in the [`nostr-java-examples`](./nostr-java-examples) module. Additional demonstrations can be found in [nostr-client](https://github.com/tcheeric/nostr-client) and [SuperConductor](https://github.com/avlo/superconductor).

Each concrete event verifies that its `kind` matches the expected value from the `Kind` enum. Calling `validate()` on an event with an incorrect kind throws an `AssertionError`.

## Supported NIPs
The API currently implements the following [NIPs](https://github.com/nostr-protocol/nips):
- [NIP-1](https://github.com/nostr-protocol/nips/blob/master/01.md)
- [NIP-2](https://github.com/nostr-protocol/nips/blob/master/02.md)
- [NIP-3](https://github.com/nostr-protocol/nips/blob/master/03.md)
- [NIP-4](https://github.com/nostr-protocol/nips/blob/master/04.md)
- [NIP-5](https://github.com/nostr-protocol/nips/blob/master/05.md)
- [NIP-8](https://github.com/nostr-protocol/nips/blob/master/08.md)
- [NIP-9](https://github.com/nostr-protocol/nips/blob/master/09.md)
- [NIP-12](https://github.com/nostr-protocol/nips/blob/master/12.md)
- [NIP-14](https://github.com/nostr-protocol/nips/blob/master/14.md)
- [NIP-15](https://github.com/nostr-protocol/nips/blob/master/15.md)
- [NIP-20](https://github.com/nostr-protocol/nips/blob/master/20.md)
- [NIP-23](https://github.com/nostr-protocol/nips/blob/master/23.md)
- [NIP-25](https://github.com/nostr-protocol/nips/blob/master/25.md)
- [NIP-28](https://github.com/nostr-protocol/nips/blob/master/28.md)
- [NIP-30](https://github.com/nostr-protocol/nips/blob/master/30.md)
- [NIP-32](https://github.com/nostr-protocol/nips/blob/master/32.md)
- [NIP-40](https://github.com/nostr-protocol/nips/blob/master/40.md)
- [NIP-42](https://github.com/nostr-protocol/nips/blob/master/42.md)
- [NIP-44](https://github.com/nostr-protocol/nips/blob/master/44.md)
- [NIP-46](https://github.com/nostr-protocol/nips/blob/master/46.md)
- [NIP-57](https://github.com/nostr-protocol/nips/blob/master/57.md)
- [NIP-60](https://github.com/nostr-protocol/nips/blob/master/60.md)
- [NIP-61](https://github.com/nostr-protocol/nips/blob/master/61.md)
- [NIP-99](https://github.com/nostr-protocol/nips/blob/master/99.md)
