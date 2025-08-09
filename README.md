# nostr-java
[![CI](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml/badge.svg)](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml)

`nostr-java` is a Java SDK for the [Nostr](https://github.com/nostr-protocol/nips) protocol. It provides utilities for creating, signing and publishing Nostr events to relays.

## Requirements
- Maven
- Java 21+

## Getting Started
Clone the repository and build the modules. Installing them locally allows you to add the artifacts as dependencies in your own projects:

```bash
$ git clone https://github.com/tcheeric/nostr-java.git
$ cd nostr-java
$ ./mvnw clean install
```

See [`docs/CODEBASE_OVERVIEW.md`](docs/CODEBASE_OVERVIEW.md) for details about running tests and contributing.

## Using Published Artifacts
Artifacts are published to GitHub Packages and can be consumed from Maven by adding the repository and desired dependency to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/OWNER/REPO</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-api</artifactId>
    <version>${nostr-java.version}</version>
  </dependency>
</dependencies>
```

Authenticating to GitHub Packages is required; provide a personal access token with the appropriate scopes or `GITHUB_TOKEN` credentials. See the [GitHub Packages documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) for more details.

## CI and Releases
The project uses GitHub Actions defined in [ci.yml](https://github.com/tcheeric/nostr-java/actions/workflows/ci.yml).
This workflow runs `mvn -q verify` to build the project and execute all tests on each push and pull request.
Releases are published using the [release.yml](https://github.com/tcheeric/nostr-java/actions/workflows/release.yml) workflow.

## Examples
Example usages are located in the [`nostr-java-examples`](./nostr-java-examples) module. Additional demonstrations can be found in [nostr-client](https://github.com/tcheeric/nostr-client) and [SuperConductor](https://github.com/avlo/superconductor).

## Retry Support
`SpringWebSocketClient` leverages Spring Retry so that failed WebSocket send operations are attempted up to three times with exponential backoff.

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
