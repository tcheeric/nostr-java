# nostr-java
[![](https://jitpack.io/v/tcheeric/nostr-java.svg)](https://jitpack.io/#tcheeric/nostr-java)

Introducing the nostr-java library, a solution written in java for generating, signing, and publishing nostr events to relays.

## Requirements
- Maven
- Java 19+

## Usage
To use the library in your project, add the following dependency to your pom.xml file:
```xml
<dependency>
    <groupId>com.github.tcheeric.nostr-java</groupId>
    <artifactId>nostr-java-api</artifactId>
    <version>${nostr.java.version}</version>
</dependency>
```

I recommend having a look at:
  - [nostr-example](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-examples) module
  - [nostr-client](https://github.com/tcheeric/nostr-client/) project
  - [SuperConductor](https://github.com/avlo/superconductor) nostr relay

for simple examples on how to use the library.

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

## Dev Discussion Group:
- Nostr Public Channel: nostr:nevent1qqszqdmxg26sehmnyrcu2ler8azz6wyj6fh0qg3ad5fnnm6xfqqvhzcppamhxue69uhkummnw3ezumt0d5pzpl7nwh45p66gvet2q28dhjpcyh6clux4cjsm5gh7waza9pzjnmgglv06ew
