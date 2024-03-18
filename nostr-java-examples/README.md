# nostr-java-examples
This is a collection of examples for the Nostr Java API. It demonstrates how to use the API to create, sign, and publish events to relays.

## Creating an OOTB Nostr Event
Here is an example illustrating how to create a TextNoteEvent using the Nostr Java API, and submitting it to relays.
1. Create a sender [identity](https://github.com/tcheeric/nostr-java/tree/main/nostr-java-id). In the example below, we generate a random identity, but you may create an identity from a private key, or use the default identity, if setup.
2. You create a list of tags. This is an optional step. In the example below, we create a list of tags with a single PubKeyTag, which is the recipient's public key.
3. You create a NIP01 object, and call the createTextNoteEvent method, passing the list of tags and the message, then sign and send it.

```java
private static final Identity SENDER = Identity.generateRandomIdentity();
private final static Map<String, String> RELAYS = Map.of("lol", "nos.lol", "damus", "relay.damus.io", "ZBD",
        "nostr.zebedee.cloud", "taxi", "relay.taxi", "mom", "nostr.mom");

private static TextNoteEvent sendTextNoteEvent() {
logHeader("sendTextNoteEvent");

        List<BaseTag> tags = new ArrayList<>(List.of(new PubKeyTag(RECIPIENT.getPublicKey())));

        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        nip01.createTextNoteEvent(tags, "Hello world, I'm here on nostr-java API!")
        		.sign()
        		.send(RELAYS);
        
        return nip01.getEvent();
    }

```

## Creating a custom Nostr Event and Tag
We use the ```nostr.api.EventNostr.GenericEventNostr.createGenericEvent(@NonNull Integer kind, @NonNull String content)``` method to create custom events, i.e. events not supported out-of-the-box by the nostr-java library. The example below re-creates the TextNoteEvent as a custom event, and a custom tag, then sends the event to relays.
The alt-tag is created using the TagFactory class, which is a helper class for creating custom tags. The TagFactory class has a method for creating each type of tag. In the example below, we create a custom tag with a tag name of "alt", a tag kind of 31, and a tag content of "an alt text". ```["alt", 31, "an alt text"]```.

```java
private static TextNoteEvent sendTextNoteEvent() {
logHeader("sendTextNoteEvent");

        var event = new GenericEventNostr(SENDER);
        nip01.createTextNoteEvent(1, "Hello world, I'm here on nostr-java API!")
                .addTag(new TagFactory("alt", 31, "an alt text").create())
        		.sign()
        		.send(RELAYS);
        
        return nip01.getEvent();
    }
```