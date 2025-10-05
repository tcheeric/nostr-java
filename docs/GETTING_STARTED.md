# Getting Started

Navigation: [Docs index](README.md) · [API how‑to](howto/use-nostr-java-api.md) · [Streaming subscriptions](howto/streaming-subscriptions.md) · [API reference](reference/nostr-java-api.md) · [Codebase overview](CODEBASE_OVERVIEW.md)

## Prerequisites
- Maven
- Java 21+

## Building from Source

```bash
git clone https://github.com/tcheeric/nostr-java.git
cd nostr-java
./mvnw clean install
```

## Using Maven

Artifacts are published to `https://maven.398ja.xyz/releases`:

```xml
<repositories>
  <repository>
    <id>nostr-java</id>
    <url>https://maven.398ja.xyz/releases</url>
  </repository>
</repositories>

<dependency>
  <groupId>xyz.tcheeric</groupId>
  <artifactId>nostr-java-api</artifactId>
  <version>0.5.1</version>
</dependency>
```

Snapshot builds are available at `https://maven.398ja.xyz/snapshots`.

## Using Gradle

```gradle
repositories {
    maven { url 'https://maven.398ja.xyz/releases' }
}

dependencies {
    implementation 'xyz.tcheeric:nostr-java-api:0.5.1'
}
```

The current version is `0.5.1`. Check the [releases page](https://github.com/tcheeric/nostr-java/releases) for the latest version.

Examples are available in the [`nostr-java-examples`](../nostr-java-examples) module.
