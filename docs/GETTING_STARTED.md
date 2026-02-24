# Getting Started

Navigation: [Docs index](README.md) · [API how-to](howto/use-nostr-java-api.md) · [Streaming subscriptions](howto/streaming-subscriptions.md) · [API reference](reference/nostr-java-api.md) · [Codebase overview](CODEBASE_OVERVIEW.md)

## Prerequisites
- Maven
- Java 21+

## Building from Source

```bash
git clone https://github.com/tcheeric/nostr-java.git
cd nostr-java
mvn clean install
```

## Using Maven

Artifacts are published to `https://maven.398ja.xyz/releases` (and snapshots to `https://maven.398ja.xyz/snapshots`).

Use the BOM to align versions and omit per-module versions:

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
      <version><!-- X.Y.Z --></version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
 </dependencyManagement>

<dependencies>
  <!-- Pull in everything (client transitively includes identity, event, core) -->
  <dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-client</artifactId>
  </dependency>
</dependencies>
```

Or pick only the modules you need:

```xml
<dependencies>
  <!-- Just events and signing (no WebSocket client) -->
  <dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-identity</artifactId>
  </dependency>

  <!-- Just the event model (no signing, no client) -->
  <dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-event</artifactId>
  </dependency>
</dependencies>
```

Check the releases page for the latest BOM and module versions: https://github.com/tcheeric/nostr-java/releases

## Using Gradle

```gradle
repositories {
    maven { url 'https://maven.398ja.xyz/releases' }
}

dependencies {
    implementation platform('xyz.tcheeric:nostr-java-bom:X.Y.Z')
    implementation 'xyz.tcheeric:nostr-java-client'
}
```

Replace X.Y.Z with the latest version from the releases page.
