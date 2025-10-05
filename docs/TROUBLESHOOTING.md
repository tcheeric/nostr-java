# Troubleshooting Guide

Navigation: [Docs index](README.md) · [Getting started](GETTING_STARTED.md) · [API reference](reference/nostr-java-api.md)

This guide helps you diagnose and resolve common issues when using nostr-java.

## Table of Contents

- [Installation Issues](#installation-issues)
- [Connection Problems](#connection-problems)
- [Authentication & Signing Issues](#authentication--signing-issues)
- [Event Publishing Issues](#event-publishing-issues)
- [Subscription Issues](#subscription-issues)
- [Encryption & Decryption Issues](#encryption--decryption-issues)
- [Performance Issues](#performance-issues)

---

## Installation Issues

### Problem: Dependency Not Found

**Symptom**: Maven or Gradle cannot resolve `xyz.tcheeric:nostr-java-api:0.5.0`

**Solution**: Ensure you've added the custom repository to your build configuration:

**Maven:**
```xml
<repositories>
  <repository>
    <id>nostr-java</id>
    <url>https://maven.398ja.xyz/releases</url>
  </repository>
</repositories>
```

**Gradle:**
```gradle
repositories {
    maven { url 'https://maven.398ja.xyz/releases' }
}
```

### Problem: Java Version Mismatch

**Symptom**: `UnsupportedClassVersionError` or compilation errors

**Solution**: nostr-java requires Java 21 or higher. Verify your Java version:

```bash
java -version
```

If needed, update your build configuration:

**Maven (`pom.xml`):**
```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

**Gradle (`build.gradle`):**
```gradle
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

### Problem: Conflicting Dependencies

**Symptom**: `ClassNotFoundException` or `NoSuchMethodError` at runtime

**Solution**: Check for dependency conflicts, especially with Spring WebSocket or JSON libraries:

```bash
# Maven
mvn dependency:tree

# Gradle
gradle dependencies
```

Exclude conflicting transitive dependencies if needed:

```xml
<dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-api</artifactId>
    <version>0.5.0</version>
    <exclusions>
        <exclusion>
            <groupId>conflicting-group</groupId>
            <artifactId>conflicting-artifact</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## Connection Problems

### Problem: WebSocket Connection Fails

**Symptom**: `IOException`, `ConnectException`, or timeouts when connecting to relay

**Possible Causes & Solutions:**

#### 1. Invalid Relay URL

Ensure the relay URL uses the correct WebSocket protocol:
- Use `wss://` for secure connections (recommended)
- Use `ws://` only for local development (e.g., `ws://localhost:5555`)

**Bad:**
```java
Map<String, String> relays = Map.of("relay", "https://relay.398ja.xyz");  // Wrong protocol
```

**Good:**
```java
Map<String, String> relays = Map.of("relay", "wss://relay.398ja.xyz");
```

#### 2. Relay is Down or Unreachable

Test the relay URL independently:
```bash
# Using websocat (install: cargo install websocat)
websocat wss://relay.398ja.xyz

# Or use an online WebSocket tester
# https://www.websocket.org/echo.html
```

Try alternative public relays:
- `wss://relay.398ja.xyz`
- `wss://nos.lol`
- `wss://relay.nostr.band`

#### 3. Firewall or Proxy Blocking WebSocket

If behind a corporate firewall, configure proxy settings:

```java
System.setProperty("https.proxyHost", "proxy.example.com");
System.setProperty("https.proxyPort", "8080");
```

#### 4. SSL/TLS Certificate Issues

**Symptom**: `SSLHandshakeException`

For self-signed certificates in development only:
```java
// WARNING: Only use in development, never in production
System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
```

### Problem: Connection Drops Unexpectedly

**Symptom**: Subscription stops receiving events after a period

**Solution**: Implement retry logic and connection monitoring:

```java
NostrSpringWebSocketClient client = new NostrSpringWebSocketClient();
client.setRelays(relays);

AutoCloseable subscription = client.subscribe(
    filters,
    "my-subscription",
    message -> System.out.println(message),
    error -> {
        System.err.println("Connection error: " + error.getMessage());
        // Implement reconnection logic here
        // Consider exponential backoff
    }
);
```

---

## Authentication & Signing Issues

### Problem: Event Signature Verification Fails

**Symptom**: Relay rejects event with signature error

**Possible Causes:**

#### 1. Event Not Signed

Ensure you sign the event before sending:

**Bad:**
```java
GenericEvent event = new GenericEvent(pubKey, Kind.TEXT_NOTE, List.of(), "Hello");
client.send(new EventMessage(event));  // Missing signature!
```

**Good:**
```java
GenericEvent event = new GenericEvent(pubKey, Kind.TEXT_NOTE, List.of(), "Hello");
identity.sign(event);  // Sign first
client.send(new EventMessage(event));
```

#### 2. Event Modified After Signing

Never modify an event after signing it. Any change invalidates the signature.

**Bad:**
```java
identity.sign(event);
event.setContent("Different content");  // Signature now invalid!
client.send(new EventMessage(event));
```

#### 3. Incorrect Key Format

Ensure private keys are in the correct format (32-byte hex string):

```java
// Valid 32-byte hex key (64 hex characters)
String validKey = "a".repeat(64);
Identity id = Identity.create(validKey);

// Invalid - too short
String invalidKey = "abc123";  // Will throw exception
```

### Problem: Identity Generation Fails

**Symptom**: `NostrException` or invalid key errors

**Solution**: Use the provided identity generation methods:

```java
// Recommended: Generate random identity
Identity identity = Identity.generateRandomIdentity();

// From existing private key (hex string)
String privateKeyHex = "...";
Identity identity = Identity.create(privateKeyHex);

// Verify the identity
PublicKey pubKey = identity.getPublicKey();
System.out.println("Public key: " + pubKey.toString());
```

---

## Event Publishing Issues

### Problem: Events Not Appearing on Relay

**Symptom**: Event sent successfully but doesn't appear in queries

**Debugging Steps:**

#### 1. Verify Event Structure

Check the event JSON before sending:

```java
GenericEvent event = new GenericEvent(pubKey, kind, tags, content);
identity.sign(event);

// Log the event JSON
String json = new EventMessage(event).encode();
System.out.println("Sending event: " + json);

client.send(new EventMessage(event));
```

#### 2. Check Relay Response

Many relays send OK messages. Listen for them:

```java
List<String> responses = client.send(new EventMessage(event));
responses.forEach(response ->
    System.out.println("Relay response: " + response)
);
```

#### 3. Verify Event ID Calculation

The event ID must be calculated correctly:

```java
// Event ID is automatically calculated during signing
identity.sign(event);
System.out.println("Event ID: " + event.getId());
```

### Problem: "Invalid Event Kind" Error

**Symptom**: Relay rejects event with kind error

**Solution**: Ensure you're using a valid kind number per [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md):

- **Regular (1-9999)**: Standard events that can be deleted
- **Replaceable (10000-19999)**: Newer event replaces older ones
- **Ephemeral (20000-29999)**: Not stored by relays
- **Parameterized Replaceable (30000-39999)**: Replaceable with parameters

```java
// Valid kinds
int TEXT_NOTE = 1;           // Regular
int METADATA = 0;            // Regular
int CONTACTS = 3;            // Replaceable (10000-19999 range in older spec, but 3 is special)
int CUSTOM_KIND = 30000;     // Parameterized replaceable

// Use appropriate kind for your use case
GenericEvent event = new GenericEvent(pubKey, CUSTOM_KIND, tags, content);
```

---

## Subscription Issues

### Problem: Subscription Receives No Events

**Symptom**: Subscription opens successfully but callback never fires

**Debugging Steps:**

#### 1. Verify Filter Configuration

Check that your filters match existing events:

```java
// Too restrictive - might match nothing
Filters tooRestrictive = new Filters(
    new AuthorFilter(specificPubKey),
    new KindFilter<>(Kind.TEXT_NOTE),
    new SinceFilter(Instant.now().getEpochSecond())  // Only future events
);

// More permissive - should match events
Filters permissive = new Filters(
    new KindFilter<>(Kind.TEXT_NOTE)
);
permissive.setLimit(10);  // Limit results
```

#### 2. Test with Known Events

Query for a specific event you know exists:

```java
Filters filters = new Filters(new IdsFilter(knownEventId));
client.subscribe(filters, "test-sub",
    message -> System.out.println("Found: " + message),
    error -> System.err.println("Error: " + error)
);
```

#### 3. Check Relay Supports Filter Type

Not all relays support all filter types. Test with basic filters first:

```java
// Most widely supported
Filters basic = new Filters(new KindFilter<>(Kind.TEXT_NOTE));
basic.setLimit(5);
```

### Problem: Subscription Callback Blocks

**Symptom**: Application becomes unresponsive or slow

**Solution**: Offload heavy processing from the WebSocket thread:

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

ExecutorService executor = Executors.newFixedThreadPool(4);

AutoCloseable subscription = client.subscribe(
    filters,
    "my-sub",
    message -> {
        // Hand off to executor immediately
        executor.submit(() -> {
            // Heavy processing here
            processMessage(message);
        });
    },
    error -> System.err.println(error)
);

// Don't forget to shut down executor
executor.shutdown();
```

### Problem: Too Many Events Causing Backpressure

**Symptom**: Memory usage grows, events arrive faster than processing

**Solution**: Implement flow control:

```java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>(1000);  // Max 1000 events

client.subscribe(
    filters,
    "my-sub",
    message -> {
        if (!eventQueue.offer(message)) {
            System.err.println("Queue full, dropping event");
        }
    },
    error -> System.err.println(error)
);

// Process from queue at controlled rate
while (running) {
    String message = eventQueue.poll(1, TimeUnit.SECONDS);
    if (message != null) {
        processMessage(message);
    }
}
```

---

## Encryption & Decryption Issues

### Problem: Decryption Fails for NIP-04 Messages

**Symptom**: `NostrException` or garbled plaintext

**Possible Causes:**

#### 1. Wrong Private Key

Ensure you're using the recipient's private key to decrypt:

```java
// Alice sends to Bob
Identity alice = Identity.generateRandomIdentity();
Identity bob = Identity.generateRandomIdentity();

NIP04 dm = new NIP04(alice, bob.getPublicKey())
    .createDirectMessageEvent("Secret message");

// Bob must use his identity to decrypt
String plaintext = NIP04.decrypt(bob, dm.getEvent());  // Correct

// This would fail:
// String plaintext = NIP04.decrypt(alice, dm.getEvent());  // Wrong!
```

#### 2. Corrupted Ciphertext

Verify the event content wasn't modified:

```java
try {
    String decrypted = NIP04.decrypt(identity, event);
    System.out.println(decrypted);
} catch (NostrException e) {
    System.err.println("Decryption failed - content may be corrupted");
    e.printStackTrace();
}
```

### Problem: NIP-44 vs NIP-04 Confusion

**Symptom**: Decryption fails with wrong cipher version

**Solution**: Match encryption and decryption versions:

```java
// NIP-04 (legacy)
MessageCipher04 cipher04 = new MessageCipher04(senderPriv, recipientPub);
String encrypted04 = cipher04.encrypt("Hello");
String decrypted04 = cipher04.decrypt(encrypted04);

// NIP-44 (recommended)
MessageCipher44 cipher44 = new MessageCipher44(senderPriv, recipientPub);
String encrypted44 = cipher44.encrypt("Hello");
String decrypted44 = cipher44.decrypt(encrypted44);

// Can't mix: cipher04.decrypt(encrypted44) will fail!
```

---

## Performance Issues

### Problem: Slow Event Publishing

**Symptom**: High latency when sending events

**Solutions:**

#### 1. Batch Events When Possible

```java
List<EventMessage> events = List.of(
    new EventMessage(event1),
    new EventMessage(event2),
    new EventMessage(event3)
);

// Send in parallel to multiple relays
events.forEach(event -> client.send(event));
```

#### 2. Use Async Publishing

```java
import java.util.concurrent.CompletableFuture;

CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    client.send(new EventMessage(event));
});

// Continue other work
future.join();  // Wait when needed
```

### Problem: High Memory Usage

**Symptom**: Application memory grows continuously

**Solutions:**

#### 1. Limit Subscription Results

```java
Filters filters = new Filters(new KindFilter<>(Kind.TEXT_NOTE));
filters.setLimit(100);  // Limit to 100 most recent events
```

#### 2. Close Subscriptions When Done

```java
AutoCloseable subscription = client.subscribe(/* ... */);

try {
    // Use subscription
} finally {
    subscription.close();  // Always close!
}
```

#### 3. Clear References to Large Objects

```java
// Don't hold references to all events
client.subscribe(filters, "sub", message -> {
    processMessage(message);
    // Don't: allMessages.add(message);  // Memory leak!
});
```

---

## Getting More Help

If your issue isn't covered here:

1. **Check the API reference**: [reference/nostr-java-api.md](reference/nostr-java-api.md)
2. **Review examples**: Browse the [`nostr-java-examples`](../nostr-java-examples) module
3. **Search existing issues**: [GitHub Issues](https://github.com/tcheeric/nostr-java/issues)
4. **Open a new issue**: Provide:
   - nostr-java version (`0.5.0`)
   - Java version (`java -version`)
   - Minimal code to reproduce
   - Full error stack trace
   - Expected vs actual behavior

## Debug Logging

Enable debug logging to diagnose issues:

```java
import java.util.logging.Logger;
import java.util.logging.Level;

Logger logger = Logger.getLogger("nostr");
logger.setLevel(Level.FINE);

// Or configure via logging.properties
// nostr.level = FINE
```

For Spring Boot applications, add to `application.properties`:

```properties
logging.level.nostr=DEBUG
logging.level.nostr.client=TRACE
```
