# nostr-java
[![](https://jitpack.io/v/tcheeric/nostr-java.svg)](https://jitpack.io/#tcheeric/nostr-java)

Introducing the nostr-java library, a solution written in java for generating, signing, and publishing nostr events to relays.

## Requirements
- Java 19. (Java 21 is currently not supported, due to a conflict with lombok. See issue [#101](https://github.com/tcheeric/nostr-java/issues/101)
- A Nostr relay to connect to. This is needed for the [unit tests](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-test). You can either run your own relay, or use a public one. To configure your test relay, update the [relays.properties](https://github.com/tcheeric/nostr-java/blob/main/nostr-java-test/src/test/resources/relays.properties) resource file in the test module.

## Building
We use Maven to build the project. To build the project, run the following command in the root directory of the project:

```bash
mvn clean install
```

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

We also provide the classes [GenericEvent](https://github.com/tcheeric/nostr-java/blob/main/nostr-java-event/src/main/java/nostr/event/impl/GenericEvent.java) and [GenericTag](https://github.com/tcheeric/nostr-java/blob/main/nostr-java-event/src/main/java/nostr/event/impl/GenericTag.java) for creating events and tags that are currently not supported out-of-the-box.
See working example [here](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-examples)

Additional reading:
- [nostr-java-api](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-api)
- [nostr-java-id](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-id)
- [nostr-java-examples](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-examples)
