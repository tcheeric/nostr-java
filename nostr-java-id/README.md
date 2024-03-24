# nostr-java-id
Identities represent nsec, npub pairs. They are used to sign events and to verify the signature of events. The Nostr Java API provides a default identity, which is used if no identity is provided. You may also create an identity from a private key, generate a random identity, or create a custom identity.

## Creating an Identity from a private key (in hex format)
```java
Identity.getInstance(String privateKey);
```

## Creating a random identity
You use to create a random identity by calling the ```Identity.generateRandomIdentity()``` method.
