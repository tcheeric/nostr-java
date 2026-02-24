# nostr-java Design Simplification Proposal

## Executive Summary

This proposal reduces nostr-java from **9 modules with ~170 classes** to **4 modules with ~40 classes** by:

- Removing the `api` and `examples` modules entirely
- Eliminating all 40 concrete event subclasses — `GenericEvent` becomes the sole event class
- Eliminating all 17 concrete tag subclasses — `GenericTag` becomes the sole tag class, backed by `List<String>` instead of `ElementAttribute`
- Removing 27 entity classes, the `Kind` enum, `ElementAttribute`, `TagRegistry`, and all NIP-specific code
- Dropping most interfaces and abstract classes (`ITag`, `IEvent`, `IElement`, `IGenericElement`, `IBech32Encodable`, `Deleteable`, `BaseEvent`, `BaseTag`)
- Simplifying filters to 3 classes, serialization to ~5 classes
- Merging 7 modules into 4 (`core`, `event`, `identity`, `client`)

**This simplification also resolves the `GenericTag.getCode()` NPE bug** documented in `docs/problems/GENERIC_TAG_GETCODE_FRAGILITY.md`. The root cause — a dual-path tag architecture where annotation-based registered tags and field-based generic tags have incompatible interfaces — is eliminated entirely. There is only one tag class, one code accessor, one value accessor. No reflection, no annotations, no NPE.

---

## Current State

```
Modules:                        9
Concrete event classes:        40  (TextNoteEvent, DirectMessageEvent, CalendarEvent, etc.)
Concrete tag classes:          17  (EventTag, PubKeyTag, AddressTag, etc.)
Entity classes:                27  (UserProfile, ZapRequest, CashuToken, etc.)
NIP API classes:               26  (NIP01..NIP99)
Interfaces/abstract classes:  ~15  (ITag, IEvent, IElement, ISignable, BaseTag, BaseEvent, etc.)
Filter classes:                17
Serializer/Deserializer:      ~16
Message classes:               10
Kind enum:                      1  (35 constants, 130 lines)
```

## Proposed State

```
Modules:                         4  (core, event, identity, client)
Concrete event classes:          1  (GenericEvent)
Concrete tag classes:            1  (GenericTag — backed by List<String>)
Entity classes:                  0
NIP API classes:                 0
Interfaces/abstract classes:     3  (ISignable, IKey, BaseKey — only those earning their existence)
Filter classes:                  3  (EventFilter, Filters, Filterable)
Serializer/Deserializer:        ~5
Message classes:                 7  (keep all — they map 1:1 to the Nostr relay protocol)
Kind constants:                  1  (optional Kinds utility class with static int fields)
```

---

## Phase 1: Remove `nostr-java-api` and `nostr-java-examples`

### What gets deleted
- **26 NIP classes** (NIP01.java through NIP99.java) — convenience wrappers around GenericEvent
- **EventNostr** and **NostrSpringWebSocketClient** — high-level orchestration
- **All factory classes** (GenericEventFactory, NIP01EventBuilder, NIP57ZapRequestBuilder, etc.)
- **Client management** (WebSocketClientHandler, NostrRelayRegistry, NostrSubscriptionManager, etc.)
- **Service layer** (NoteService, DefaultNoteService)
- **All 6 example classes**
- **Configuration classes** (RelayConfig, RelaysProperties, Constants)

### What migrates elsewhere
Nothing. Users build `GenericEvent` directly and use `nostr-java-client` to send. The NIP classes were syntactic sugar — they created a `GenericEvent` with a specific kind and specific tags. With a good builder and clear documentation, users can do this themselves.

### Impact
Users lose convenience methods like `NIP01.createTextNote("Hello")`. Instead:
```java
GenericEvent event = GenericEvent.builder()
    .pubKey(identity.getPublicKey())
    .kind(1)
    .content("Hello Nostr!")
    .build();
identity.sign(event);
```
The tradeoff: more explicit code, but far less library surface area to maintain.

### Risk: Low
The api module has no dependents except examples. No downstream code breaks.

---

## Phase 2: Remove All Concrete Event Subclasses

### What gets deleted (40 classes)
All classes in `nostr-java-event/src/main/java/nostr/event/impl/` except `GenericEvent.java`:

- TextNoteEvent, DirectMessageEvent, ContactListEvent, ReactionEvent, DeletionEvent
- EphemeralEvent, ReplaceableEvent, AddressableEvent
- All Calendar events (4 classes + abstract base)
- All Marketplace events (7 classes)
- All Nostr Connect events (4 classes + abstract base)
- All Channel events (5 classes)
- Zap events, NutZap events, OTS events, etc.
- InternetIdentifierMetadataEvent, MentionsEvent, ClassifiedListingEvent

### What `GenericEvent` already provides
`GenericEvent` is already **fully functional** as the sole event class:
- Supports any kind via `Integer` — no need for subclass-per-kind
- Has `isReplaceable()`, `isEphemeral()`, `isAddressable()` — range checks based on kind value
- Has builder pattern, validation, serialization, signing support
- Tags are a generic list

### What the subclasses provided (and why it's not needed)
1. **Kind validation** (`validateKind()` overrides) — Replace with: validation at event creation time using integer range checks
2. **Tag validation** (`validateTags()` overrides) — Replace with: optional validation utilities users can call, or builder-time validation
3. **Convenience constructors** — Replace with: `GenericEvent.builder()` already handles this
4. **Type-safe casting** — Replace with: query by kind integer. Runtime `instanceof` checks were fragile anyway since deserialized events come back as `GenericEvent`

### What needs updating
- **`GenericEvent.convert()`** static method — remove it (no subclasses to convert to)
- **Deserialization** — `GenericEventDeserializer` already returns `GenericEvent`, so the specialized deserializers (CalendarEventDeserializer, ClassifiedListingEventDeserializer) are deleted

### Risk: Medium
Any external code using concrete event types like `TextNoteEvent` breaks. This is a major version change.

---

## Phase 3: Remove All Concrete Tag Subclasses and `TagRegistry`

### What gets deleted (18 classes)
All classes in `nostr-java-event/src/main/java/nostr/event/tag/` except `GenericTag.java`:

- EventTag, PubKeyTag, AddressTag, IdentifierTag, ReferenceTag
- HashtagTag, ExpirationTag, UrlTag, SubjectTag, DelegationTag
- RelaysTag, NonceTag, PriceTag, EmojiTag, GeohashTag
- LabelTag, LabelNamespaceTag, VoteTag
- **TagRegistry** — no registrations needed when there's only GenericTag

### Risk: Medium
Same as Phase 2 — breaking change for typed tag users.

---

## Phase 4: Remove Entity Classes

### What gets deleted (27 classes in `entities/`)
- UserProfile, Profile, ChannelProfile
- ZapRequest, ZapReceipt, Reaction
- CalendarContent, CalendarRsvpContent
- All Cashu-related entities (CashuToken, CashuProof, CashuQuote, CashuMint, CashuWallet)
- NutZap, NutZapInformation
- All marketplace entities (Product, Stall, CustomerOrder, etc.)
- ClassifiedListing, NIP15Content, NIP42Content
- Amount, PaymentRequest, PaymentShipmentStatus, SpendingHistory, Response

### Rationale
These are content DTOs for specific NIPs. With GenericEvent as the sole event class, the content is just a `String` (often JSON). Users parse it themselves into whatever model they need. The library shouldn't prescribe content schemas.

### Risk: Low
These entities were only used by the concrete event subclasses and the api module NIP classes — both already removed.

---

## Phase 5: Drop `Kind` Enum — Replace with Integer + Optional Constants

### What gets deleted
- `Kind.java` — the 130-line enum with 35 constants, `@JsonCreator`, `valueOf()` / `valueOfStrict()` / `findByValue()`, and the null-vs-exception handling that was itself a recent bug fix

### Why

1. **The Nostr protocol uses integers.** Kind is just an integer on the wire. The enum adds an indirection layer that must be maintained as new NIPs appear.
2. **The enum is already incomplete.** There are hundreds of defined kinds in the wild. The current enum has ~35. Users constantly hit `null` from `Kind.valueOf(int)` for kinds not in the enum.
3. **Custom kinds require bypassing the enum anyway.** `GenericEvent.builder()` already has `.customKind(Integer)` alongside `.kind(Kind)` — two paths for the same thing.
4. **Maintenance burden.** Every new NIP means someone has to add an enum constant and release a new library version. That's the exact coupling this simplification eliminates.

### Replacement: Optional constants class

```java
/**
 * Common Nostr event kind values for discoverability.
 * Users can use any integer — these are convenience constants, not an exhaustive list.
 */
public final class Kinds {
    public static final int SET_METADATA = 0;
    public static final int TEXT_NOTE = 1;
    public static final int RECOMMEND_SERVER = 2;
    public static final int CONTACT_LIST = 3;
    public static final int ENCRYPTED_DIRECT_MESSAGE = 4;
    public static final int DELETION = 5;
    public static final int REPOST = 6;
    public static final int REACTION = 7;
    public static final int ZAP_REQUEST = 9734;
    public static final int ZAP_RECEIPT = 9735;
    // ... other commonly used kinds

    /** Valid kind range per NIP-01: 0 to 65535. */
    public static boolean isValid(int kind) { return kind >= 0 && kind <= 65_535; }
    public static boolean isReplaceable(int kind) { return kind >= 10_000 && kind < 20_000; }
    public static boolean isEphemeral(int kind) { return kind >= 20_000 && kind < 30_000; }
    public static boolean isAddressable(int kind) { return kind >= 30_000 && kind < 40_000; }

    private Kinds() {}
}
```

This gives IDE autocompletion without any enum baggage — no `valueOf`, no `@JsonCreator`, no null-vs-exception debates, no forced library updates for new kinds.

### Implications
- **`GenericEvent.kind`** — already `Integer`. Remove the `Kind`-typed constructors and builder methods. One `.kind(int)` method.
- **`GenericEvent.builder()`** — simplify: remove `.kind(Kind)` and `.customKind(Integer)`, keep only `.kind(int)`.
- **`isReplaceable()` / `isEphemeral()` / `isAddressable()`** — migrate to `Kinds` utility or keep on `GenericEvent` (they already check integer ranges).
- **Deserialization** — simpler. `kind` deserializes as a plain `int`. No `@JsonCreator` dance, no null handling for unknown kinds.
- **`KindFilter`** — already works with integers.
- **`EventTypeChecker`** — can be merged into `Kinds` utility class.

### Risk: Low
Zero functional loss. Strictly simpler.

---

## Phase 6: Drop `ElementAttribute` — Tags Become `List<String>`

### What gets deleted
- **`ElementAttribute`** record — `record ElementAttribute(String name, Object value)` in `nostr-java-base`
- **`IGenericElement`** interface — its only purpose was to expose `getAttributes()`/`addAttribute()` for `ElementAttribute`

### Why
The `name` field in `ElementAttribute` (e.g., "param0", "param1") is **entirely synthetic** — generated during deserialization, never present in the Nostr protocol. Tags are just **positional arrays**: `["e", "abc123", "wss://relay.example.com", "reply"]`. The wrapper object adds indirection for no protocol benefit.

### Replacement
`GenericTag` stores a `List<String>` directly:

```java
// Before
GenericTag {
    code = "e",
    attributes = [ElementAttribute("param0", "abc123"), ElementAttribute("param1", "wss://...")]
}
tag.getAttributes().get(0).value().toString()  // to read a value

// After
GenericTag {
    code = "e",
    params = ["abc123", "wss://..."]
}
tag.getParams().get(0)  // to read a value — direct, no wrapper
```

### Implications
- **`GenericTag`** — replace `List<ElementAttribute> attributes` with `List<String> params`
- **`GenericTagSerializer`** — simplify: iterate `params` list directly instead of mapping `ElementAttribute.value().toString()`
- **`GenericTagDecoder`** — simplify: build `List<String>` directly from JSON array elements instead of wrapping in `ElementAttribute`
- **`BaseTag.create()`** (migrating to `GenericTag.of()`) — no longer needs to generate synthetic `ElementAttribute` names
- **`GenericMessage`** — also implements `IGenericElement`; update to use `List<String>` or a similar simple approach
- **`GenericTagQueryFilter`** — update to work with `List<String>` params

### Risk: Low
`ElementAttribute` was internal plumbing. Downstream consumers were already working around it.

---

## Phase 7: Drop Interfaces and Abstract Classes — Resolves GenericTag.getCode() NPE

**This phase directly resolves the bug documented in `docs/problems/GENERIC_TAG_GETCODE_FRAGILITY.md`.**

### The Problem

The current tag system has a **dual-path architecture** that causes a design contradiction in `GenericTag`:

1. **Annotation path** (registered tags): `BaseTag.getCode()` reads `@Tag` annotation via reflection
2. **Field path** (generic tags): `GenericTag.getCode()` reads instance `code` field

`GenericTag.getCode()` delegates to `super.getCode()` when `code` is empty, but `GenericTag` has no `@Tag` annotation — so the delegation **always throws NPE**. This dead branch has caused production bugs in downstream consumers (see: imani-bridge Cashu gateway losing ecash tokens due to silent tag filtering failures).

The deeper problem: consumers must handle both paths to access tag values uniformly, leading to reflection hacks that break under Java 21 JPMS.

### How this simplification fixes it

By eliminating the entire interface/abstract hierarchy, `GenericTag` becomes a standalone concrete class. There is no `super.getCode()` to delegate to. `getCode()` is a trivial field accessor: `return this.code`. Zero NPE risk.

### What gets dropped

| Interface / Abstract Class | Current Purpose | Why Droppable |
|---|---|---|
| **`IElement`** | `default getNip() { return "1"; }` — a default method returning a constant | Dead weight. Nothing meaningful depends on the NIP string. |
| **`IGenericElement`** | Exposes `getAttributes()`/`addAttribute()` for `ElementAttribute` | Goes away with `ElementAttribute` (Phase 6). |
| **`ITag`** | `setParent(IEvent)`, `getCode()` — 2 methods | With one concrete tag class, the interface adds no polymorphism. `GenericEvent` references `GenericTag` directly. |
| **`IEvent`** | `getId()` — extends `IElement`, `IBech32Encodable` | With one concrete event class, same reasoning. `GenericEvent` has these methods directly. |
| **`IBech32Encodable`** | `toBech32()` — 1 method | `toBech32()` stays as a method on `GenericEvent`. Doesn't need an interface. |
| **`Deleteable`** | `getKind()` — 1 method | `GenericEvent` already has `getKind()`. The interface adds nothing. |
| **`BaseEvent`** | Empty abstract class: `abstract class BaseEvent implements IEvent {}` | Inline into `GenericEvent`. |
| **`BaseTag`** | Factory methods + reflection-based `getSupportedFields()` / `getFieldValue()` | Factory methods migrate to `GenericTag.of()`. Reflection methods deleted — they only served annotation-driven serialization of concrete tags. |

### What gets kept

| Interface / Class | Why It Earns Its Existence |
|---|---|
| **`ISignable`** | Real polymorphic contract. `Identity.sign(ISignable)` decouples signing from the event model. Tiny (4 methods). In practice `GenericEvent` is the only implementor after simplification, but the interface keeps the `id` module independent of the `event` module — which matters for the module merge (Phase 10). |
| **`IKey`** + **`BaseKey`** | Real shared behavior for `PublicKey`/`PrivateKey` — Bech32 encoding, hex conversion, equality semantics. Worth keeping. |
| **`IDecoder<T>`** | Shared by 7 decoder classes. Provides a shared `ObjectMapper` instance and `decode(String)` contract. Could be simplified to a static utility + functional interface later. |
| **`BaseMessage`** | Genuinely polymorphic — 7+ distinct message types share the `command` field and `encode()` contract. |

### Remove annotations
- **`@Tag` annotation** — no longer needed. GenericTag stores its code in a field.
- **`@Event` annotation** — no longer needed. No concrete event subclasses.
- **`@Key` annotation on tag fields** — no longer needed for tag serialization.

Note: `@Key` is still used on `GenericEvent` fields for event serialization. Evaluate whether it can be replaced by explicit serialization logic in `EventSerializer` (which already knows the field order). If so, `@Key` can be dropped entirely.

### What `GenericTag` looks like after this phase

```java
@Data
@JsonSerialize(using = GenericTagSerializer.class)
public class GenericTag {

    private String code;
    private final List<String> params;

    public GenericTag(String code, String... params) {
        this.code = code;
        this.params = new ArrayList<>(List.of(params));
    }

    public GenericTag(String code, List<String> params) {
        this.code = code;
        this.params = new ArrayList<>(params);
    }

    public String getCode() { return this.code; }

    public List<String> getParams() { return Collections.unmodifiableList(this.params); }

    /** NIP-01 wire format: ["code", "param0", "param1", ...] */
    public List<String> toArray() {
        var result = new ArrayList<String>();
        result.add(code);
        result.addAll(params);
        return result;
    }

    /** Factory method — the primary way to create tags. */
    public static GenericTag of(String code, String... params) {
        return new GenericTag(code, params);
    }

    public static GenericTag of(String code, List<String> params) {
        return new GenericTag(code, params);
    }
}
```

No interfaces, no abstract classes, no `ElementAttribute`, no annotations, no reflection. Just a code and a list of strings — exactly what a Nostr tag is.

### What `GenericEvent` looks like after this phase

```java
@Data
public class GenericEvent implements ISignable {

    private String id;
    private PublicKey pubKey;
    private Long createdAt;
    private int kind;
    private List<GenericTag> tags;       // was List<BaseTag>
    private String content;
    private Signature signature;

    // builder, update(), validate(), toBech32(), isReplaceable(), etc.
    // No BaseEvent parent, no IEvent, no Deleteable
}
```

### Ripple effects
1. **`GenericEvent.tags` type** — changes from `List<BaseTag>` to `List<GenericTag>`
2. **`BaseMessage implements IElement`** — remove `implements IElement`; the `getNip()` default was never used meaningfully
3. **`IDecoder<T extends IElement>`** — change bound to `IDecoder<T>` (unbounded)
4. **`GenericTagQuery implements IElement`** — remove `implements IElement`; it's a standalone record
5. **`EventMessage`** — references `IEvent` currently; change to reference `GenericEvent` directly
6. **`BaseTag.setParent(IEvent)` no-op** — disappears entirely since `GenericTag` doesn't implement `ITag`
7. **`GenericEvent.updateTagsParents()`** — can be removed (it called `setParent` which was a no-op)

### Risk: Medium
Breaking change for any code referencing these interfaces. Justified by a major version bump.

---

## Phase 8: Simplify the Filter System

### Current state: 17 filter classes
Most are thin wrappers: `KindFilter`, `AuthorFilter`, `SinceFilter`, `UntilFilter`, `HashtagTagFilter`, `AddressTagFilter`, etc.

### Proposed state: 3 classes
Keep only:
1. **`EventFilter`** — The composable filter builder. Enhance it to be the single entry point:
   ```java
   EventFilter.builder()
       .kinds(List.of(1, 7))
       .authors(List.of("pubkey_hex"))
       .since(timestamp)
       .until(timestamp)
       .addTagFilter("e", List.of("event_id"))
       .addTagFilter("p", List.of("pubkey"))
       .addTagFilter("#t", List.of("nostr"))
       .limit(100)
       .build();
   ```
2. **`Filters`** — Container for multiple EventFilter (OR logic), needed for REQ messages
3. **`Filterable`** — Interface (if still needed)

### Delete
- AbstractFilterable, KindFilter, AuthorFilter, SinceFilter, UntilFilter
- HashtagTagFilter, AddressTagFilter, GeohashTagFilter, IdentifierTagFilter
- ReferencedEventFilter, ReferencedPublicKeyFilter, UrlTagFilter, VoteTagFilter
- GenericTagQueryFilter (merge into EventFilter)

### Risk: Low
Filters are internal plumbing. The new EventFilter API is cleaner.

---

## Phase 9: Simplify Serialization Infrastructure

### Delete
- All concrete tag serializers (AddressTagSerializer, ReferenceTagSerializer, etc.)
- All concrete event deserializers (CalendarEventDeserializer, ClassifiedListingEventDeserializer, etc.)
- `BaseTagSerializer` — merge into GenericTagSerializer
- Codec classes tied to concrete types

### Keep
- `GenericEventSerializer` / `GenericEventDeserializer` — core event JSON
- `GenericTagSerializer` — simplified to output `[code, param0, param1, ...]` from `List<String>`
- `EventSerializer` — canonical NIP-01 serialization for ID/signature computation
- `PublicKeyDeserializer`, `SignatureDeserializer` — needed for key/sig parsing
- `BaseEventEncoder`, `BaseMessageDecoder` — needed for wire protocol
- `EventJsonMapper` — central mapper

### Simplify
- `TagDeserializer` — simplify to always produce `GenericTag(code, List<String>)` directly from the JSON array. No more dispatch to concrete types via TagRegistry.

---

## Phase 10: Replace Custom Hex with `java.util.HexFormat`

### What gets deleted
- `NostrUtil.HEX_ARRAY` constant — hand-rolled lookup table
- `NostrUtil.bytesToHex(byte[])` — manual char-array loop with `toLowerCase()`
- `NostrUtil.hexToBytesConvert(String)` — manual `Character.digit()` loop
- `NostrUtil.hex128ToBytes(String)` — duplicate of `hexToBytes` with different length
- `NostrUtil.nip04PubKeyHexToBytes(String)` — duplicate of `hexToBytes` with different length

### Why
Java 17+ provides `java.util.HexFormat` — a standard, well-tested, performant hex codec. The project requires Java 21+, so it's available. The current implementation is ~30 lines of hand-rolled byte manipulation that `HexFormat` replaces with one-liners.

The three length-specific methods (`hexToBytes` for 64-char, `hex128ToBytes` for 128-char, `nip04PubKeyHexToBytes` for 66-char) differ only in the length parameter passed to `HexStringValidator`. They can be collapsed into one method.

### Replacement

```java
import java.util.HexFormat;

public class NostrUtil {

    private static final HexFormat HEX = HexFormat.of();

    /** Encode bytes to lowercase hex string. */
    public static String bytesToHex(byte[] b) {
        return HEX.formatHex(b);
    }

    /** Decode hex string to bytes with length validation. */
    public static byte[] hexToBytes(String hex, int expectedHexLength) {
        HexStringValidator.validateHex(hex, expectedHexLength);
        return HEX.parseHex(hex);
    }

    // Convenience overloads for common lengths:

    /** Decode 64-char hex (32-byte keys). */
    public static byte[] hexToBytes(String hex) {
        return hexToBytes(hex, 64);
    }

    /** Decode 128-char hex (64-byte Schnorr signatures). */
    public static byte[] hex128ToBytes(String hex) {
        return hexToBytes(hex, 128);
    }
}
```

`HexFormat.of()` produces lowercase output by default — matching the current `bytesToHex` behavior (which uppercases then calls `toLowerCase()`). The `HexFormat` instance is thread-safe and reusable.

### Call sites affected (21 files)

No call-site changes needed for `bytesToHex` — signature is identical.

For `hexToBytes` / `hex128ToBytes` — signatures are identical, so existing callers work unchanged.

The only caller of `nip04PubKeyHexToBytes` is `EncryptedDirectMessage`:
```java
// Before
ECPoint pubKeyPt = curve.decodePoint(NostrUtil.nip04PubKeyHexToBytes("02" + publicKeyHex));

// After — use the general method with explicit length
ECPoint pubKeyPt = curve.decodePoint(NostrUtil.hexToBytes("02" + publicKeyHex, 66));
```

### Behavioral difference
`HexFormat.parseHex()` throws `IllegalArgumentException` on invalid hex characters. The current `hexToBytesConvert()` silently returns `-1` bytes from `Character.digit()` on invalid input (which then corrupt downstream data). The `HexFormat` behavior is strictly better — fail fast instead of silent corruption.

### Risk: Very Low
Drop-in replacement. Same signatures, same output, stricter input validation. One call site changes (`nip04PubKeyHexToBytes` → `hexToBytes` with length).

---

## Phase 11: Harden WebSocket Client

The current WebSocket client has several reliability and robustness issues that become more critical once the api-layer orchestration (NostrRelayRegistry, WebSocketClientHandler, NostrSubscriptionManager) is removed and users interact with the client directly.

### Current Weaknesses

#### 1. Brittle termination detection via string prefix matching

```java
// Current — StandardWebSocketClient line 194
return payload.startsWith("[\"EOSE\"")
    || payload.startsWith("[\"OK\"")
    || payload.startsWith("[\"NOTICE\"")
    || payload.startsWith("[\"CLOSED\"");
```

**Problems:**
- Breaks on JSON formatting variations: `[ "EOSE"` (space after bracket) is valid JSON but won't match
- Could false-positive on content that starts with these strings (unlikely but possible)
- Hardcoded — new Nostr relay message types require code changes
- No validation that the message is well-formed JSON

**Fix:** Parse the first element of the JSON array properly:
```java
private boolean isTerminationMessage(String payload) {
    if (payload == null || payload.length() < 2) return false;
    try {
        JsonNode node = objectMapper.readTree(payload);
        if (!node.isArray() || node.isEmpty()) return false;
        String command = node.get(0).asText();
        return TERMINATION_COMMANDS.contains(command);
    } catch (Exception e) {
        return false;
    }
}

private static final Set<String> TERMINATION_COMMANDS =
    Set.of("EOSE", "OK", "NOTICE", "CLOSED", "AUTH");
```

Jackson ObjectMapper is reusable and thread-safe. The cost of parsing is negligible compared to the network round-trip that produced the message.

#### 2. No automatic reconnection

When a connection drops mid-subscription, all messages are silently lost. The caller's error listener fires, but there's no recovery mechanism. The caller must detect the failure, create a new client, and re-establish all subscriptions.

**Fix:** Add a `ReconnectingWebSocketClient` wrapper:

```java
public class ReconnectingWebSocketClient implements WebSocketClientIF {
    private final String relayUri;
    private final ReconnectPolicy policy;
    private volatile StandardWebSocketClient delegate;
    private final Map<String, SubscriptionRecord> activeSubscriptions = new ConcurrentHashMap<>();

    // On disconnect:
    // 1. Exponential backoff reconnect (configurable max retries, max delay)
    // 2. Re-register all active subscriptions after reconnect
    // 3. Notify listeners of reconnection via onReconnect callback
    // 4. Give up after max retries and notify error listeners
}
```

Key behaviors:
- Reconnect with exponential backoff (default: 1s → 2s → 4s → ... → 60s cap, infinite retries)
- Re-send active subscription REQ messages after reconnect
- Fire a `reconnectListener` callback so callers know subscriptions were re-established
- Configurable via `ReconnectPolicy` (max retries, base delay, max delay, jitter)
- Thread-safe — reconnection happens on a background thread, sends are queued or rejected during reconnect

#### 3. No ping/pong heartbeat for stale connection detection

The idle timeout (default 1 hour) is passive — it only fires if the *container* detects inactivity. If the network silently drops (e.g., NAT timeout, mobile network switch), the connection appears alive but no messages flow. The caller won't know until the next send attempt fails.

**Fix:** Add periodic WebSocket ping frames:

```java
private final ScheduledExecutorService heartbeatExecutor =
    Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "nostr-heartbeat");
        t.setDaemon(true);
        return t;
    });

// Schedule after connection established:
heartbeatExecutor.scheduleAtFixedRate(() -> {
    try {
        if (clientSession.isOpen()) {
            clientSession.sendMessage(new PingMessage());
        }
    } catch (Exception e) {
        log.warn("Heartbeat ping failed, connection may be dead", e);
        handleConnectionLoss(e);
    }
}, pingIntervalMs, pingIntervalMs, TimeUnit.MILLISECONDS);
```

Default interval: 30 seconds (configurable via `nostr.websocket.ping-interval-ms`). Set to 0 to disable.

#### 4. Unbounded message accumulation in `PendingRequest`

When a REQ produces a large result set, `PendingRequest.events` accumulates all messages in memory until EOSE arrives. A relay returning 100k events could OOM the client.

**Fix:** Add a configurable event count limit:
```java
private static final int DEFAULT_MAX_EVENTS_PER_REQUEST = 10_000;

void addEvent(String event) {
    if (events.size() >= maxEventsPerRequest) {
        log.warn("Event limit reached ({}), completing request early", maxEventsPerRequest);
        complete();
        return;
    }
    events.add(event);
}
```

Configurable via `nostr.websocket.max-events-per-request`. Default: 10,000. Users expecting larger result sets should use subscriptions instead of blocking sends.

#### 5. No connection state query

Users cannot ask "is this client connected?" without attempting a send and catching an exception.

**Fix:** Add state tracking:
```java
public enum ConnectionState { CONNECTING, CONNECTED, RECONNECTING, CLOSED }

private final AtomicReference<ConnectionState> state =
    new AtomicReference<>(ConnectionState.CONNECTING);

public ConnectionState getConnectionState() { return state.get(); }
public boolean isConnected() { return state.get() == ConnectionState.CONNECTED; }
```

Update state in `afterConnectionEstablished()`, `afterConnectionClosed()`, and reconnection logic.

#### 6. Configuration scattered across System properties and Spring `@Value`

Currently:
- `@Value` annotations for `awaitTimeoutMs`, `pollIntervalMs`, `maxIdleTimeoutMs` (Spring injection)
- `System.getProperty()` calls in `createSpringClient()` for container-level config
- Constructor parameters for programmatic config

These three config paths can conflict. A user setting `nostr.websocket.max-idle-timeout-ms` as a Spring property won't affect `createSpringClient()` which reads system properties.

**Fix:** Consolidate into a `WebSocketClientConfig` record:
```java
public record WebSocketClientConfig(
    long awaitTimeoutMs,          // default 60_000
    long maxIdleTimeoutMs,        // default 3_600_000
    long pingIntervalMs,          // default 30_000, 0 to disable
    int maxTextMessageBufferSize, // default 1_048_576
    int maxEventsPerRequest,      // default 10_000
    ReconnectPolicy reconnectPolicy  // default: exponential backoff, infinite retries
) {
    public static WebSocketClientConfig defaults() { ... }
    public static Builder builder() { ... }
}
```

Spring autoconfiguration can populate this from `application.properties`. Programmatic users pass it to the constructor. One source of truth.

#### 7. `pollIntervalMs` parameter is dead code

Documented as "no longer used for polling" but still required in constructors, still validated, still stored. It's API surface that misleads users.

**Fix:** Remove it. This is a major version — no backward-compat obligation. The constructor becomes:
```java
public StandardWebSocketClient(String relayUri, WebSocketClientConfig config)
```

#### 8. `send()` returns empty list on timeout (silent failure)

```java
} catch (TimeoutException e) {
    // ...
    return List.of();  // Caller can't distinguish "no results" from "timed out"
}
```

An empty list is a valid response (relay has no matching events). Returning it on timeout makes the failure invisible.

**Fix:** Throw a dedicated exception:
```java
} catch (TimeoutException e) {
    throw new RelayTimeoutException(
        "Timed out waiting for relay response after " + timeout + "ms",
        clientSession.getUri().toString(), timeout);
}
```

Where `RelayTimeoutException extends IOException` so existing catch blocks still work but callers can distinguish the failure mode.

#### 9. No NIP-42 AUTH support at the client level

Relays may send `["AUTH", "challenge"]` requiring the client to respond with a signed authentication event (kind 22242). Currently there's no mechanism for this — AUTH messages are just dispatched to regular listeners with no framework support.

**The NIP-42 protocol flow:**
```
Client                          Relay
  |                               |
  |-------- connect ------------->|
  |                               |
  |<------ ["AUTH", "challenge"]--|   relay challenges client
  |                               |
  |--- ["AUTH", signed_event] --->|   client responds with kind 22242 event
  |                               |
  |<------ ["OK", ...]  ---------|   relay accepts/rejects
```

The signed response event must contain:
```json
{
  "kind": 22242,
  "tags": [["relay", "wss://relay.example.com/"], ["challenge", "the-challenge-string"]],
  "content": "",
  "pubkey": "...", "id": "...", "sig": "...", "created_at": ...
}
```

**The dependency problem:** Building that response requires `GenericEvent` (from `nostr-java-event`) and `Identity.sign()` (from `nostr-java-identity`). But in the dependency chain `core → event → identity → client`, the client module **cannot** import event or identity — that would create a circular dependency. The client only works with raw JSON strings.

**Fix:** Invert the dependency with a callback. The client defines the *contract*, the application provides the *implementation*:

```java
// In nostr-java-client — knows nothing about events or signing
@FunctionalInterface
public interface RelayAuthHandler {
    /**
     * Called when a relay sends an AUTH challenge.
     *
     * @param challenge the challenge string from the relay
     * @param relayUri  the URI of the relay that sent the challenge
     * @return a fully encoded AUTH message as JSON (e.g. ["AUTH", {signed_event}]),
     *         or null to skip authentication
     */
    String handleAuthChallenge(String challenge, String relayUri);
}
```

**Application-side implementation** (has access to all modules):
```java
Identity identity = Identity.create(myPrivateKey);

RelayAuthHandler authHandler = (challenge, relayUri) -> {
    GenericEvent authEvent = GenericEvent.builder()
        .pubKey(identity.getPublicKey())
        .kind(22242)
        .content("")
        .tags(List.of(
            GenericTag.of("relay", relayUri),
            GenericTag.of("challenge", challenge)
        ))
        .build();
    identity.sign(authEvent);
    return "[\"AUTH\"," + EventJsonMapper.toJson(authEvent) + "]";
};

// Pass it when creating the client
var config = WebSocketClientConfig.builder()
    .authHandler(authHandler)
    .build();
var client = new NostrWebSocketClient("wss://relay.example.com", config);
```

**Client-side handling** (inside `NostrWebSocketClient`):
```java
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();

    // Check for AUTH challenge before normal dispatch
    if (isAuthChallenge(payload)) {
        handleAuth(payload);
        return;  // Don't dispatch AUTH challenges to regular listeners
    }

    // ... normal message handling (dispatch, termination detection, etc.)
}

private void handleAuth(String payload) {
    if (authHandler == null) {
        log.warn("Relay sent AUTH challenge but no RelayAuthHandler configured");
        return;
    }

    String challenge = objectMapper.readTree(payload).get(1).asText();
    String relayUri = clientSession.getUri().toString();

    try {
        String response = authHandler.handleAuthChallenge(challenge, relayUri);
        if (response != null) {
            clientSession.sendMessage(new TextMessage(response));
            log.debug("Sent AUTH response to relay {}", relayUri);
        }
    } catch (Exception e) {
        log.warn("Auth handler failed for relay {}", relayUri, e);
        notifyError(e);
    }
}
```

**Design decisions:**

- **`String` return type, not a typed event** — the client module can't depend on `GenericEvent`. The application serializes the event to JSON before returning. This keeps the module boundary clean.
- **Optional (nullable handler)** — not all relays require auth, not all apps need it. If no handler is set, AUTH challenges are logged and ignored. The client still works for public relay operations.
- **Re-authentication after reconnect** — when the `ReconnectPolicy` (fix #2) re-establishes a connection, the relay sends a fresh AUTH challenge. The same handler fires again automatically — it's stateless, so no special handling is needed.
- **Late AUTH challenges** — some relays send AUTH mid-session, not just on connect. The `handleTextMessage` check runs on every inbound message, so late challenges are handled transparently.
- **AUTH intercept before dispatch** — AUTH challenges are consumed by the handler and not forwarded to regular message listeners or subscription callbacks. This prevents application code from seeing protocol-level messages it shouldn't need to handle.

#### 10. Spring framework coupling is mandatory

`StandardWebSocketClient` extends `TextWebSocketHandler`, requires Spring WebSocket, Spring Retry, and is annotated with `@Component`. Users who don't use Spring cannot use the client without pulling in the entire Spring Boot WebSocket dependency tree.

**Fix:** Split into two layers:
1. **`NostrWebSocketClient`** — plain Java, no Spring dependencies. Uses `jakarta.websocket` (JSR 356) directly. Includes reconnection, heartbeat, config, auth handler.
2. **`SpringNostrWebSocketClient`** — thin Spring wrapper adding `@Component`, `@Retryable`, property binding. Delegates to `NostrWebSocketClient`.

Users who don't use Spring get a fully functional client. Spring users get autoconfiguration on top.

### Proposed client class structure after hardening

```
nostr-java-client/
├── NostrWebSocketClient.java        -- Core client (plain Java, jakarta.websocket)
├── ReconnectPolicy.java             -- Reconnect config (max retries, backoff, jitter)
├── WebSocketClientConfig.java       -- Unified configuration record
├── RelayAuthHandler.java            -- NIP-42 auth callback interface
├── RelayTimeoutException.java       -- Typed timeout exception
├── ConnectionState.java             -- Enum: CONNECTING, CONNECTED, RECONNECTING, CLOSED
├── ConnectionListener.java          -- Callbacks: onConnect, onReconnect, onDisconnect
├── spring/
│   ├── SpringNostrWebSocketClient.java   -- Spring wrapper (@Component, @Retryable)
│   ├── NostrRetryable.java               -- Retry annotation
│   └── RetryConfig.java                  -- @EnableRetry config
└── (removed: WebSocketClientIF, WebSocketClientFactory, SpringWebSocketClientFactory,
     StandardWebSocketClient, SpringWebSocketClient)
```

### Summary of hardening changes

| Issue | Severity | Fix |
|---|---|---|
| Brittle string-prefix termination detection | High | Proper JSON parsing of message command |
| No automatic reconnection | High | `ReconnectPolicy` with exponential backoff, subscription re-registration |
| No heartbeat/ping | High | Periodic WebSocket ping frames (default 30s) |
| Unbounded message accumulation | Medium | Configurable max events per request (default 10k) |
| Silent timeout failure (empty list) | Medium | Throw `RelayTimeoutException` |
| No connection state query | Medium | `ConnectionState` enum + `isConnected()` |
| Scattered configuration | Medium | Unified `WebSocketClientConfig` record |
| Dead `pollIntervalMs` parameter | Low | Remove |
| No NIP-42 AUTH support | Medium | `RelayAuthHandler` callback interface |
| Mandatory Spring coupling | Medium | Plain Java core + optional Spring wrapper |

### Risk: Medium
The client API changes (new constructor signatures, new exception type, removed `pollIntervalMs`) are breaking changes, but they're already part of the 2.0.0 major version. The reconnection and heartbeat additions are purely additive — they add reliability without changing existing call patterns.

---

## Phase 12: Merge Modules

After the above simplifications, the 7 remaining modules (util, crypto, base, event, id, encryption, client) become small enough to consolidate.

### Module merges

| Current Modules | Merged Into | Contents |
|---|---|---|
| `util` + `crypto` | **`nostr-java-core`** | Hashing, Schnorr signatures, Bech32, hex utils, exceptions |
| `base` + `event` | **`nostr-java-event`** | GenericEvent, GenericTag, Kinds, PublicKey, PrivateKey, Signature, messages, filters, serialization, ISignable, IKey/BaseKey |
| `id` + `encryption` | **`nostr-java-identity`** | Identity, MessageCipher04, MessageCipher44 |
| `client` | **`nostr-java-client`** | NostrWebSocketClient, reconnection, heartbeat, Spring wrapper |

### Resulting dependency chain
```
nostr-java-core  →  nostr-java-event  →  nostr-java-identity  →  nostr-java-client
```

**4 modules** instead of 9. Each module has a clear, focused purpose.

### Risk: High (breaking for all existing consumers)
This is a major restructuring affecting all import paths. Justified by a 2.0.0 major version bump. Could be deferred to a separate release after the class-level simplifications stabilize.

---

## Summary of Deletions

| Category | Current | After | Deleted |
|---|:---:|:---:|:---:|
| Modules | 9 | 4 | 5 |
| Event classes | 40 | 1 | 39 |
| Tag classes | 18 | 1 | 17 |
| Entity classes | 27 | 0 | 27 |
| NIP API classes | 26 | 0 | 26 |
| Interfaces/abstracts dropped | — | — | 8 (ITag, IEvent, IElement, IGenericElement, IBech32Encodable, Deleteable, BaseEvent, BaseTag) |
| Other classes dropped | — | — | 3 (Kind enum, ElementAttribute, TagRegistry) |
| Filter classes | 17 | 3 | 14 |
| Factory classes | ~10 | 0 | ~10 |
| Serializer/Deserializer | ~16 | ~5 | ~11 |
| Example classes | 6 | 0 | 6 |
| Annotation classes | 3 | 0-1 | 2-3 (@Tag, @Event, possibly @Key) |
| **Total classes** | **~180** | **~40** | **~140** |

---

## What Survives (Complete List)

### `nostr-java-core` (merged util + crypto)
- `NostrUtil` — SHA-256, hex encoding, random bytes
- `Schnorr` — BIP340 Schnorr signatures
- `Bech32`, `Bech32Prefix` — Bech32 encoding/decoding
- `EncryptedDirectMessage` — NIP-04 AES-256-CBC primitives
- `Point`, `Pair` — elliptic curve math
- Exception classes — NostrException, NostrCryptoException, etc.
- Validators — HexStringValidator, Nip05Validator
- HTTP support — HttpClientProvider, DefaultHttpClientProvider

### `nostr-java-event` (merged base + event)
- **`GenericEvent`** — the sole event class
- **`GenericTag`** — the sole tag class (code + `List<String>`)
- **`Kinds`** — static int constants for common kinds + range check utilities
- **`PublicKey`**, **`PrivateKey`**, **`Signature`** — key/sig types
- **`BaseKey`**, **`IKey`** — shared key behavior
- **`ISignable`** — signing contract
- **`BaseMessage`** + message classes — EventMessage, ReqMessage, CloseMessage, OkMessage, EoseMessage, NoticeMessage, GenericMessage
- **`EventFilter`**, **`Filters`** — filter system
- **Serialization** — GenericEventSerializer/Deserializer, GenericTagSerializer, EventSerializer, PublicKeyDeserializer, SignatureDeserializer, EventJsonMapper
- **`IDecoder`** + codec classes — GenericEventDecoder, GenericTagDecoder, BaseMessageDecoder, BaseEventEncoder, etc.
- Supporting types — Relay, SubscriptionId, Marker, Command, Encoder, Nip05Content
- Validation — EventValidator

### `nostr-java-identity` (merged id + encryption)
- **`Identity`** — key management and signing
- **`MessageCipher`** interface + **`MessageCipher04`**, **`MessageCipher44`** — encryption

### `nostr-java-client`
- **`NostrWebSocketClient`** — core WebSocket client (plain Java, jakarta.websocket). Blocking `send()`, non-blocking `subscribe()`, automatic reconnection, heartbeat ping, NIP-42 auth callback, connection state tracking
- **`WebSocketClientConfig`** — unified configuration record (timeouts, buffer sizes, heartbeat interval, max events per request, reconnect policy)
- **`ReconnectPolicy`** — reconnection strategy (max retries, base delay, max delay, jitter)
- **`RelayAuthHandler`** — NIP-42 authentication callback interface
- **`RelayTimeoutException`** — typed exception for timeout failures (replaces silent empty-list return)
- **`ConnectionState`** — enum: CONNECTING, CONNECTED, RECONNECTING, CLOSED
- **`ConnectionListener`** — callbacks: onConnect, onReconnect, onDisconnect
- **`spring/SpringNostrWebSocketClient`** — Spring wrapper (@Component, @Retryable, property binding)
- **`spring/NostrRetryable`**, **`spring/RetryConfig`** — Spring Retry support

---

## What Users Gain

1. **Dramatically smaller API surface** — one event class, one tag class, ~40 total classes
2. **No version lag** — new NIPs don't require library updates. Users create `GenericEvent` with any kind integer.
3. **Predictable deserialization** — everything comes back as `GenericEvent` with `GenericTag`. No surprise polymorphism.
4. **No NPE traps** — the `GenericTag.getCode()` bug and its entire class of dual-path problems are structurally eliminated.
5. **No reflection** — tag code/value access is direct field access. Works cleanly under Java 21 JPMS.
6. **Reliable connectivity** — automatic reconnection with subscription re-registration, heartbeat ping for stale connection detection, typed timeout exceptions instead of silent failures, NIP-42 auth support.
7. **No Spring lock-in** — plain Java WebSocket client works without Spring. Spring wrapper optional.
8. **Easier to learn** — the whole library fits in your head.
9. **Easier to maintain** — ~40 classes instead of ~180.

## What Users Lose

1. **Type safety for specific NIPs** — no more `TextNoteEvent` vs `ReactionEvent`. Users work with kind integers.
2. **Convenience builders** — no more `NIP04.createEncryptedDM()`. Users compose events manually.
3. **Content parsing** — no more entity classes for structured content. Users parse JSON content themselves.
4. **NIP-specific validation** — tag/content validation is removed. Users validate their own events.
5. **Kind enum autocompletion** — replaced by `Kinds.TEXT_NOTE` constants (same IDE experience, less machinery).

---

## Migration Path

This is a **major version** change (2.0.0). Provide:

1. A migration guide mapping old patterns to new:
   ```java
   // Old
   TextNoteEvent event = new TextNoteEvent(pubKey, List.of(), "Hello");
   EventTag eTag = new EventTag("abc123", "wss://relay.example.com", Marker.REPLY);
   event.addTag(eTag);

   // New
   GenericEvent event = GenericEvent.builder()
       .pubKey(pubKey)
       .kind(Kinds.TEXT_NOTE)
       .content("Hello")
       .tags(List.of(GenericTag.of("e", "abc123", "wss://relay.example.com", "reply")))
       .build();
   ```
2. Clear examples in documentation showing how to create common event types
3. Update CLAUDE.md, README, and all docs to reflect the new architecture

---

## Recommended Implementation Order

1. **Phase 1**: Remove `api` + `examples` modules (lowest risk, highest impact)
2. **Phase 4**: Remove entity classes (no dependents after Phase 1)
3. **Phase 2**: Remove concrete event subclasses
4. **Phase 3**: Remove concrete tag subclasses + TagRegistry
5. **Phase 5**: Drop `Kind` enum, add `Kinds` constants
6. **Phase 6**: Drop `ElementAttribute`, make tags use `List<String>`
7. **Phase 7**: Drop interfaces/abstract classes, flatten hierarchy
8. **Phase 8**: Simplify filters
9. **Phase 9**: Simplify serialization
10. **Phase 10**: Replace custom hex with `java.util.HexFormat` (small, independent — can be done any time)
11. **Phase 11**: Harden WebSocket client (can be done in parallel with phases 5-9)
12. **Phase 12**: Module merges (final step, can be a separate release)

Each phase can be a separate PR for easier review. Phases 1-4 remove dead code. Phases 5-7 reshape the core model. Phases 8-10 clean up infrastructure. Phase 11 hardens connectivity. Phase 12 restructures the build.
