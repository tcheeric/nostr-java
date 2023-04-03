# nostr-java
A nostr client library written in java, for generating, signing and publishing events to relays.
1. Unsupported events will not be published to the relay.
2. Unsupported tags will be discarded from the event before being published.

## Usage
Have a look at [NostrExamples.java](https://github.com/tcheeric/nostr-java/blob/main/nostr-examples/src/main/java/nostr/examples/NostrExamples.java) in the [nostr-examples](https://github.com/tcheeric/nostr-java/tree/main/nostr-examples) folder for more details.

## Currently Supported NIPS:
 1. NIP-01
 2. NIP-02
 3. NIP-03
 4. NIP-04
 5. NIP-05
 6. NIP-08
 7. NIP-09
 8. NIP-10
 9. NIP-12
 10. NIP-14
 11. NIP-16
 12. NIP-19 (Partial: Bare keys and ids)
 13. NIP-25
 14. NIP-26
 15. NIP-28