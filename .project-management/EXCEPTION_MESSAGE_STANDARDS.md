# Exception Message Standards

**Created:** 2025-10-07
**Purpose:** Standardize exception messages across nostr-java for better debugging and user experience

---

## Guiding Principles

1. **Be specific** - Include what failed and why
2. **Include context** - Add relevant values (IDs, names, types)
3. **Use consistent format** - Follow established patterns
4. **Be actionable** - Help developers understand how to fix the issue

---

## Standard Message Formats

### Pattern 1: "Failed to {action}: {reason}"

**Use for:** Operational failures (encoding, decoding, network, I/O)

**Examples:**
```java
// ✅ Good
throw new EventEncodingException("Failed to encode event message: invalid JSON structure");
throw new NostrNetworkException("Failed to connect to relay: connection timeout after 60s");
throw new NostrCryptoException("Failed to sign event [id=" + eventId + "]: private key is null");

// ❌ Bad
throw new RuntimeException(e);  // No context!
throw new EventEncodingException("Error");  // Too vague!
```

### Pattern 2: "Invalid {entity}: {reason}"

**Use for:** Validation failures

**Examples:**
```java
// ✅ Good
throw new IllegalArgumentException("Invalid event kind: must be between 0 and 65535, got " + kind);
throw new NostrProtocolException("Invalid event: missing required field 'content'");
throw new IllegalArgumentException("Invalid tag type: expected EventTag, got " + tag.getClass().getSimpleName());

// ❌ Bad
throw new IllegalArgumentException("The event is not a channel creation event");  // Use "Invalid" prefix
throw new IllegalArgumentException("tag must be of type RelaysTag");  // Use "Invalid" prefix
```

### Pattern 3: "Cannot {action}: {reason}"

**Use for:** Prevented operations (state issues)

**Examples:**
```java
// ✅ Good
throw new IllegalStateException("Cannot sign event: sender identity is required");
throw new IllegalStateException("Cannot verify event: event is not signed");
throw new NostrProtocolException("Cannot create zap request: relays tag or relay list is required");

// ❌ Bad
throw new IllegalStateException("Sender identity is required for zap operations");  // Use "Cannot" prefix
throw new IllegalStateException("The event is not signed");  // Use "Cannot" prefix
```

### Pattern 4: "{Entity} is/are {state}"

**Use for:** Simple state assertions

**Examples:**
```java
// ✅ Good - for simple cases
throw new NoSuchElementException("No matching p-tag found in event tags");
throw new IllegalArgumentException("Relay URL is null or empty");

// ✅ Also good - with more context
throw new NoSuchElementException("No matching p-tag found in event [id=" + eventId + "]");
```

---

## Exception Type Selection

### Use Domain Exceptions First

Prefer nostr-java domain exceptions over generic Java exceptions:

```java
// ✅ Preferred - Domain exceptions
throw new NostrProtocolException("Invalid event: ...");
throw new NostrCryptoException("Failed to sign: ...");
throw new NostrEncodingException("Failed to decode: ...");
throw new NostrNetworkException("Failed to connect: ...");

// ⚠️ Acceptable - Standard Java exceptions when appropriate
throw new IllegalArgumentException("Invalid parameter: ...");
throw new IllegalStateException("Cannot perform action: ...");
throw new NoSuchElementException("Element not found: ...");

// ❌ Avoid - Bare RuntimeException
throw new RuntimeException(e);  // Use specific exception type!
throw new RuntimeException("Something failed");  // Use NostrProtocolException or other domain exception
```

### Domain Exception Usage Guide

| Exception Type | When to Use | Example |
|----------------|-------------|---------|
| `NostrProtocolException` | NIP violations, invalid events/messages | Invalid event structure, missing required tags |
| `NostrCryptoException` | Signing, verification, encryption failures | Failed to sign event, invalid signature |
| `NostrEncodingException` | JSON, Bech32, hex encoding/decoding errors | Invalid JSON, malformed Bech32 |
| `NostrNetworkException` | Relay communication, timeouts, connection errors | Connection timeout, relay rejected event |
| `IllegalArgumentException` | Invalid method parameters | Null parameter, out of range value |
| `IllegalStateException` | Object state prevents operation | Event not signed, identity not set |
| `NoSuchElementException` | Expected element not found | Tag not found, subscription not found |

---

## Context Inclusion

### Include Relevant Context Values

**Good examples:**
```java
// Event ID
throw new NostrCryptoException("Failed to sign event [id=" + event.getId() + "]: private key is null");

// Kind value
throw new IllegalArgumentException("Invalid event kind [" + kind + "]: must be between 0 and 65535");

// Tag type
throw new IllegalArgumentException("Invalid tag type: expected " + expectedType + ", got " + actualType);

// Relay URL
throw new NostrNetworkException("Failed to connect to relay [" + relay.getUrl() + "]: " + cause);

// Field name
throw new NostrProtocolException("Invalid event: missing required field '" + fieldName + "'");
```

### Use String.format() for Complex Messages

```java
// ✅ Good - Readable with String.format
throw new NostrCryptoException(
    String.format("Failed to sign event [id=%s, kind=%d]: %s",
        event.getId(), event.getKind(), reason)
);

// ⚠️ Okay - String concatenation for simple messages
throw new IllegalArgumentException("Invalid kind: " + kind);
```

---

## Cause Chain Preservation

**Always preserve the original exception as the cause:**

```java
// ✅ Good - Preserve cause
try {
    mapper.writeValueAsString(event);
} catch (JsonProcessingException e) {
    throw new EventEncodingException("Failed to encode event message", e);
}

// ❌ Bad - Lost stack trace
try {
    mapper.writeValueAsString(event);
} catch (JsonProcessingException e) {
    throw new EventEncodingException("Failed to encode event message");  // Cause lost!
}

// ❌ Bad - No context
try {
    mapper.writeValueAsString(event);
} catch (JsonProcessingException e) {
    throw new RuntimeException(e);  // What operation failed?
}
```

---

## Common Patterns by Module

### Event Validation (nostr-java-event)

```java
// Required field validation
if (content == null) {
    throw new NostrProtocolException("Invalid event: missing required field 'content'");
}

// Kind range validation
if (kind < 0 || kind > 65535) {
    throw new IllegalArgumentException("Invalid event kind [" + kind + "]: must be between 0 and 65535");
}

// Signature validation
if (!event.verify()) {
    throw new NostrCryptoException("Failed to verify event [id=" + event.getId() + "]: invalid signature");
}
```

### Encoding/Decoding (nostr-java-event)

```java
// JSON encoding
try {
    return mapper.writeValueAsString(message);
} catch (JsonProcessingException e) {
    throw new EventEncodingException("Failed to encode " + messageType + " message", e);
}

// Bech32 decoding
try {
    return Bech32.decode(bech32String);
} catch (Exception e) {
    throw new NostrEncodingException("Failed to decode Bech32 string [" + bech32String + "]", e);
}
```

### API Operations (nostr-java-api)

```java
// State preconditions
if (sender == null) {
    throw new IllegalStateException("Cannot create event: sender identity is required");
}

// Type checking
if (!(tag instanceof RelaysTag)) {
    throw new IllegalArgumentException(
        "Invalid tag type: expected RelaysTag, got " + tag.getClass().getSimpleName()
    );
}

// Event type validation
if (event.getKind() != Kind.CHANNEL_CREATE.getValue()) {
    throw new IllegalArgumentException(
        "Invalid event: expected kind " + Kind.CHANNEL_CREATE + ", got " + event.getKind()
    );
}
```

---

## Migration Examples

### Before → After Examples

#### Example 1: Generic RuntimeException
```java
// ❌ Before
throw new RuntimeException(e);

// ✅ After
throw new NostrEncodingException("Failed to serialize event", e);
```

#### Example 2: Vague Message
```java
// ❌ Before
throw new IllegalArgumentException("The event is not a channel creation event");

// ✅ After
throw new IllegalArgumentException(
    "Invalid event: expected kind " + Kind.CHANNEL_CREATE + ", got " + event.getKind()
);
```

#### Example 3: Missing Context
```java
// ❌ Before
throw new IllegalStateException("Sender identity is required for zap operations");

// ✅ After
throw new IllegalStateException("Cannot create zap request: sender identity is required");
```

#### Example 4: No Cause Preservation
```java
// ❌ Before
try {
    algorithm.sign(data);
} catch (Exception e) {
    throw new RuntimeException("Signing failed");
}

// ✅ After
try {
    algorithm.sign(data);
} catch (Exception e) {
    throw new NostrCryptoException("Failed to sign event [id=" + eventId + "]: " + e.getMessage(), e);
}
```

---

## Audit Checklist

When reviewing exception throws:

- [ ] **Type:** Is a domain exception used? (NostrProtocolException, NostrCryptoException, etc.)
- [ ] **Format:** Does it follow a standard pattern? (Failed to.., Invalid.., Cannot..)
- [ ] **Context:** Are relevant values included? (IDs, kinds, types, URLs)
- [ ] **Cause:** Is the original exception preserved in the chain?
- [ ] **Actionable:** Can a developer understand what went wrong and how to fix it?

---

## Statistics

**Current Status (as of 2025-10-07):**
- Total exception throws: 209
- Following standard patterns: ~85% (estimated)
- Need improvement: ~15% (bare RuntimeException, vague messages, missing context)

**Priority Areas for Improvement:**
1. Replace bare `throw new RuntimeException(e)` with domain exceptions
2. Standardize validation messages to use "Invalid {entity}: {reason}" format
3. Add "Cannot {action}" prefix to IllegalStateException messages
4. Include context values (event IDs, kinds, types) where missing

---

**Last Updated:** 2025-10-07
**Status:** Standards defined, gradual adoption recommended
**Enforcement:** Code review + IDE inspections
