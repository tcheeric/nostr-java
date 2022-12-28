We support out of the box a few events and tags:

The events:
Event Deletion
Encrypted Direct Messages
Ephemeral Events
Internet Identifier Metadata Events
Mentions
Set Metadata events
Ots Events
Reaction Events
Replaceable Events
Text Note Events

The Tags:
Deletion Tag
Event Tag
Nonce Tag
Pubkey Tag
Subject Tag

Additionally, you may use GenericTag and GenericEvent classes to create custom tags and events.

Creating a custom Tag.

Consider the NIP-36 specifying the content-warning tag.

Here is the string representation of the tag:
["code", "attribute value 1", "attribute value 2", ..., "attribute value n"] 


Here is the corresponding java code:

```java

```