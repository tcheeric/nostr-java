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
- [Integration Testing Issues](#integration-testing-issues)

---

## Installation Issues

### Problem: Dependency Not Found

**Symptom**: Maven or Gradle cannot resolve `xyz.tcheeric:nostr-java-client:<version>`

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

Exclude conflicting transitive dependencies if needed (version managed by the BOM):

```xml
<dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-client</artifactId>
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
new NostrRelayClient("https://relay.398ja.xyz");  // Wrong protocol
```

**Good:**
```java
new NostrRelayClient("wss://relay.398ja.xyz");
```

#### 2. Relay is Down or Unreachable

Test the relay URL independently:
```bash
# Using websocat (install: cargo install websocat)
websocat wss://relay.398ja.xyz

# Or use an online WebSocket tester
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

### Problem: Connection Drops Unexpectedly

**Symptom**: Subscription stops receiving events after a period

**Solution**: Use retry and error handling with `NostrRelayClient`:

```java
try (NostrRelayClient client = new NostrRelayClient("wss://relay.398ja.xyz")) {
    AutoCloseable subscription = client.subscribe(
        req,
        message -> System.out.println(message),
        error -> {
            System.err.println("Connection error: " + error.getMessage());
            // NostrRelayClient uses Spring Retry with exponential backoff
        },
        () -> System.out.println("Connection closed")
    );
}
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
GenericEvent event = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello")
    .build();
client.send(new EventMessage(event));  // Missing signature!
```

**Good:**
```java
GenericEvent event = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello")
    .build();
identity.sign(event);  // Sign first
client.send(new EventMessage(event));
```

#### 2. Event Modified After Signing

Never modify an event after signing it. Any change invalidates the signature.

#### 3. Incorrect Key Format

Ensure private keys are in the correct format (32-byte hex string, 64 hex characters):

```java
String validKey = "a".repeat(64);
Identity id = Identity.create(validKey);
```

---

## Event Publishing Issues

### Problem: Events Not Appearing on Relay

**Debugging Steps:**

#### 1. Verify Event Structure

```java
GenericEvent event = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello")
    .build();
identity.sign(event);

// Log the event JSON
String json = new EventMessage(event).encode();
System.out.println("Sending event: " + json);
```

#### 2. Check Relay Response

```java
List<String> responses = client.send(new EventMessage(event));
responses.forEach(response ->
    System.out.println("Relay response: " + response)
);
```

### Problem: Relay Timeout

**Symptom**: `RelayTimeoutException` when sending events

**Solution**: Increase the timeout or check relay connectivity:

```java
// Increase timeout to 2 minutes
NostrRelayClient client = new NostrRelayClient("wss://relay.398ja.xyz", 120_000);
```

Or configure via Spring properties:
```properties
nostr.websocket.await-timeout-ms=120000
```

---

## Subscription Issues

### Problem: Subscription Receives No Events

**Debugging Steps:**

#### 1. Verify Filter Configuration

```java
// Too restrictive — might match nothing
EventFilter tooRestrictive = EventFilter.builder()
    .authors(List.of(specificPubKey))
    .kinds(List.of(Kinds.TEXT_NOTE))
    .since(Instant.now().getEpochSecond())  // Only future events
    .build();

// More permissive — should match events
EventFilter permissive = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .limit(10)
    .build();
```

#### 2. Test with Basic Filters

```java
// Most widely supported
EventFilter basic = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .limit(5)
    .build();
```

### Problem: Subscription Callback Blocks

**Symptom**: Application becomes unresponsive or slow

**Note**: In nostr-java 2.0, subscription callbacks are dispatched on Virtual Threads, so blocking in callbacks does not block WebSocket I/O. However, for high-throughput feeds, consider using a bounded queue:

```java
BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>(1000);

client.subscribe(req,
    message -> {
        if (!eventQueue.offer(message)) {
            System.err.println("Queue full, dropping event");
        }
    },
    error -> System.err.println(error),
    () -> {}
);

// Process from queue at controlled rate
while (running) {
    String message = eventQueue.poll(1, TimeUnit.SECONDS);
    if (message != null) processMessage(message);
}
```

---

## Encryption & Decryption Issues

### Problem: Decryption Fails

**Symptom**: `NostrException` or garbled plaintext

**Possible Causes:**

#### 1. Wrong Private Key

Ensure you're using the recipient's private key to decrypt:

```java
Identity alice = Identity.generateRandomIdentity();
Identity bob = Identity.generateRandomIdentity();

// Alice encrypts for Bob
MessageCipher04 cipher = new MessageCipher04(alice.getPrivateKey(), bob.getPublicKey());
String encrypted = cipher.encrypt("Secret message");

// Bob decrypts (using his private key + Alice's public key)
MessageCipher04 bobCipher = new MessageCipher04(bob.getPrivateKey(), alice.getPublicKey());
String decrypted = bobCipher.decrypt(encrypted);
```

### Problem: NIP-44 vs NIP-04 Confusion

**Solution**: Match encryption and decryption versions:

```java
// NIP-04 (legacy)
MessageCipher04 cipher04 = new MessageCipher04(senderPriv, recipientPub);
String encrypted04 = cipher04.encrypt("Hello");

// NIP-44 (recommended)
MessageCipher44 cipher44 = new MessageCipher44(senderPriv, recipientPub);
String encrypted44 = cipher44.encrypt("Hello");

// Can't mix: cipher04.decrypt(encrypted44) will fail!
```

---

## Performance Issues

### Problem: Slow Event Publishing

**Solutions:**

#### Use Async APIs

```java
NostrRelayClient.connectAsync("wss://relay.398ja.xyz")
    .thenCompose(client -> client.sendAsync(new EventMessage(event)))
    .thenAccept(responses -> System.out.println("Done: " + responses));
```

### Problem: High Memory Usage

**Solutions:**

#### 1. Limit Subscription Results

```java
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .limit(100)
    .build();
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

---

## Integration Testing Issues

### Problem: Tests Timeout After 60 Seconds

**Solution**: Use strfry relay instead of nostr-rs-relay:

```properties
# src/test/resources/relay-container.properties
relay.container.image=dockurr/strfry:latest
relay.container.port=7777
```

### Problem: Tests Fail in CI but Pass Locally

**Possible Causes:**

1. **Docker not available**: Skip tests when Docker is unavailable:
```java
@DisabledIfSystemProperty(named = "noDocker", matches = "true")
```

2. **Resource constraints**: Use tmpfs for relay storage:
```java
.withTmpFs(Map.of("/app/strfry-db", "rw"))
```

---

## Getting More Help

If your issue isn't covered here:

1. **Check the API reference**: [reference/nostr-java-api.md](reference/nostr-java-api.md)
2. **Review examples**: [howto/api-examples.md](howto/api-examples.md)
3. **Search existing issues**: [GitHub Issues](https://github.com/tcheeric/nostr-java/issues)
4. **Open a new issue**: Provide nostr-java version, Java version, minimal reproducing code, and full stack trace.

## Debug Logging

For Spring Boot applications, add to `application.properties`:

```properties
logging.level.nostr=DEBUG
logging.level.nostr.client=TRACE
```
