# nostr-java
[![](https://jitpack.io/v/xyz.tcheeric/nostr-java.svg)](https://jitpack.io/#xyz.tcheeric/nostr-java)

Nostr-java is a library for generating, signing, and publishing nostr events to relays.

## Requirements
- Maven
- Java 22+

## Usage
To use it in your project, add the following to your pom.xml file.

```xml
    <properties>
        <nostr-java.version>0.6.2</nostr-java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
```

```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

```xml
    <dependencies>
         <dependency>
            <groupId>nostr-java</groupId>
            <artifactId>nostr-java-api</artifactId>
            <version>${nostr-java.version}</version>
        </dependency>
    </dependencies>
```

## Examples
I recommend having a look at these repositories/module for examples:
  - [nostr-example](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-examples) module
  - [nostr-client](https://github.com/tcheeric/nostr-client) github repository
  - [SuperConductor](https://github.com/avlo/superconductor) nostr relay


## Supported NIPs
The following NIPs are supported by the API out-of-the-box:
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
- [NIP-99](https://github.com/nostr-protocol/nips/blob/master/99.md)