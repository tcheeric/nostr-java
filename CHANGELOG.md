# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by Keep a Changelog, and this project adheres to semantic versioning once 1.0.0 is released.

## [Unreleased]

## [2.3.0] - 2026-08-30

### Added
- MCP guided prompts (`compose-note`, `catch-up-feed`, `watch-mentions`) and context resources (`nostr://identity/{alias}`, `nostr://relay/{name}`). The prompts encode the sequences models get wrong, such as treating a publish preview as the publication or reading a still-replaying subscription as an empty one, since a tool surface with no guidance makes an agent learn by trial and error on a permanent public medium.
- MCP packaging: a runnable jar (`-runnable` classifier), a distroless `Dockerfile` running as a non-root user, and a `docker-compose.yml` that runs the server beside a local relay and demonstrates one bound container per identity. Every published port binds the host loopback explicitly, because Docker otherwise publishes to all interfaces and would bypass the server's own loopback default.
- MCP HTTP transport, selected with `nostr.mcp.transport=http`, for hosted deployments where the MCP host does not launch the process itself. It has no authentication of its own, so it binds `127.0.0.1` by default and warns at startup when configured otherwise; exposing it beyond the machine needs a reverse proxy with real credentials in front. Documented in a new how-to guide, [Run the Nostr MCP server](docs/howto/run-the-mcp-server.md), whose settings table is checked against the code by a test.
- MCP social tools: `nostr_fetch_thread`, `nostr_get_contacts`, `nostr_send_direct_message` and `nostr_read_direct_messages`, plus `replyTo` and `mentions` on note publishing so a reply is threaded rather than detached. Direct messages use NIP-17 gift wrapping, so relays see neither the correspondents nor the content, and NIP-04 is not exposed at all. Two delivery facts are surfaced rather than smoothed over: a recipient who published no kind-10050 relay list is reported unreachable by name, and the sender's own archival copy is reported separately from the recipients so it is never counted as a failed delivery. Decrypting incoming messages is opt-in per identity via `nostr.mcp.dm.decrypt-for`.
- MCP subscriptions: `nostr_subscribe`, `nostr_read_subscription`, `nostr_list_subscriptions` and `nostr_unsubscribe`, so an agent can watch for events that have not happened yet. MCP cannot push into a tool result, so events land in a bounded per-subscription buffer that the agent drains; overflow drops the oldest and reports a monotonic count, because an agent told nothing about a gap will summarise a partial feed as the whole one. Subscriptions are also exposed as `nostr://subscription/{id}` resources with update notifications, are capped in number, and are reaped once nobody has read them, since an agent's session can end without the server being told.
- MCP identity lifecycle tools, shaped around what can and cannot be undone. Creating, renaming and choosing a default are freely allowed; exporting a backup and removing a key are guarded. `nostr_import_identity` accepts no key material at all: `source` names a file, an environment variable or a terminal prompt that the server reads for itself, so a key never passes through the model's context. `nostr_remove_identity` confirms in two steps and refuses outright unless a backup exists or the caller explicitly acknowledges there is none. `identity-policy` governs these separately from `write-policy` and is capped by it, so a read-only server cannot mutate the keystore either.
- MCP publishing: `nostr_publish_note`, `nostr_publish_event` and `nostr_update_profile`, all passing through one `WriteGuard`. `write-policy: confirm` is the default, so a write is previewed and published only when the agent returns the token it was given, turning a hallucinated post into a no-op; `deny` registers no write tool at all, and `allow` publishes directly. Writes are rate-limited per identity, every write is logged with event id, kind, signing key and target relays, and a partial success reports the per-relay outcome rather than an error, so an agent is never told to retry a write that already landed.
- MCP read tools: `nostr_query_events`, `nostr_get_profile` (by public key or NIP-05 address) and `nostr_relay_info`. Shared argument conventions live in one place: `NostrIdentifier` accepts hex or bech32 for keys and event ids, and `TimeArgument` normalises relative ages such as `24h`, ISO-8601 timestamps and bare dates to Unix seconds. Queries are bounded by `limits.max-events-per-query` and `limits.query-timeout`, and a truncated or timed-out answer says so rather than passing as complete.
- MCP single-identity mode and key-admin CLI. `nostr.mcp.identity` binds a server process to one alias: only that entry is decrypted, so another identity's key is absent from the heap rather than merely refused, and a bound server registers no keystore-mutating tools. The same jar offers `keygen`, `import`, `list` and `remove` on the command line, keeping key administration in the hands of the human who set the servers up rather than any agent.
- MCP identity vault: `nostr_list_identities` tool with os-keychain, encrypted-file, and environment key sources; private keys never appear on the tool surface and are wiped on shutdown.
- **`nostr-java-mcp`**, a new module exposing the SDK as a Model Context Protocol server so an LLM agent can use Nostr without Nostr-specific code. This first slice ships the stdio transport an MCP host launches directly, a tool registry where adding a capability means adding a class rather than editing a dispatcher, and `nostr_list_relays` reporting each configured relay's connection state. It adapts `nostr-java-api` rather than reaching past it, so relay pooling, result aggregation and de-duplication stay the SDK's concern.
- `ContactList` and `Contact`, modelling a NIP-02 follow list. Each entry keeps the three parts the specification defines, the followed key plus an optional relay hint and petname, rather than the key alone: the hint is how a client finds someone it has never seen, and the petname is how it shows a readable name without a global registry. Entries keep their order, since NIP-02 asks that new follows be appended so a list reads chronologically, and a duplicated key keeps its first entry. Malformed entries are discarded rather than making a whole list unreadable. Groundwork for the planned `nostr-java-mcp` module, whose contacts tool had nothing to call.

### Fixed
- A server bound to a single identity still offered an `identity` argument on its five signing tools, so the argument binding exists to remove was merely redundant rather than absent. An argument with exactly one acceptable value invites a model to pass a different one, turning an impossible mistake back into a possible one. Found by driving the shipped jar as an MCP host does.
- Settings whose names contain a hyphen, including `write-policy`, `bind-address` and every `limits.*` entry, could not be set from the environment: the name was translated to an environment variable by replacing dots but not hyphens, producing names such as `NOSTR_MCP_BIND-ADDRESS` that no shell can set. Hyphens are now translated too, which is what makes the container configurable at all.
- Inbound relay frames are delivered to each listener in the order the relay sent them. Every frame was previously dispatched on a freshly started virtual thread, so an `EOSE` could overtake the stored events it follows; any query ending on that signal then returned a partial answer indistinguishable from the relay holding less data. Observed against a real relay: asking for three stored events, the end-of-backlog signal arrived with only one or two delivered, varying run to run.

## [2.2.0] - 2026-08-30

### Added
- **`nostr-java-api`**, a new module and the intended entry point for applications. `NostrClient` ties an identity to a set of relays and covers what every client would otherwise write itself: signing and publishing in one call, subscribing across relays, and sending NIP-17 private direct messages. Events remain `GenericEvent` and the relay pool stays reachable, so the facade adds capability without walling anything off. Ownership follows construction: a pool the client built is closed with it, a pool passed in is left to its owner ([ADR-0001](docs/decisions/0001-introduce-nostr-java-api-module.md)).
- `RelayListLookup`, the first real implementation of `DirectMessageRelayLookup`, resolving kind-10050 lists through the relay pool. `nostr-java-identity` declared this need but could not meet it without acquiring a transport.
- `DirectMessagePublisher`, which performs the delivery plan `nostr-java-identity` can only produce: each gift wrap goes to its own recipient's relays, and every participant's outcome is reported, since a group message that reaches three of four people has partly succeeded.
- Runtime relay pool membership. Relays join and leave a running pool, and one borrowed to reach a message recipient is released afterwards by counting holders, so overlapping deliveries do not cut each other off.
- Multi-relay subscriptions. `RelayPool.subscribe` registers one filter with every relay and presents the result as a single stream: each event is delivered once however many relays hold it, already parsed as a `GenericEvent`, with de-duplication through a bounded window so a long-lived firehose cannot grow its memory without limit. The per-relay `EOSE` frames are aggregated into one end-of-backlog signal, emitted when every relay has reported or a timeout expires, so an unresponsive relay cannot leave an application loading forever. A relay that drops mid-stream is reported to the caller and re-subscribed from its stored filter when it reconnects, and a malformed payload is reported without ending the subscription ([ADR-0004](docs/decisions/0004-pool-concurrency-and-subscription-lifecycle.md), [ADR-0005](docs/decisions/0005-pool-membership-eose-and-ownership.md)).
- `RelayPool` now serialises operations per relay, so several threads can publish at once without colliding with `NostrRelayClient`'s one-request-in-flight limit. Locking is per relay rather than pool-wide, so a slow relay delays only its own queue while fan-out across relays stays concurrent. Each relay's `ConnectionState` is observable, and downed relays are retried on a schedule the pool owns, so a relay that recovers rejoins without an application restart. Relays that drop after connecting are reconnected too, not only those that failed at startup ([ADR-0004](docs/decisions/0004-pool-concurrency-and-subscription-lifecycle.md)).
- `RelayPool`, which publishes one event to many relays at once and reports what each of them did. `PublishResult` records, per relay, acceptance, rejection with the relay's verbatim reason, a timeout, or unreachability, so partial delivery is visible instead of collapsed into a boolean. A publish that no relay accepted throws `NoRelayAcceptedException` carrying the same result, because an event that reached nobody must not be mistaken for a published one. The pool is best-effort on construction, so an unreachable relay cannot stop an application starting, and one timeout bounds the whole publish rather than each relay in turn ([ADR-0002](docs/decisions/0002-multi-relay-failure-semantics.md)).
- `RelayConnection` and `RelayConnectionFactory`, the seam between relay coordination and relay transport. `NostrRelayClient` implements the interface, which exposes only what code coordinating several relays needs (identify, send, subscribe, observe state, close) rather than mirroring the client's full surface. Behaviour is unchanged; the seam exists so that multi-relay work can be tested against scripted relay behaviour instead of live sockets, and so modules above it need not depend on Spring. Groundwork for the planned `nostr-java-api` module ([ADR-0001](docs/decisions/0001-introduce-nostr-java-api-module.md)).

### Fixed
- Stored events could arrive after the end-of-backlog signal that is supposed to follow them. The transport dispatches each inbound frame on its own thread, so an `EOSE` could overtake the events it trails and tell an application its backlog was drained while those events were still arriving. Subscriptions now deliver frames in the order the relay sent them. Found by testing against a live relay, where a lookup intermittently reported no result for a list the relay was serving.

## [2.1.0] - 2026-08-30

### Added
- NIP-17 private direct messages, with the NIP-59 gift wrapping they build on. `Nip17DirectMessageService` composes a `ChatMessage` into one gift wrap per participant and reads incoming wraps back; `Nip59GiftWrapper` implements the generic three-layer envelope (unsigned kind-14 rumor, kind-13 seal signed by the real author, kind-1059 wrap signed by a single-use key) and is usable for any event kind, not just messages. Unlike NIP-04, which hides only the message text, this conceals the correspondents, the timing, and the message count.
- `Rumor`, the unsigned event NIP-59 wraps. It is deliberately not `ISignable` and holds no signature field, so the deniability the scheme depends on is enforced by the type system rather than by convention.
- `DirectMessageRelayList` (kind 10050) and `DirectMessageService.planDelivery`, which pairs each participant's copy with the relays that participant nominated. NIP-17 permits delivery only to those relays and forbids sending at all to someone who published no list; an unreachable recipient is reported explicitly rather than omitted, so a message cannot go half-delivered unnoticed.
- `GenericEvent.update(long createdAt)`, which recomputes an event's id without consulting the clock. The existing no-arg `update()` delegates to it, so no call site changes behaviour.
- [How to send private direct messages](docs/howto/private-direct-messages.md).

### Fixed
- `GenericEvent.getByteArraySupplier()` no longer resets `created_at` to the current time. It calls `update()`, and so ran during `Identity.sign()` — meaning **signing silently moved an event in time**. Any deliberately chosen timestamp was discarded moments after being set, which made NIP-59's randomised past timestamps impossible to produce and defeated the timing-correlation defence they exist to provide, while the calling code read as though the protection were present. An event that already carries a creation time now keeps it.

### Deprecated
- NIP-04 encrypted direct messages (`EncryptedDirectMessage`, `MessageCipher04`). They remain functional for reading existing conversations and interoperating with clients that send nothing else, but new code should use NIP-17. Nothing is removed in this release.

## [2.0.8] - 2026-08-22

### Fixed
- Relay payloads are now delivered only to the listener whose subscription they name. `NostrRelayClient.dispatchMessage()` broadcast every inbound frame to every listener on the connection, and nothing downstream read the subscription id the frame carried — so on a connection with two concurrent REQs, one subscription's `EOSE` fired the other's EOSE handler and ended its query early with whatever had arrived so far. Observed in the field as a gift-wrap query returning 0, 2 or 6 events at random from a relay holding exactly 5. `EVENT`, `EOSE` and `CLOSED` are now routed by subscription id for listeners registered through `subscribe(ReqMessage, …)`; connection-scoped frames (`NOTICE`, `OK`, `AUTH`) and listeners registered with raw JSON still see everything, so no existing caller loses a frame it receives today.
- NIP-44 no longer depends on a JCE provider being registered. `EncryptedPayloads` asked for `Cipher.getInstance("ChaCha20")` with an `IvParameterSpec`, which only BouncyCastle's provider accepts — SunJCE requires a `ChaCha20ParameterSpec` and throws. The provider happened to be registered as a side effect of `Schnorr.generatePrivateKey()`, so encryption worked for callers who generated a key and failed for callers who loaded one, and could not work on Android at all (adding a provider named "BC" is a no-op there; the platform owns the name). Both cipher call sites now use BouncyCastle's lightweight `ChaCha7539Engine`, which needs no provider lookup. Closes #537.

### Changed
- `Schnorr.generatePrivateKey()` no longer calls `Security.addProvider(new BouncyCastleProvider())`. It uses BouncyCastle's lightweight `ECKeyPairGenerator` instead, so generating a key no longer mutates process-wide JCE state. Callers that relied on nostr-java registering the provider for them must now register it themselves.

### Added
- NIP-44 v2 is verified against the specification's own test vectors (`nip44.vectors.json` from the reference implementation), and the whole suite runs with the BouncyCastle provider de-registered so the defect above cannot come back unnoticed.

## [2.0.7] - 2026-05-27

### Fixed
- Closed a TOCTOU window in the 2.0.6 `send()` `isOpen()` guard (PR #526 review). The check was performed at the top of `send()`, *before* `sendFrameGated()` acquired the `sessionGate` read lock — so a concurrent `close()` could acquire the write lock, close the session, and release it between the check and the actual `sendMessage()`, still letting the frame reach a closed session. Moved the `isOpen()` check inside `sendFrameGated()`, immediately after the read lock is taken, so the open-check and the write are now atomic with respect to `closeGated()` (which holds the write lock). 2.0.6 already handled the dominant case (the session already closed when `send()` is invoked); this closes the narrow in-flight-close window.

## [2.0.6] - 2026-05-27

### Fixed
- `NostrRelayClient.send()` now guards on `clientSession.isOpen()` before writing, mirroring `subscribe()`. A per-subscription Nostr `CLOSE` issued while a relay connection was being torn down previously reached Tomcat's `sendText`, where `WsRemoteEndpointImplBase.sendMessageBlockInternal` invokes `doClose()` mid-write and emits a WS CLOSE frame while the text write is still pending on the async channel — throwing `IllegalStateException: Concurrent write operations are not permitted`, which surfaced via `handleTransportError` as a transport-error reconnect storm during subscription teardown of a breaking connection (spec-026). The application-level locks (the decorator, the `sessionGate`, and the upstream adapter `sendLock`) cannot prevent this because it is Tomcat closing the session *inside* a single in-progress send, not two application threads racing. `send()` now fails fast with `IOException("WebSocket session is closed")` on a closed session — the doomed write never enters Tomcat's close-mid-write path. +regression test `send_onClosedSession_failsFastWithoutDelegateWrite`.

## [2.0.5] - 2026-05-26

### Fixed
- `NostrRelayClient.send()` no longer leaves the client stuck in the "request in flight" state when the write fails for a reason other than overflow. A plain transport `IOException` from the gated send previously bypassed `pendingRequest` cleanup, so the next `send()` (including an `@NostrRetryable` retry) hit `IllegalStateException: A request is already in flight` instead of retrying. `send()` now clears `pendingRequest` on any send failure, not only `SessionLimitExceededException`. (PR #525 review follow-up.)
- `closeQuietly()` now logs the swallowed throwable (with stack trace) and the relay URI instead of just the exception message, preserving diagnostics for best-effort closes.

## [2.0.4] - 2026-05-26

### Fixed
- Close-vs-write race in `NostrRelayClient` (spec-026 US3). The client wrapped its session in Spring's `ConcurrentWebSocketSessionDecorator` but called the **no-arg** `clientSession.close()` (in `close()` and the `send()` timeout path). Spring 6.2.x's decorator overrides only `close(CloseStatus)` (guarded by `closeLock`); the no-arg `close()` falls through to `WebSocketSessionDecorator.close()` → `delegate.close()` with no coordination, sending a CLOSE frame straight to the Tomcat delegate while a `sendMessage` flush was in flight (Tomcat permits only one write in flight → `IllegalStateException: Concurrent write operations are not permitted`). Routing through `close(CloseStatus)` alone is insufficient because its `closeLock` is a separate lock from the `flushLock` guarding `delegate.sendMessage`. Introduced a `ReentrantReadWriteLock` session gate: every write (`send`/`subscribe`) holds the read side (sends stay concurrent — the decorator still serialises the delegate writes among them), every close holds the write side and always uses `close(CloseStatus)`, so a close waits for all in-flight sends to drain before sending the CLOSE frame. No lock nesting → no added deadlock risk.

### Removed
- Dead code cleanup — deleted unused classes: `IContent`, `JsonContent`, `Reaction` enum, `Response`, `Nip05Content`, `Nip05ContentDecoder`, `BaseAuthMessage`, `GenericMessage`, `IKey`, `GenericEventConverter`, `GenericEventTypeClassifier`, `GenericEventDecoder`, `FiltersDecoder`, `BaseTagDecoder`, `GenericEventValidator`, `GenericEventSerializer`, `GenericEventUpdater`, `GenericTagQuery`, `HttpClientProvider`, `DefaultHttpClientProvider`.
- `testAuthMessage` test and `GenericEventSupportTest` removed (tested deleted classes).
- `createGenericTagQuery()` removed from `EntityFactory` (only consumer of deleted `GenericTagQuery`).

### Changed
- `RelayAuthenticationMessage` and `CanonicalAuthenticationMessage` now extend `BaseMessage` directly (previously extended the now-deleted `BaseAuthMessage`).
- `BaseKey` now directly implements `Serializable` (previously implemented the now-deleted `IKey` interface).
- `Nip05Validator` now creates `HttpClient` instances directly via a `Function<Duration, HttpClient>` factory (previously used deleted `HttpClientProvider`/`DefaultHttpClientProvider` interface).

## [2.0.3] - 2026-05-08

### Fixed
- Explicit close on `OverflowStrategy.TERMINATE`; restores upstream `isOpen()==false` reconnect contract. Spring's `ConcurrentWebSocketSessionDecorator` under `OverflowStrategy.TERMINATE` only sets a private `limitExceeded` flag and throws `SessionLimitExceededException` from `limitExceeded()` — it does **not** close the delegate session. As a result, after 2.0.2 the upstream caller's `clientSession.isOpen()==false` → reconnect contract did not hold (the session stayed open after overflow). `NostrRelayClient.subscribe()` and `NostrRelayClient.send()` now detect a `SessionLimitExceededException` cause in their respective catch blocks and call `clientSession.close(CloseStatus.SESSION_NOT_RELIABLE)` explicitly before rewrapping the exception. Non-overflow `RuntimeException`s continue to flow through the existing wrap-as-`IOException` path unchanged. The §6.7d concurrency test now strictly asserts `clientSession.isOpen()==false` after overflow, matching the spec's original contract.

## [2.0.2] - 2026-05-08

### Fixed
- `NostrRelayClient.subscribe()` was calling `clientSession.sendMessage(...)` without holding the existing `sendLock`, while the same class's `send()` path correctly held it. Two threads racing inside `subscribe()` (or one in `subscribe()` racing one in `send()`) could trigger the underlying writer's `IllegalStateException("The remote endpoint was in state [TEXT_FULL_WRITING]")` (Tomcat / Spring `StandardWebSocketSession`) or `Blocking message pending` (Jetty), which the `catch (RuntimeException)` block at the bottom of `subscribe()` rewrapped as `IOException("Failed to send subscription payload", e)`. Downstream consumer (`imani-gateway-core` `account-app`) observed ~70 `RelaySubscribeException` per 60 minutes once a circuit-breaker tuning fix unmasked the underlying race. Fixed by wrapping the underlying `WebSocketSession` returned by `connectSession(...)` in Spring's `ConcurrentWebSocketSessionDecorator(session, sendTimeLimit, bufferSizeLimit, OverflowStrategy.TERMINATE)` at construction time, so concurrent `sendMessage()` calls from any send-path are serialised at the session layer.

### Added
- New canonical four-arg public constructor `NostrRelayClient(String relayUri, long awaitTimeoutMs, int sendBufferLimit, int sendTimeLimitMs)` annotated `@Autowired` for Spring constructor-injection. Spring binds against this overload via the new `@Value("${nostr.websocket.send-buffer-limit:262144}")` and `@Value("${nostr.websocket.send-time-limit-ms:10000}")` keys (also reachable via the `NOSTR_WEBSOCKET_SEND_BUFFER_LIMIT` / `NOSTR_WEBSOCKET_SEND_TIME_LIMIT_MS` env-vars under Spring relaxed binding). The previously-existing one-arg `(String)` and two-arg `(String, long)` public constructors are retained as delegating overloads (binary-compatible) and now also benefit from the decorator wrap by way of the canonical ctor.
- Test-only static factories `forTestWithRawSession(...)` and `forTestWithDecoratedSession(...)` (two overloads — defaults and explicit) plus a package-private four-arg test constructor, so the new `NostrRelayClientConcurrencyTest` can assert both the regression (raw-session reproduction of the `IllegalStateException` race) and the resolution (decorator-wrapped session serialises sends).

### Changed
- `awaitTimeoutMs` is now a `final` field assigned once via constructor injection (previously `@Value`-annotated field). Spring's constructor-injection ordering guarantees the value reaches the constructor body before the decorator is constructed, which is required for the explicit overflow strategy to be applied. The system property / env-var key (`nostr.websocket.await-timeout-ms`) and its default (60 000 ms) are unchanged.

## [2.0.1] - 2026-05-06

### Fixed
- `NostrRelayClient` log statements were emitting `Sending request to relay null: ...` (and similar) once the WebSocket session had closed, because the relay URI was being read via `clientSession.getUri()` which returns `null` after close. Captured the URI in a `private final String relayUri` field set in each constructor and replaced 8 `clientSession.getUri()` log call-sites. Resolves 188 occurrences per 2-hour window observed in a downstream consumer's staging logs ([#523](https://github.com/tcheeric/nostr-java/pull/523)).

## [2.0.0] - 2026-02-24

This is a major release that implements the full design simplification described in `docs/developer/SIMPLIFICATION_PROPOSAL.md`, reducing the library from 9 modules with ~180 classes to 4 modules with ~40 classes.

### Added
- `Kinds` utility class with static `int` constants for common Nostr event kinds (`TEXT_NOTE`, `SET_METADATA`, `CONTACT_LIST`, etc.) and range-check methods (`isReplaceable()`, `isEphemeral()`, `isAddressable()`, `isValid()`).
- `GenericTag.of(String code, String... params)` factory method for concise tag creation.
- `GenericTag.toArray()` returning the NIP-01 wire format `["code", "param0", "param1", ...]`.
- `GenericTag` now stores tag values as `List<String>` (replacing `List<ElementAttribute>`), providing direct access via `getParams()`.
- `EventFilter` builder API for composable relay filters: `.kinds()`, `.authors()`, `.since()`, `.until()`, `.addTagFilter()`, `.limit()`, `.ids()`.
- `RelayTimeoutException` — typed exception replacing silent empty-list returns on relay timeout.
- `ConnectionState` enum (`CONNECTING`, `CONNECTED`, `RECONNECTING`, `CLOSED`) for WebSocket connection state tracking.
- `NostrRelayClient` async Virtual Thread APIs: `connectAsync(...)`, `sendAsync(...)`, and `subscribeAsync(...)`.
- `Nip05Validator.validateAsync()` and `Nip05Validator.validateBatch(...)` for parallel NIP-05 validation workloads.
- Spring Retry support (`@NostrRetryable`, `@Recover`) consolidated directly into `NostrRelayClient`.

### Changed
- **Module consolidation** — merged 9 modules into 4:
  - `nostr-java-util` + `nostr-java-crypto` → `nostr-java-core`
  - `nostr-java-base` + `nostr-java-event` → `nostr-java-event`
  - `nostr-java-id` + `nostr-java-encryption` → `nostr-java-identity`
  - `nostr-java-client` (unchanged)
- **`GenericEvent`** is now the sole event class. All 39 concrete event subclasses removed. Events are differentiated by `int kind` instead of Java type.
- **`GenericTag`** is now the sole tag class. All 17 concrete tag subclasses removed. Tags are a simple `code` + `List<String> params`.
- `GenericEvent.kind` changed from `Kind` enum to plain `int`. Builder simplified to `.kind(int)` only.
- `GenericEvent.tags` changed from `List<BaseTag>` to `List<GenericTag>`.
- `GenericEvent` implements `ISignable` directly (no longer extends `BaseEvent`).
- `EventMessage` now references `GenericEvent` directly instead of `IEvent`.
- `IDecoder<T>` type bound changed from `IDecoder<T extends IElement>` to unbounded `IDecoder<T>`.
- `TagDeserializer` now always produces `GenericTag` with `List<String>` params — no registry dispatch.
- `GenericTagSerializer` simplified to output `[code, param0, param1, ...]` directly from `List<String>`.
- `GenericEventDeserializer` simplified — no subclass dispatch to concrete event types.
- `NostrUtil.bytesToHex()` now uses `java.util.HexFormat` instead of hand-rolled hex encoding.
- `NostrUtil.hexToBytes()` family of methods now uses `java.util.HexFormat.parseHex()` — fails fast on invalid hex instead of silently producing corrupt bytes.
- WebSocket client termination detection now uses proper JSON parsing instead of brittle string-prefix matching.
- `StandardWebSocketClient` renamed to `NostrRelayClient`.
- `SpringWebSocketClient` absorbed into `NostrRelayClient` (single client class with retry support).
- Relay subscription callbacks are now dispatched on Virtual Threads to avoid blocking inbound WebSocket processing.
- `DefaultHttpClientProvider` now uses a shared Virtual Thread executor instead of creating a new executor per `HttpClient`.
- All `synchronized` blocks in `NostrRelayClient` replaced with `ReentrantLock` to avoid Virtual Thread pinning.
- Configurable max events per request limit (default 10,000) to prevent unbounded memory accumulation.

### Fixed
- **`GenericTag.getCode()` NPE** — structurally eliminated by removing the dual-path tag architecture. `getCode()` is now a trivial field accessor with zero NPE risk.
- Removed the remaining `synchronized` cleanup block from `NostrRelayClient.send(...)`, using `ReentrantLock` consistently to avoid VT pinning risk.
- Relay timeout now throws `RelayTimeoutException` instead of silently returning an empty list, allowing callers to distinguish "no results" from "timed out".

### Removed
- **`nostr-java-api` module** — all 26 NIP classes (NIP01–NIP99), `EventNostr`, factory classes, client managers, service layer, and configuration classes.
- **`nostr-java-examples` module** — all 6 example classes.
- **39 concrete event subclasses** — `TextNoteEvent`, `DirectMessageEvent`, `ContactListEvent`, `ReactionEvent`, `DeletionEvent`, `EphemeralEvent`, `ReplaceableEvent`, `AddressableEvent`, all Calendar/Marketplace/Channel/NostrConnect events, and more. Use `GenericEvent` with the appropriate `int kind`.
- **17 concrete tag subclasses** — `EventTag`, `PubKeyTag`, `AddressTag`, `IdentifierTag`, `ReferenceTag`, `HashtagTag`, `ExpirationTag`, `UrlTag`, `SubjectTag`, `DelegationTag`, `RelaysTag`, `NonceTag`, `PriceTag`, `EmojiTag`, `GeohashTag`, `LabelTag`, `LabelNamespaceTag`, `VoteTag`. Use `GenericTag.of(code, params...)`.
- **27 entity classes** — `UserProfile`, `Profile`, `ChannelProfile`, `ZapRequest`, `ZapReceipt`, `Reaction`, all Cashu entities, all marketplace entities, and more.
- **`Kind` enum** — replaced by `Kinds` utility class with static `int` constants.
- **`ElementAttribute`** — replaced by `List<String>` in `GenericTag`.
- **`TagRegistry`** — no longer needed with a single tag class.
- **Interfaces and abstract classes**: `IElement`, `ITag`, `IEvent`, `IGenericElement`, `IBech32Encodable`, `Deleteable`, `BaseEvent`, `BaseTag`.
- **Annotations**: `@Tag`, `@Event`, `@Key`.
- **14 filter classes** — `AbstractFilterable`, `KindFilter`, `AuthorFilter`, `SinceFilter`, `UntilFilter`, `HashtagTagFilter`, `AddressTagFilter`, `GeohashTagFilter`, `IdentifierTagFilter`, `ReferencedEventFilter`, `ReferencedPublicKeyFilter`, `UrlTagFilter`, `VoteTagFilter`, `GenericTagQueryFilter`. Use `EventFilter.builder()`.
- **Concrete serializers/deserializers** — `AddressTagSerializer`, `ReferenceTagSerializer`, `ExpirationTagSerializer`, `IdentifierTagSerializer`, `RelaysTagSerializer`, `BaseTagSerializer`, `AbstractTagSerializer`, `CalendarEventDeserializer`, `ClassifiedListingEventDeserializer`, `CashuTokenSerializer`.
- **Client classes** — `WebSocketClientIF`, `WebSocketClientFactory`, `SpringWebSocketClientFactory`, `SpringWebSocketClient`. Use `NostrRelayClient` directly.
- **Dead code** — `Marker` enum, `RelayUri` value object.
- **5 old modules** — `nostr-java-util`, `nostr-java-crypto`, `nostr-java-base`, `nostr-java-id`, `nostr-java-encryption` (merged into the 4 remaining modules).
- Dead `pollIntervalMs` parameter from WebSocket client constructors.

## [1.3.0] - 2026-01-25

### Added
- Configurable WebSocket buffer sizes for handling large Nostr events via `nostr.websocket.max-text-message-buffer-size` and `nostr.websocket.max-binary-message-buffer-size` properties.

### Changed
- No additional behavior changes in this release; Kind APIs and WebSocket concurrency improvements were introduced in 1.2.1.

### Fixed
- No new fixes beyond 1.2.1; this release focuses on configurable WebSocket buffer sizes.
## [1.2.1] - 2026-01-21

### Fixed
- NIP-44 now correctly uses HKDF-Extract for conversation key derivation, ensuring proper cryptographic key generation.
- WebSocket client now correctly accumulates all relay responses (EVENT messages) before completing, waiting for termination signals (EOSE, OK, NOTICE, CLOSED) instead of returning after the first message.
- WebSocket client thread-safety improved by encapsulating pending request state, preventing potential race conditions when multiple threads call send() concurrently.
- WebSocket client now rejects concurrent send() calls with IllegalStateException instead of silently orphaning the previous request's future.
- KindFilter and ClassifiedListingEventDeserializer now use Kind.valueOfStrict() for fail-fast deserialization of unknown kind values.

### Changed
- Kind.valueOf(int) now returns null for unknown kind values instead of throwing, allowing graceful handling of custom or future NIP kinds during JSON deserialization.
- Added Kind.valueOfStrict(int) for callers who need fail-fast behavior on unknown kinds.
- Added Kind.findByValue(int) returning Optional<Kind> for safe, explicit handling of unknown kinds.

## [1.2.0] - 2025-12-26

### Fixed
- NIP-44 encryption now correctly uses HKDF instead of PBKDF2 for key derivation, as required by the specification. This fix enables DM interoperability between Java backend and JavaScript frontend implementations (e.g., nostr-tools).

### Changed
- Switched integration tests to use strfry relay for improved robustness.

### Removed
- Removed AGENTS.md and CLAUDE.md documentation files from the repository.

## [1.1.1] - 2025-12-24

### Fixed
- StandardWebSocketClient now configures WebSocketContainer with a 1-hour idle timeout (configurable via `nostr.websocket.max-idle-timeout-ms`) to prevent premature connection closures when relays have periods of inactivity.

## [1.1.0] - 2025-12-23

### Added
- Public constructor `StandardWebSocketClient(String relayUri, long awaitTimeoutMs, long pollIntervalMs)` for programmatic timeout configuration outside Spring DI context.

### Changed
- Enhanced diagnostic logging for timeout configuration in StandardWebSocketClient.
- Simplified WebSocket client initialization and retry logic in tests.

### Fixed
- Updated `JsonDeserialize` builder reference in API module.

## [1.0.1] - 2025-12-20

### Changed
- Updated project version and added artifact names in POM files.
- Added Sonatype Central server credentials configuration.
- Updated Maven command for central publishing.

## [1.0.0] - 2025-10-13

### Added
- Release automation script `scripts/release.sh` with bump/tag/verify/publish/next-snapshot commands (supports `--no-docker`, `--skip-tests`, and `--dry-run`).
- GitHub Actions:
  - CI workflow `.github/workflows/ci.yml` with Java 21 build and Java 17 POM validation; separate Docker-based integration job; uploads reports/artifacts.
  - Release workflow `.github/workflows/release.yml` publishing to Maven Central, validating tag vs POM version, and creating GitHub releases.
- Documentation:
  - `docs/explanation/dependency-alignment.md` — BOM alignment and post-1.0 override removal plan.
  - `docs/howto/version-uplift-workflow.md` — step-by-step release process; wired to `scripts/release.sh`.

### Changed
- Roadmap project helper `scripts/create-roadmap-project.sh` now adds tasks for:
  - Release workflow secrets setup (Central + GPG)
  - Enforcing tag/version parity during releases
  - Updating docs version references to latest
  - CI + Docker IT stability and triage plan
- Expanded decoder and mapping tests to cover all implemented relay commands (EVENT, CLOSE, EOSE, NOTICE, OK, AUTH).
- Stabilized NIP-52 (calendar) and NIP-99 (classifieds) integration tests for deterministic relay behavior.
- Docs updates to prefer BOM usage:
  - `docs/GETTING_STARTED.md` updated with Maven/Gradle BOM examples
  - `docs/howto/use-nostr-java-api.md` updated to import BOM and omit per-module versions
  - Cross-links added from the roadmap to migration and dependency alignment docs
- README cleanup: removed maintainer-only roadmap automation and moved troubleshooting to `docs/howto/diagnostics.md`.

### Removed
- Deprecated APIs finalized for 1.0.0:
  - `nostr.config.Constants.Kind` facade — use `nostr.base.Kind`
  - `nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD` — use `nostr.event.json.EventJsonMapper#getMapper()`
  - `nostr.api.NIP01#createTextNoteEvent(Identity, String)` and related Identity-based overloads — use instance-configured sender
  - `nostr.api.NIP61#createNutzapEvent(Amount, List<CashuProof>, URL, List<EventTag>, PublicKey, String)` — use slimmer overload and add amount/unit via `NIP60`
  - `nostr.event.tag.GenericTag(String, Integer)` compatibility ctor
  - `nostr.id.EntityFactory.Events#createGenericTag(PublicKey, IEvent, Integer)`

### Notes
- Integration tests require Docker (Testcontainers). CI runs a separate job for them on push; PRs use the no-Docker profile.
- See `MIGRATION.md` for complete guidance on deprecated API replacements.
