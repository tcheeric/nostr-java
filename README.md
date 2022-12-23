# nostr-java
A nostr client API written in java, for generating, signing and publishing events to relays.
The API assumes the relay supports NIP-11, it uses the relay information for publishing the events to the relay (or not!):
1. Unsupported events will not be published to the relay.
2. Unsupported tags will be discarded from the event before being published.

## Usage
Have a look at [NostrExamples.java](https://github.com/tcheeric/nostr-java/blob/main/nostr-examples/src/main/java/nostr/examples/NostrExamples.java) in the [nostr-examples](https://github.com/tcheeric/nostr-java/tree/main/nostr-examples) folder for more details.

## Currently Supported NIPS:
 1. NIP-01
 2. NIP-02
 3. NIP-03
 5. NIP-05
 6. NIP-08
 7. NIP-09
 8. NIP-10
 9. NIP-14
 10. NIP-16
 11. NIP-19 (Partial: Bare keys and ids)
 12. NIP-25
 13. NIP-26

## Known Issues:
 1. NIP-04

## Work In Progress:
 1. NIP-15
 2. NIP-20

## TODO:
1. NIP-12
2. NIP-13
3. NIP-28
4. NIP-35
5. NIP-36