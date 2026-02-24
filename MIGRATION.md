# Migration Guide

This guide helps you migrate between major versions of nostr-java, detailing breaking changes and API replacements.

---

## Table of Contents

- [Migrating to 2.0.0](#migrating-to-200)
- [Migrating to 1.0.0](#migrating-to-100) (historical)

---

## Migrating to 2.0.0

**Status:** Released 2026-02-24

Version 2.0.0 is a major simplification that reduces the library from 9 modules (~180 classes) to 4 modules (~40 classes). See [CHANGELOG.md](CHANGELOG.md) for the full list of changes and [docs/developer/SIMPLIFICATION_PROPOSAL.md](docs/developer/SIMPLIFICATION_PROPOSAL.md) for the design rationale.

### Module changes

| Old Module | New Module | Notes |
|---|---|---|
| `nostr-java-util` + `nostr-java-crypto` | `nostr-java-core` | Merged |
| `nostr-java-base` + `nostr-java-event` | `nostr-java-event` | Merged |
| `nostr-java-id` + `nostr-java-encryption` | `nostr-java-identity` | Merged |
| `nostr-java-client` | `nostr-java-client` | Unchanged |
| `nostr-java-api` | (removed) | Use `GenericEvent` directly |
| `nostr-java-examples` | (removed) | See docs/howto guides |

**Dependency:** Replace `nostr-java-api` with `nostr-java-client` (transitively includes all modules):

```xml
<!-- Before -->
<artifactId>nostr-java-api</artifactId>

<!-- After -->
<artifactId>nostr-java-client</artifactId>
```

### Event creation

```java
// Before (1.x)
NIP01 nip01 = new NIP01(identity);
nip01.createTextNoteEvent("Hello Nostr!")
     .sign()
     .send(relays);

// After (2.0)
GenericEvent event = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(Kinds.TEXT_NOTE)
    .content("Hello Nostr!")
    .build();
identity.sign(event);
try (NostrRelayClient client = new NostrRelayClient("wss://relay.398ja.xyz")) {
    client.send(new EventMessage(event));
}
```

### Tags

```java
// Before (1.x)
EventTag eTag = new EventTag("abc123", "wss://relay.example.com", Marker.REPLY);
PubKeyTag pTag = new PubKeyTag(recipientPublicKey);
HashtagTag hTag = new HashtagTag("nostr");

// After (2.0)
GenericTag.of("e", "abc123", "wss://relay.example.com", "reply")
GenericTag.of("p", recipientPublicKey.toString())
GenericTag.of("t", "nostr")
```

### Kind values

```java
// Before (1.x)
Kind.TEXT_NOTE          // Kind enum
Kind.TEXT_NOTE.getValue() // to get int

// After (2.0)
Kinds.TEXT_NOTE         // static int constant (value: 1)
```

### Filters

```java
// Before (1.x)
new Filters(
    new KindFilter<>(Kind.TEXT_NOTE),
    new AuthorFilter<>(pubKey),
    new SinceFilter(timestamp)
);

// After (2.0)
EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .authors(List.of(pubKeyHex))
    .since(timestamp)
    .build();
```

### WebSocket client

```java
// Before (1.x) — StandardWebSocketClient or SpringWebSocketClient
StandardWebSocketClient client = new StandardWebSocketClient("wss://relay.example.com");
// or: SpringWebSocketClient client = new SpringWebSocketClient(wsClient);

// After (2.0) — NostrRelayClient (with retry, VT dispatch, async APIs)
NostrRelayClient client = new NostrRelayClient("wss://relay.example.com");
// Async:
NostrRelayClient.connectAsync("wss://relay.example.com");
```

### Timeout handling

```java
// Before (1.x) — silent empty list on timeout
List<String> events = client.send(message);  // returns empty list on timeout

// After (2.0) — throws RelayTimeoutException
try {
    List<String> events = client.send(message);
} catch (RelayTimeoutException e) {
    // explicit timeout handling
}
```

### Removed classes (summary)

- All 26 NIP classes (`NIP01`–`NIP99`) and `EventNostr`
- All 39 concrete event subclasses (`TextNoteEvent`, `ReactionEvent`, etc.)
- All 17 concrete tag subclasses (`EventTag`, `PubKeyTag`, etc.)
- All 27 entity classes (`UserProfile`, `ZapRequest`, etc.)
- `Kind` enum, `ElementAttribute`, `TagRegistry`, `BaseTag`, `BaseEvent`
- `IElement`, `ITag`, `IEvent`, `IGenericElement`, `IBech32Encodable`, `Deleteable`
- `@Tag`, `@Event`, `@Key` annotations
- 14 filter classes (`KindFilter`, `AuthorFilter`, etc.)
- `WebSocketClientIF`, `WebSocketClientFactory`, `SpringWebSocketClientFactory`, `SpringWebSocketClient`

---

## Migrating to 1.0.0 (Historical)

**Status:** Released 2025-10-13
**Deprecation Warnings Since:** 0.6.2

Version 1.0.0 will remove all APIs deprecated in the 0.6.x series. This guide helps you prepare your codebase for a smooth upgrade.

### Deprecated APIs Removed

The following deprecated APIs will be removed in 1.0.0. Migrate to the recommended alternatives before upgrading.

- Removed: `nostr.config.Constants.Kind` nested class
  - Use: `nostr.base.Kind` enum and `Kind#getValue()` when an integer is required
  - See: [Event Kind Constants](#event-kind-constants)

- Removed: `nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD`
  - Use: `nostr.event.json.EventJsonMapper.getMapper()` for event JSON
  - Also available: `nostr.base.json.EventJsonMapper.mapper()` in tests/utility contexts
  - See: [ObjectMapper Usage](#objectmapper-usage)

- Removed: `nostr.api.NIP01#createTextNoteEvent(Identity, String)`
  - Use: `new NIP01(identity).createTextNoteEvent(String)` with sender configured on the instance
  - See: [NIP01 API Changes](#nip01-api-changes)

- Removed: `nostr.api.NIP61#createNutzapEvent(Amount, List<CashuProof>, URL, List<EventTag>, PublicKey, String)`
  - Use: `createNutzapEvent(List<CashuProof>, URL, EventTag, PublicKey, String)`
  - And add amount/unit tags explicitly via `NIP60.createAmountTag(Amount)` and `NIP60.createUnitTag(String)` if needed

- Removed: `nostr.event.tag.GenericTag(String, Integer)` constructor
  - Use: `new GenericTag(String)` or `new GenericTag(String, ElementAttribute...)`

- Removed: `nostr.id.EntityFactory.Events#createGenericTag(PublicKey, IEvent, Integer)`
  - Use: `createGenericTag(PublicKey, IEvent)`

These removals were announced with `@Deprecated(forRemoval = true)` in 0.6.2 and are now finalized in 1.0.0.

---

## Migrating from 0.6.x

### Event Kind Constants

**Deprecated Since:** 0.6.2
**Removed In:** 1.0.0
**Migration Difficulty:** 🟢 Easy (find & replace)

#### Problem

The `Constants.Kind` class is deprecated in favor of the `Kind` enum, which provides better type safety and IDE support.

#### Before (Deprecated ❌)

```java
import nostr.config.Constants;

// Using deprecated integer constants
int kind = Constants.Kind.TEXT_NOTE;
int dmKind = Constants.Kind.ENCRYPTED_DIRECT_MESSAGE;
int zapKind = Constants.Kind.ZAP_REQUEST;
```

#### After (Recommended ✅)

```java
import nostr.base.Kind;

// Using Kind enum
Kind kind = Kind.TEXT_NOTE;
Kind dmKind = Kind.ENCRYPTED_DIRECT_MESSAGE;
Kind zapKind = Kind.ZAP_REQUEST;

// Get integer value when needed
int kindValue = Kind.TEXT_NOTE.getValue();
```

#### Complete Migration Table

| Deprecated Constant | New Enum | Notes |
|---------------------|----------|-------|
| `Constants.Kind.USER_METADATA` | `Kind.SET_METADATA` | Renamed for consistency |
| `Constants.Kind.SHORT_TEXT_NOTE` | `Kind.TEXT_NOTE` | Simplified name |
| `Constants.Kind.RECOMMENDED_RELAY` | `Kind.RECOMMEND_SERVER` | Renamed for accuracy |
| `Constants.Kind.CONTACT_LIST` | `Kind.CONTACT_LIST` | Same name |
| `Constants.Kind.ENCRYPTED_DIRECT_MESSAGE` | `Kind.ENCRYPTED_DIRECT_MESSAGE` | Same name |
| `Constants.Kind.EVENT_DELETION` | `Kind.DELETION` | Simplified name |
| `Constants.Kind.REPOST` | `Kind.REPOST` | Same name |
| `Constants.Kind.REACTION` | `Kind.REACTION` | Same name |
| `Constants.Kind.REACTION_TO_WEBSITE` | `Kind.REACTION_TO_WEBSITE` | Same name |
| `Constants.Kind.CHANNEL_CREATION` | `Kind.CHANNEL_CREATE` | Renamed for consistency |
| `Constants.Kind.CHANNEL_METADATA` | `Kind.CHANNEL_METADATA` | Same name |
| `Constants.Kind.CHANNEL_MESSAGE` | `Kind.CHANNEL_MESSAGE` | Same name |
| `Constants.Kind.CHANNEL_HIDE_MESSAGE` | `Kind.HIDE_MESSAGE` | Simplified name |
| `Constants.Kind.CHANNEL_MUTE_USER` | `Kind.MUTE_USER` | Simplified name |
| `Constants.Kind.OTS_ATTESTATION` | `Kind.OTS_EVENT` | Renamed for consistency |
| `Constants.Kind.REPORT` | `Kind.REPORT` | Same name |
| `Constants.Kind.ZAP_REQUEST` | `Kind.ZAP_REQUEST` | Same name |
| `Constants.Kind.ZAP_RECEIPT` | `Kind.ZAP_RECEIPT` | Same name |
| `Constants.Kind.RELAY_LIST_METADATA` | `Kind.RELAY_LIST_METADATA` | Same name |
| `Constants.Kind.RELAY_LIST_METADATA_EVENT` | `Kind.RELAY_LIST_METADATA` | Duplicate removed |
| `Constants.Kind.CLIENT_AUTHENTICATION` | `Kind.CLIENT_AUTH` | Simplified name |
| `Constants.Kind.REQUEST_EVENTS` | `Kind.REQUEST_EVENTS` | Same name |
| `Constants.Kind.BADGE_DEFINITION` | `Kind.BADGE_DEFINITION` | Same name |
| `Constants.Kind.BADGE_AWARD` | `Kind.BADGE_AWARD` | Same name |
| `Constants.Kind.SET_STALL` | `Kind.STALL_CREATE_OR_UPDATE` | Renamed for clarity |

#### Migration Script (Find & Replace)

```bash
# Example sed commands for bulk migration (test first!)
find . -name "*.java" -exec sed -i 's/Constants\.Kind\.TEXT_NOTE/Kind.TEXT_NOTE/g' {} \;
find . -name "*.java" -exec sed -i 's/Constants\.Kind\.ENCRYPTED_DIRECT_MESSAGE/Kind.ENCRYPTED_DIRECT_MESSAGE/g' {} \;
# ... repeat for other constants
```

#### Why This Change?

- **Type Safety:** Enum provides compile-time type checking
- **IDE Support:** Better autocomplete and refactoring
- **Extensibility:** Easier to add new kinds and metadata
- **Clean Architecture:** Removes dependency on Constants utility class

---

### ObjectMapper Usage

**Deprecated Since:** 0.6.2
**Removed In:** 1.0.0
**Migration Difficulty:** 🟢 Easy (find & replace)

#### Problem

The `Encoder.ENCODER_MAPPER_BLACKBIRD` static field was an anti-pattern (static field in interface). It's now replaced by a dedicated utility class.

#### Before (Deprecated ❌)

```java
import nostr.base.Encoder;

// Using deprecated static field from interface
ObjectMapper mapper = Encoder.ENCODER_MAPPER_BLACKBIRD;
String json = mapper.writeValueAsString(event);
```

#### After (Recommended ✅)

```java
import nostr.event.json.EventJsonMapper;

// Using dedicated utility class
ObjectMapper mapper = EventJsonMapper.getMapper();
String json = mapper.writeValueAsString(event);
```

#### Why This Change?

- **Better Design:** Removes anti-pattern (static field in interface)
- **Single Responsibility:** JSON configuration in dedicated class
- **Discoverability:** Clear location for JSON mapper configuration
- **Maintainability:** Single place to update mapper settings

#### Alternative: Direct Usage

For most use cases, you don't need the mapper directly. Use event serialization methods instead:

```java
// Recommended: Use built-in serialization
GenericEvent event = ...;
String json = event.toJson();

// Deserialization
GenericEvent event = GenericEvent.fromJson(json);
```

---

### NIP01 API Changes

**Deprecated Since:** 0.6.2
**Removed In:** 1.0.0
**Migration Difficulty:** 🟡 Medium (requires code changes)

#### Problem

The `createTextNoteEvent(Identity, String)` method signature is changing to remove the redundant `Identity` parameter (the sender is already set in the NIP01 instance).

#### Before (Deprecated ❌)

```java
import nostr.api.NIP01;
import nostr.id.Identity;

Identity sender = new Identity("nsec1...");
NIP01 nip01 = new NIP01(sender);

// Redundant: sender passed both in constructor AND method
nip01.createTextNoteEvent(sender, "Hello Nostr!")
     .sign()
     .send(relays);
```

#### After (Recommended ✅)

```java
import nostr.api.NIP01;
import nostr.id.Identity;

Identity sender = new Identity("nsec1...");
NIP01 nip01 = new NIP01(sender);

// Cleaner: sender only passed in constructor
nip01.createTextNoteEvent("Hello Nostr!")
     .sign()
     .send(relays);
```

#### Migration Steps

1. **Find all usages:**
   ```bash
   grep -r "createTextNoteEvent(" --include="*.java"
   ```

2. **Update method calls:**
   - Remove the first parameter (Identity)
   - Keep the content parameter

3. **Verify sender is set:**
   - Ensure NIP01 constructor receives the Identity
   - Or use `setSender(identity)` before calling methods

#### Why This Change?

- **DRY Principle:** Don't repeat yourself (sender already in instance)
- **Consistency:** Matches pattern used by other NIP classes
- **Less Verbose:** Simpler API with fewer parameters
- **Clearer Intent:** Sender is instance state, not method parameter

---

## Breaking Changes in 1.0.0

### 1. Removal of Constants.Kind Class

**Impact:** 🔴 High (widely used)

The entire `nostr.config.Constants.Kind` class will be removed. Migrate to `nostr.base.Kind` enum.

**Migration:** See [Event Kind Constants](#event-kind-constants) section above.

---

### 2. Removal of Encoder.ENCODER_MAPPER_BLACKBIRD

**Impact:** 🟡 Medium (internal usage mostly)

The `nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD` field will be removed.

**Migration:** See [ObjectMapper Usage](#objectmapper-usage) section above.

---

### 3. NIP01 Method Signature Changes

**Impact:** 🟡 Medium (common usage)

Method signature changes in NIP01:
- `createTextNoteEvent(Identity, String)` → `createTextNoteEvent(String)`

**Migration:** See [NIP01 API Changes](#nip01-api-changes) section above.

---

## Preparing for 1.0.0

### Step-by-Step Checklist

- [ ] **Run compiler with warnings enabled**
  ```bash
  mvn clean compile -Xlint:deprecation
  ```

- [ ] **Search for deprecated API usage**
  ```bash
  grep -r "Constants.Kind\." --include="*.java" src/
  grep -r "ENCODER_MAPPER_BLACKBIRD" --include="*.java" src/
  grep -r "createTextNoteEvent(.*,.*)" --include="*.java" src/
  ```

- [ ] **Update imports**
  - Replace `import nostr.config.Constants;` with `import nostr.base.Kind;`
  - Replace `import nostr.base.Encoder;` with `import nostr.event.json.EventJsonMapper;`

- [ ] **Update constants**
  - Replace all `Constants.Kind.X` with `Kind.X`
  - Update any renamed constants (see migration table)

- [ ] **Update method calls**
  - Remove redundant `Identity` parameter from `createTextNoteEvent()`

- [ ] **Run tests**
  ```bash
  mvn clean test
  ```

- [ ] **Verify no deprecation warnings**
  ```bash
  mvn clean compile -Xlint:deprecation 2>&1 | grep "deprecated"
  ```

---

## Automated Migration Tools

### IntelliJ IDEA

1. **Analyze → Run Inspection by Name**
2. Search for "Deprecated API Usage"
3. Apply suggested fixes

### Eclipse

1. **Project → Properties → Java Compiler → Errors/Warnings**
2. Enable "Deprecated and restricted API"
3. Use Quick Fixes (Ctrl+1) on warnings

### Command Line (sed)

```bash
#!/bin/bash
# Automated migration script (BACKUP YOUR CODE FIRST!)

# Replace Kind constants
find src/ -name "*.java" -exec sed -i 's/Constants\.Kind\.TEXT_NOTE/Kind.TEXT_NOTE/g' {} \;
find src/ -name "*.java" -exec sed -i 's/Constants\.Kind\.ENCRYPTED_DIRECT_MESSAGE/Kind.ENCRYPTED_DIRECT_MESSAGE/g' {} \;

# Replace ObjectMapper
find src/ -name "*.java" -exec sed -i 's/Encoder\.ENCODER_MAPPER_BLACKBIRD/EventJsonMapper.getMapper()/g' {} \;

# Note: NIP01 method calls require manual review due to parameter removal
```

---

## Need Help?

If you encounter issues during migration:

1. **Check the documentation:** [docs/](docs/)
2. **Review examples:** [nostr-java-examples/](nostr-java-examples/)
3. **Ask for help:** [GitHub Issues](https://github.com/tcheeric/nostr-java/issues)
4. **Join discussions:** [GitHub Discussions](https://github.com/tcheeric/nostr-java/discussions)

---

## Version History

| Version | Release Date | Major Changes |
|---------|--------------|---------------|
| 0.6.2 | 2025-10-06 | Deprecation warnings added for 1.0.0 |
| 0.6.3 | 2025-10-07 | Extended JavaDoc, exception hierarchy |
| 1.0.0 | TBD | Deprecated APIs removed (breaking) |

---

**Last Updated:** 2025-10-07
**Applies To:** nostr-java 0.6.2 → 1.0.0
