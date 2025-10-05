# Extending Events

Navigation: [Docs index](../README.md) · [API how‑to](../howto/use-nostr-java-api.md) · [Custom events](../howto/custom-events.md) · [API reference](../reference/nostr-java-api.md)

This project uses factories and registries to make it easy to introduce new event types while keeping core classes stable.

## Factory and Registry Overview

- **Event factories** (e.g. [`EventFactory`](../../nostr-java-api/src/main/java/nostr/api/factory/EventFactory.java) and its implementations) centralize event creation so that callers don't have to handle boilerplate like setting the sender, tags, or content.
- **TagRegistry** maps tag codes to concrete implementations, allowing additional tag types to be resolved at runtime without modifying `BaseTag`.

## Adding a New Event Type

1. **Define the kind.** Add a constant to [`Kind`](../../nostr-java-base/src/main/java/nostr/base/Kind.java) or reserve a custom value.
2. **Implement the event.** Create a class under `nostr.event.impl` that extends `GenericEvent` or a more specific base class.
3. **Provide a factory.** Implement a factory extending `EventFactory` to encapsulate default tags and content for the new event.
4. **Register tags.** If the event introduces new tag codes, register their factory functions with [`TagRegistry`](../../nostr-java-event/src/main/java/nostr/event/tag/TagRegistry.java).
5. **Write tests.** Add unit and integration tests covering serialization, deserialization, and NIP compliance.
6. **Follow contributing guidelines.** Run `mvn -q verify` before committing, ensure events comply with Nostr NIPs, and document your changes.

## Testing & Contribution Requirements

- Run `mvn -q verify` from the repository root and ensure all checks pass.
- Include comprehensive tests for new functionality and remove unused imports.
- Summaries of changes and test results are expected in pull requests.

Refer to the repository's `AGENTS.md` for the full list of contribution expectations.
