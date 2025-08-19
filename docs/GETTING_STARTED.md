# Getting Started

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
  <version>[VERSION]</version>
</dependency>
```

Snapshot builds are available at `https://maven.398ja.xyz/snapshots`.

## Using Gradle

```gradle
repositories {
    maven { url 'https://maven.398ja.xyz/releases' }
}

dependencies {
    implementation 'xyz.tcheeric:nostr-java-api:[VERSION]'
}
```

Replace `[VERSION]` with the latest release number from the [releases page](https://github.com/tcheeric/nostr-java/releases).

Examples are available in the [`nostr-java-examples`](../nostr-java-examples) module.

