# Extending Events

Navigation: [Docs index](../README.md) · [API how‑to](../howto/use-nostr-java-api.md) · [Custom events](../howto/custom-events.md) · [API reference](../reference/nostr-java-api.md)

This guide explains how to properly extend nostr-java with new event types, custom tags, and factories. The project uses factories and registries to make it easy to introduce new event types while keeping core classes stable.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Adding a New Event Type](#adding-a-new-event-type)
- [Complete Example: Poll Event](#complete-example-poll-event)
- [Adding Custom Tags](#adding-custom-tags)
- [Creating Event Factories](#creating-event-factories)
- [Testing & Contribution](#testing--contribution)

---

## Architecture Overview

### Event Factories

**Event factories** (e.g. [`EventFactory`](../../nostr-java-api/src/main/java/nostr/api/factory/EventFactory.java) and its implementations) centralize event creation so that callers don't have to handle boilerplate like setting the sender, tags, or content.

Example: [`GenericEventFactory`](../../nostr-java-api/src/main/java/nostr/api/factory/impl/GenericEventFactory.java)

### Tag Registry

[`TagRegistry`](../../nostr-java-event/src/main/java/nostr/event/tag/TagRegistry.java) maps tag codes to concrete implementations, allowing additional tag types to be resolved at runtime without modifying `BaseTag`.

**How it works:**
```java
// Registration (done once, typically in static initializer)
TagRegistry.register("expiration", ExpirationTag::updateFields);

// Runtime resolution
Function<GenericTag, ? extends BaseTag> factory = TagRegistry.get("expiration");
BaseTag tag = factory.apply(genericTag);
```

### Event Hierarchy

```
BaseEvent (abstract)
  └── GenericEvent (concrete)
       ├── ContactListEvent
       ├── DeletionEvent
       ├── ZapRequestEvent
       └── Your custom events...
```

---

## Adding a New Event Type

### Step-by-Step Process

1. **Define the kind** – Add a constant to [`Kind`](../../nostr-java-base/src/main/java/nostr/base/Kind.java) or use a custom value per [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md)
2. **Implement the event** – Create a class under `nostr.event.impl` that extends `GenericEvent`
3. **Add custom tags** (if needed) – Create tag classes and register them in `TagRegistry`
4. **Provide a factory** (optional) – Implement a factory extending `EventFactory` for convenience
5. **Write tests** – Add unit and integration tests
6. **Document** – Update documentation and examples

### Choosing a Kind Number

Per [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md), kind numbers are grouped:

| Range | Type | Description |
|-------|------|-------------|
| 0-9999 | Regular | Can be deleted, standard events |
| 10000-19999 | Replaceable | Newer event replaces older (by pubkey) |
| 20000-29999 | Ephemeral | Not stored by relays |
| 30000-39999 | Parameterized Replaceable | Replaceable with `d` tag parameter |

**Example:**
```java
// In nostr.base.Kind enum
public static final Kind POLL = new Kind(30078);  // Parameterized replaceable
```

---

## Complete Example: Poll Event

Let's implement a complete poll event (NIP-69 style) with custom tags.

### 1. Define the Kind

```java
// Add to nostr-java-base/src/main/java/nostr/base/Kind.java
public static final Kind POLL = new Kind(30078);
```

### 2. Create Custom Tags

**PollOptionTag.java** (in `nostr-java-event/src/main/java/nostr/event/tag/`):

```java
package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Tag(code = "poll_option", name = "Poll Option")
public class PollOptionTag extends BaseTag {

    @Key
    @JsonProperty
    private String optionId;

    @JsonProperty
    private String optionText;

    public PollOptionTag(String optionId, String optionText) {
        this.optionId = optionId;
        this.optionText = optionText;
        this.code = "poll_option";
    }

    /**
     * Factory method for TagRegistry
     */
    public static PollOptionTag updateFields(@NonNull GenericTag tag) {
        if (!"poll_option".equals(tag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for PollOptionTag");
        }
        String optionId = tag.getAttributes().get(0).value().toString();
        String optionText = tag.getAttributes().get(1).value().toString();
        return new PollOptionTag(optionId, optionText);
    }
}
```

**Register the tag** in `TagRegistry`:

```java
// In TagRegistry static initializer
static {
    // ... existing registrations ...
    register("poll_option", PollOptionTag::updateFields);
}
```

### 3. Create the Event Class

**PollEvent.java** (in `nostr-java-event/src/main/java/nostr/event/impl/`):

```java
package nostr.event.impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.PollOptionTag;

@Data
@EqualsAndHashCode(callSuper = true)
@Event(name = "Poll Event", nip = 69)
@NoArgsConstructor
public class PollEvent extends GenericEvent {

    public PollEvent(@NonNull PublicKey pubKey, @NonNull String question,
                     @NonNull List<PollOptionTag> options) {
        super(pubKey, Kind.POLL,
              options.stream().map(o -> (BaseTag) o).collect(Collectors.toList()),
              question);
    }

    public PollEvent(@NonNull PublicKey pubKey, @NonNull String question,
                     @NonNull List<PollOptionTag> options, @NonNull List<BaseTag> additionalTags) {
        super(pubKey, Kind.POLL, combineTags(options, additionalTags), question);
    }

    private static List<BaseTag> combineTags(List<PollOptionTag> options,
                                             List<BaseTag> additional) {
        List<BaseTag> allTags = options.stream()
            .map(o -> (BaseTag) o)
            .collect(Collectors.toList());
        allTags.addAll(additional);
        return allTags;
    }

    /**
     * Get poll options from tags
     */
    public List<PollOptionTag> getOptions() {
        return getTags().stream()
            .filter(tag -> tag instanceof PollOptionTag)
            .map(tag -> (PollOptionTag) tag)
            .collect(Collectors.toList());
    }

    /**
     * Get the poll question
     */
    public String getQuestion() {
        return getContent();
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        long optionCount = getTags().stream()
            .filter(t -> "poll_option".equals(t.getCode()))
            .count();

        if (optionCount < 2) {
            throw new AssertionError("Poll must have at least 2 options");
        }
        if (optionCount > 10) {
            throw new AssertionError("Poll cannot have more than 10 options");
        }
    }

    @Override
    protected void validateKind() {
        if (getKind() != Kind.POLL.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.POLL.getValue());
        }
    }
}
```

### 4. Create a Factory (Optional but Recommended)

**PollEventFactory.java** (in `nostr-java-api/src/main/java/nostr/api/factory/impl/`):

```java
package nostr.api.factory.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.PollEvent;
import nostr.event.tag.PollOptionTag;
import nostr.id.Identity;

@EqualsAndHashCode(callSuper = true)
@Data
public class PollEventFactory extends EventFactory<PollEvent, BaseTag> {

    private String question;
    private List<String> options;

    public PollEventFactory(Identity sender, @NonNull String question,
                           @NonNull List<String> options) {
        super(sender);
        this.question = question;
        this.options = options;
    }

    @Override
    public PollEvent create() {
        List<PollOptionTag> pollOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            pollOptions.add(new PollOptionTag(String.valueOf(i), options.get(i)));
        }

        return new PollEvent(
            getIdentity().getPublicKey(),
            question,
            pollOptions,
            getTags()  // Additional tags from factory
        );
    }

    /**
     * Convenience method to add expiration
     */
    public PollEventFactory withExpiration(int timestamp) {
        addTag(new nostr.event.tag.ExpirationTag(timestamp));
        return this;
    }
}
```

### 5. Usage Example

```java
import nostr.id.Identity;
import nostr.api.factory.impl.PollEventFactory;
import nostr.event.impl.PollEvent;
import nostr.event.message.EventMessage;
import nostr.client.springwebsocket.StandardWebSocketClient;
import java.util.List;

public class PollExample {
    public static void main(String[] args) throws Exception {
        Identity identity = Identity.generateRandomIdentity();

        // Method 1: Using the factory
        PollEventFactory factory = new PollEventFactory(
            identity,
            "What's your favorite programming language?",
            List.of("Java", "Python", "Rust", "Go")
        );

        // Optionally add expiration (1 week from now)
        factory.withExpiration((int) (System.currentTimeMillis() / 1000) + 604800);

        PollEvent poll = factory.create();
        identity.sign(poll);

        // Send to relay
        try (StandardWebSocketClient client =
                new StandardWebSocketClient("wss://relay.damus.io")) {
            client.send(new EventMessage(poll));
            System.out.println("Poll created: " + poll.getId());
        }

        // Method 2: Direct construction
        List<PollOptionTag> options = List.of(
            new PollOptionTag("0", "Java"),
            new PollOptionTag("1", "Python"),
            new PollOptionTag("2", "Rust")
        );

        PollEvent directPoll = new PollEvent(
            identity.getPublicKey(),
            "Best language for backend?",
            options
        );
        identity.sign(directPoll);

        // Access poll data
        System.out.println("Question: " + directPoll.getQuestion());
        directPoll.getOptions().forEach(opt ->
            System.out.println("  - " + opt.getOptionText())
        );
    }
}
```

---

## Adding Custom Tags

### Tag Implementation Pattern

All custom tags should:

1. **Extend `BaseTag`**
2. **Use annotations**: `@Tag(code = "your_code", name = "Tag Name", nip = X)`
3. **Implement `updateFields` method** for TagRegistry
4. **Mark key fields** with `@Key` annotation

**Example: Custom LocationTag**

```java
package nostr.event.tag;

import lombok.*;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Tag(code = "location", name = "Location Tag")
public class LocationTag extends BaseTag {

    @Key
    private String latitude;

    @Key
    private String longitude;

    private String name;  // Optional field

    public static LocationTag updateFields(@NonNull GenericTag tag) {
        if (!"location".equals(tag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code");
        }

        String lat = tag.getAttributes().get(0).value().toString();
        String lon = tag.getAttributes().get(1).value().toString();
        String name = tag.getAttributes().size() > 2
            ? tag.getAttributes().get(2).value().toString()
            : null;

        LocationTag locationTag = new LocationTag();
        locationTag.setLatitude(lat);
        locationTag.setLongitude(lon);
        locationTag.setName(name);
        return locationTag;
    }
}
```

**Register in TagRegistry:**

```java
static {
    // ... existing registrations ...
    register("location", LocationTag::updateFields);
}
```

---

## Creating Event Factories

Event factories provide a clean API for creating events with sensible defaults.

### Factory Pattern

```java
public class MyEventFactory extends EventFactory<MyEvent, BaseTag> {

    private String customField;

    public MyEventFactory(Identity sender, String customField) {
        super(sender);
        this.customField = customField;
    }

    @Override
    public MyEvent create() {
        return new MyEvent(
            getIdentity().getPublicKey(),
            customField,
            new ArrayList<>(getTags()),
            getContent()
        );
    }

    // Fluent methods for convenience
    public MyEventFactory withSomeOption(String value) {
        addTag(new SomeTag(value));
        return this;
    }
}
```

### When to Create a Factory

Create a factory when:
- Event construction has multiple steps
- You want to provide default tags or content
- The API should be fluent and user-friendly
- Events are created frequently in client code

Don't create a factory when:
- Event is very simple (just use constructor)
- Event is only used internally
- Construction is straightforward

---

## Testing & Contribution

### Unit Tests

Test your event implementation:

```java
@Test
void testPollEventCreation() {
    PublicKey pubKey = new PublicKey(/* ... */);

    List<PollOptionTag> options = List.of(
        new PollOptionTag("0", "Option A"),
        new PollOptionTag("1", "Option B")
    );

    PollEvent poll = new PollEvent(pubKey, "Question?", options);

    assertEquals("Question?", poll.getQuestion());
    assertEquals(2, poll.getOptions().size());
    assertEquals(Kind.POLL.getValue(), poll.getKind());
}

@Test
void testPollEventValidation() {
    PublicKey pubKey = new PublicKey(/* ... */);

    // Should fail with < 2 options
    assertThrows(AssertionError.class, () -> {
        new PollEvent(pubKey, "Question?", List.of(
            new PollOptionTag("0", "Only one option")
        ));
    });
}
```

### Serialization Tests

Test JSON encoding/decoding:

```java
@Test
void testPollEventSerialization() throws Exception {
    PollEvent original = createTestPoll();

    // Serialize
    String json = new EventMessage(original).encode();

    // Deserialize
    BaseMessage decoded = BaseMessage.read(json);
    assertTrue(decoded instanceof EventMessage);

    PollEvent deserialized = (PollEvent) ((EventMessage) decoded).getEvent();
    assertEquals(original.getQuestion(), deserialized.getQuestion());
    assertEquals(original.getOptions().size(), deserialized.getOptions().size());
}
```

### Integration Tests

Test with real relay (using Testcontainers):

```java
@Test
void testSendPollToRelay() throws Exception {
    // Use testcontainer relay or local relay
    String relayUrl = "ws://localhost:5555";

    Identity identity = Identity.generateRandomIdentity();
    PollEvent poll = createTestPoll(identity.getPublicKey());
    identity.sign(poll);

    try (StandardWebSocketClient client = new StandardWebSocketClient(relayUrl)) {
        List<String> responses = client.send(new EventMessage(poll));
        assertFalse(responses.isEmpty());
    }
}
```

### Contribution Checklist

Before submitting a PR:

- [ ] Run `mvn -q verify` – all tests pass
- [ ] Event complies with relevant NIP
- [ ] Added unit tests (>80% coverage)
- [ ] Added integration tests if applicable
- [ ] Updated documentation
- [ ] Added example usage
- [ ] Removed unused imports
- [ ] Followed code style (use formatter)
- [ ] Updated CHANGELOG or release notes
- [ ] Tested with real relay

### Contributing Guidelines

1. **Run verification**:
   ```bash
   mvn -q verify
   ```

2. **Ensure NIP compliance**: Events should follow Nostr specifications

3. **Include comprehensive tests**: Cover edge cases and error conditions

4. **Document your changes**: Add examples and update relevant docs

5. **Follow PR template**: Complete all sections in `.github/pull_request_template.md`

For complete contribution guidelines, see [CONTRIBUTING.md](../../CONTRIBUTING.md).

---

## Real-World Examples

Study these existing implementations:

- **Simple event**: [`ContactListEvent`](../../nostr-java-event/src/main/java/nostr/event/impl/ContactListEvent.java) – basic validation
- **Complex event**: [`CalendarRsvpEvent`](../../nostr-java-event/src/main/java/nostr/event/impl/CalendarRsvpEvent.java) – custom content type
- **Tag implementation**: [`ExpirationTag`](../../nostr-java-event/src/main/java/nostr/event/tag/ExpirationTag.java) – tag with updateFields
- **Factory**: [`GenericEventFactory`](../../nostr-java-api/src/main/java/nostr/api/factory/impl/GenericEventFactory.java) – flexible factory pattern

## See Also

- [Custom Events How-To](../howto/custom-events.md) – Basic custom event creation
- [API Reference](../reference/nostr-java-api.md) – API documentation
- [NIP-16](https://github.com/nostr-protocol/nips/blob/master/16.md) – Event kind ranges
- [Contributing Guide](../../CONTRIBUTING.md) – Full contribution guidelines
