# nostr-event

We offer support for selected events and tags out of the box:

The events:
- Event Deletion
- Encrypted Direct Messages
- Ephemeral Events
- Internet Identifier Metadata Events
- Mentions
- Set Metadata events
- Ots Events
- Reaction Events
- Replaceable Events
- Text Note Events

The Tags:
- Deletion Tag
- Event Tag
- Nonce Tag
- Pubkey Tag
- Subject Tag

Additionally, you may use the `GenericTag` and `GenericEvent` classes to create your custom tags and events.

## Creating a Custom Tag
Consider the tag syntax:
`[<code>, <attribute value 0>, <attribute value 1>, ..., <attribute value n>]`

Given:
- The tag is specified in a NIP
- The tag has a code and zero or more attributes
- An attribute may be specified by another NIP
- The tag is related to a parent event

Practical example:
For illustration purpose, we will implement a tag defined in **NIP-777** and with three attributes, the last attribute is specified in **NIP-888**

Here is the corresponding java code:

```java
    // Create the attributes...
    // ...using the static builder method
    final ElementAttribute attr0 = ElementAttribute.builder().value(new StringValue("value 0")).build();
    
    //...by invoking the constructors
    final ElementAttribute attr1 = new ElementAttribute("value 1");
    final ElementAttribute attr2 = new ElementAttribute("value 2", 888);;                        
    
    Set<ElementAttribute> attributes = new HashSet<>();
    attributes.add(attr0);
    attributes.add(attr1);
    attributes.add(attr2);

    // Create the tag
    GenericTag tag = new GenericTag(777, "code", attributes);
    System.out.println(tag.toString()); //["code", "value 0", "value 1", "value 2"]
    
    // Create the parent event
    TagList tags = new TagList();
    tags.add(tag);
    TextNoteEvent event = new TextNoteEvent(publicKey, tags, "Hello Nostr!");
```

## Creating a Custom Event

See my [implementation](https://github.com/tcheeric/nostr-java/blob/main/nostr-event/src/main/java/nostr/event/impl/OtsEvent.java) of the [OTS Event](https://github.com/nostr-protocol/nips/blob/master/03.md) as a cusom event.
Also, review my simple [json implementation](https://github.com/tcheeric/nostr-java/tree/0bd9a8858705e5d39ab34706ea23a584f5dfc9b6/nostr-json).