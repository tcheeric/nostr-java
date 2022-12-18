# nostr-java
A nostr client API, written in java, for generating, signing and publishing events to relays.
The API assumes the relay supports NIP-11, it uses the relay information for publishing (or not!) the events to the relay:
1. Unsupported events will not be published to the relay.
2. Unsupported tags will be discarded from the event before being published.


## Currently Supported NIPS:
 1. NIP-01
 2. NIP-02
 3. NIP-03
 5. NIP-05
 6. NIP-08
 7. NIP-09
 8. NIP-10
 9. NIP-14
 11. NIP-16
 13. NIP-25
 14. NIP-26

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