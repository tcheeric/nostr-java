# nostr-java-mcp: Module Specification (Draft)

This document specifies a new module, `nostr-java-mcp`, that exposes the nostr-java SDK
as a [Model Context Protocol](https://modelcontextprotocol.io) server so that LLM agents
can read from and write to Nostr relays using natural language. It is a design
explanation: it states the goals, the boundaries, the tool surface, and the open
decisions. It is not yet an implementation guide.

## 1. Motivation

Today an application must speak Java to use nostr-java: build an event, sign it with an
`Identity`, and publish it through `NostrRelayClient`. An LLM agent cannot do that
directly. MCP is the emerging standard for giving an agent typed, discoverable
capabilities over stdio or HTTP. Wrapping the SDK in an MCP server means any MCP client
(Claude Desktop, Claude Code, IDE agents, custom hosts) gets Nostr access with no
Nostr-specific code, and the SDK gains a natural-language front door without polluting
the existing modules.

## 2. Goals and non-goals

### Goals

- Expose a small, deep tool surface covering the Nostr operations an agent actually needs:
  publish, query, long-lived subscriptions, profile lookup, direct messages, and relay/key
  introspection.
- Keep signing keys inside the server process. The agent names an identity; it never sees
  or supplies a private key.
- Support both stdio (local desktop agents) and streamable HTTP (remote/hosted) transports.
- Be strictly additive: no changes required in `core`, `event`, `identity`, or `client`.
- Make every destructive or public-facing action (anything that writes to a relay) opt-in
  and auditable.

### Non-goals

- No relay implementation. The module is a client only.
- No LLM inference. Natural-language understanding lives in the MCP host, not here.
- No persistent event store. Live subscriptions buffer in memory only (§5.1).
- No new NIP support beyond NIP-17, which this module needs and the SDK lacks (§5.2).
  Everything else missing is implemented in `nostr-java-event`, not here.
- No remote signing (NIP-46) in v1. Keys are held locally; see §6.

## 3. Resolved decisions

| Decision | Choice |
| --- | --- |
| MCP library | Official SDK, `io.modelcontextprotocol.sdk:mcp` (2.x), plus its stdio and HTTP transports. Not Spring AI's starter. |
| Signing | Local `nsec`/hex keys in an `IdentityVault`, unlocked at startup. NIP-46 deferred but designed for. |
| Subscriptions | Long-lived streaming, surfaced as MCP resources with change notifications. |
| Packaging | Executable jar plus `Dockerfile` and `docker-compose.yml`. |
| Direct messages | NIP-17 gift-wrapped DMs. Requires NIP-17 support to be added to `nostr-java-event` first. |

## 4. Position in the module graph

```
nostr-java-core  ──▶ nostr-java-event ──▶ nostr-java-identity ──▶ nostr-java-client
                                                                        │
                                                                        ▼
                                                                 nostr-java-mcp
```

`nostr-java-mcp` is a leaf: it depends on `client` (and transitively on the rest) and
nothing depends on it. This satisfies the Stable Dependencies Principle — the volatile,
protocol-adapting component depends on the stable ones, never the reverse.

It is packaged as an executable Spring Boot application **and** a library jar, so it can
be run standalone (`java -jar nostr-java-mcp.jar`) or embedded in a host application.

## 5. Architecture

Four layers, each with one reason to change:

| Layer | Responsibility | Changes when |
| --- | --- | --- |
| **Transport** | Official MCP SDK server (`McpServer`) over `StdioServerTransportProvider` or `HttpServletStreamableServerTransportProvider`; JSON-RPC framing | The MCP spec changes |
| **Tool adapters** | Map MCP tool calls to domain commands; validate arguments; shape results | The tool surface changes |
| **Nostr services** | `PublishEventService`, `QueryEventService`, `SubscriptionService`, `ProfileService`, `DirectMessageService` | Nostr semantics change |
| **SDK facade** | Thin wrappers over `NostrRelayClient`, `Identity`, event factories | The SDK API changes |

Tool adapters depend on service *interfaces*, not on `NostrRelayClient`, so the tool layer
is testable with in-memory fakes and no relay.

### MCP library choice

The official SDK (`io.modelcontextprotocol.sdk:mcp` 2.x) is used directly rather than Spring
AI's `spring-ai-starter-mcp-server`. The official SDK is the reference implementation of the
spec, tracks it first, and pulls in only Jackson plus a transport. Spring AI's starter would
drag a large AI-framework dependency tree into an SDK whose only concern is Nostr. The
module still uses Spring Boot for configuration and lifecycle, because `nostr-java-client`
already does, and registers the MCP server as a bean rather than via an autoconfiguration we
do not control.

### Key components

- `NostrMcpServer` — bootstraps the transport and registers tools.
- `NostrToolRegistry` — the single place where tools are declared, one class per tool
  implementing a common `NostrTool` interface (name, JSON schema, `execute`). New tools are
  added by adding a class, not by editing a switch (Open/Closed).
- `RelayPool` — resolves logical relay names to URLs, owns connection lifecycle and reuse.
- `IdentityVault` — resolves a logical identity name (`"default"`, `"alice"`) to an
  `Identity`. Private keys are loaded once at startup and never leave the vault (§7.1).
- `SubscriptionRegistry` — owns live subscriptions and their bounded buffers (§6.1).
- `WriteGuard` — the policy object consulted before any relay write (see §8).

## 6. Tool surface (v1)

Names are namespaced `nostr_*` so they read clearly in an agent's tool list. Every tool
returns structured JSON plus a short human-readable summary.

| Tool | Purpose | Key arguments |
| --- | --- | --- |
| `nostr_publish_note` | Publish a kind-1 text note | `content`, `identity?`, `relays?`, `replyTo?`, `mentions?` |
| `nostr_publish_event` | Publish an arbitrary event (escape hatch) | `kind`, `content`, `tags?`, `identity?`, `relays?` |
| `nostr_query_events` | One-shot REQ, collect until EOSE | `authors?`, `kinds?`, `tags?`, `since?`, `until?`, `limit`, `relays?` |
| `nostr_subscribe` | Open a long-lived subscription (§6.1) | `filters`, `relays?`, `name?`, `bufferSize?` |
| `nostr_read_subscription` | Drain buffered events from a subscription | `subscriptionId`, `max?` |
| `nostr_list_subscriptions` | Live subscriptions, filters, buffer depth, drop count | — |
| `nostr_unsubscribe` | Close a subscription and free its buffer | `subscriptionId` |
| `nostr_fetch_thread` | Resolve a note and its replies (NIP-10) | `eventId`, `depth?` |
| `nostr_get_profile` | Fetch and decode kind-0 metadata | `pubkey` or `nip05`, `relays?` |
| `nostr_update_profile` | Publish kind-0 metadata | `fields`, `identity?` |
| `nostr_get_contacts` | Read a kind-3 contact list (NIP-02) | `pubkey?` |
| `nostr_send_direct_message` | Gift-wrapped encrypted DM (NIP-17) | `recipient`, `content`, `identity?`, `subject?` |
| `nostr_read_direct_messages` | Unwrap and decrypt DMs addressed to an identity | `identity?`, `since?`, `limit` |
| `nostr_list_identities` | Public keys the server can sign with | — |
| `nostr_list_relays` | Configured relays and connection state | — |
| `nostr_relay_info` | NIP-11 relay metadata | `relay` |

### 6.1 Long-lived subscriptions

Query-until-EOSE is not enough: an agent asked to "watch my mentions" needs events that
arrive after the call returns. MCP has no server-push-into-a-tool-result mechanism, so
subscriptions are modelled as **stateful server resources**:

- `nostr_subscribe` opens a REQ through `NostrRelayClient` (which already supports
  long-lived subscriptions, see the streaming-subscriptions how-to) and returns a
  `subscriptionId`.
- Incoming events land in a **bounded ring buffer** per subscription (default 500 events).
  When it overflows the oldest events are dropped and a monotonic `droppedCount` is
  incremented, so the agent is told it missed data rather than silently losing it. Nothing
  is persisted; a restart drops all subscriptions.
- Each subscription is also exposed as an MCP resource, `nostr://subscription/{id}`, and the
  server emits `notifications/resources/updated` when new events arrive. A host that
  supports resource subscriptions gets push; one that does not can poll
  `nostr_read_subscription`.
- `nostr_read_subscription` **drains** what it returns, so repeated calls yield only new
  events. This is a command that also answers, which we accept deliberately here because
  an at-most-once read is what keeps an agent's context from filling with duplicates.
- Subscriptions have a configurable idle TTL (default 1 hour) after which they are closed
  and reaped, so an abandoned agent session cannot leak relay connections. Total live
  subscriptions are capped.
- Reconnection is delegated to `NostrRelayClient`'s retry logic; on reconnect the REQ is
  replayed with `since` set to the last received event's timestamp.

### 6.2 Direct messages and NIP-17

The SDK today ships NIP-04 (`EncryptedDirectMessage`) and the NIP-44 v2 primitives
(`EncryptedPayloads`, including `getConversationKey`), but **not** NIP-17. NIP-17 needs a
kind-14 chat rumor, sealed in a kind-13 with NIP-44, then gift-wrapped in a kind-1059 signed
by a fresh throwaway key with a randomised `created_at`.

NIP-04 leaks metadata (both pubkeys and the conversation are visible to every relay) and is
unsuitable as the DM story for a tool an agent drives on a user's behalf. So v1 ships NIP-17
only, and NIP-04 is not exposed at all.

The NIP-17 seal/gift-wrap logic is **not** implemented in this module. It belongs in
`nostr-java-event` (and the NIP-44 layer it builds on in `nostr-java-identity`), where every
consumer of the SDK benefits. This makes NIP-17 support a **hard prerequisite** of the DM
phase of the delivery plan, tracked as its own work item against those modules.


### 6.3 Resources and prompts

- **Resources**: `nostr://identity/{alias}` (public key, npub, configured relays),
  `nostr://relay/{name}` (NIP-11 document), and `nostr://subscription/{id}` (buffered
  events, updated by notification), so an agent can read context without a tool call.
- **Prompts**: a small set of guided templates, e.g. `compose-note`, `catch-up-feed`, and
  `watch-mentions`, that teach the host how to sequence the tools.

### 6.4 Argument conventions

- Public keys accept hex or `npub`; event ids accept hex or `note`/`nevent`. Decoding is
  centralised in one `NostrIdentifier` value type — the tools never parse bech32 inline.
- Timestamps accept ISO-8601 or relative expressions (`"24h"`, `"7d"`) and are normalised
  to Unix seconds at the boundary.
- `relays` defaults to the configured write/read set; explicit relays override it.

## 7. Configuration

Spring Boot properties under `nostr.mcp.*`, overridable by environment variables:

```yaml
nostr:
  mcp:
    transport: stdio            # stdio | http
    keystore:
      type: encrypted-file      # env | encrypted-file | os-keychain
      path: ${HOME}/.nostr-java/keys.jceks
    identities:
      default:
        alias: personal         # entry in the keystore; never the key itself
        relays:
          write: [wss://relay.damus.io]
    relays:
      read:  [wss://relay.damus.io, wss://nos.lol]
      write: [wss://relay.damus.io]
    subscriptions:
      max-live: 16
      buffer-size: 500
      idle-ttl: 1h
    limits:
      max-events-per-query: 500
      query-timeout: 15s
    write-policy: confirm        # deny | confirm | allow
```

Keys must never be logged. The startup banner prints public keys only.

### 7.1 Key storage

Keys are held locally, which makes *where* and *how* the central security decision of this
module. Three backends behind one `KeySource` interface, chosen by `keystore.type`, so a
later NIP-46 remote signer is a fourth implementation and not a redesign:

**A. `env` — environment variables (dev only).**
`NOSTR_MCP_IDENTITY_DEFAULT_NSEC` and friends. Zero setup, works in a container, matches
how the SDK's tests already pass keys. But the key sits in the process environment where
any child process, `/proc`, and most crash reporters can read it. Acceptable for a throwaway
test identity; the server logs a warning at startup when this backend is active.

**B. `encrypted-file` — a password-protected keystore (default, recommended).**
A JCEKS/PKCS#12 file at `~/.nostr-java/keys.jceks`, one entry per identity alias, the file
encrypted with a passphrase supplied at startup (prompt for stdio, `NOSTR_MCP_KEYSTORE_PASSPHRASE`
for headless). Decrypted keys live only inside `IdentityVault`, held as `byte[]`/`char[]`
that are zeroed on shutdown rather than as `String`, since a `String` cannot be wiped and
may be interned. File permissions are checked at startup and the server refuses to start on
a world-readable keystore. This is a familiar, portable, dependency-free mechanism and it
survives a container restart via a mounted volume.

**C. `os-keychain` — delegate to the platform.**
macOS Keychain, Windows DPAPI, or Secret Service / `libsecret` on Linux, reached through a
small adapter. Best available protection on a developer desktop and no passphrase to manage,
but it is platform-specific, awkward in containers, and needs a native dependency. Offered
as an option, not the default.

Regardless of backend:

- A `nostr_list_identities` result exposes aliases and public keys only. No tool, resource,
  or error message can ever return a private key; a unit test asserts this over the whole
  tool surface.
- `Identity` objects are never handed to the tool layer. Tools pass an alias to a signing
  service, which returns a signed event. Signing is the only capability that crosses the
  vault boundary.
- A **generate** path (`nostr-mcp keygen <alias>`) creates a key inside the keystore so a
  user never has to paste an `nsec` into a shell.
- Import accepts `nsec` or hex once, at rest it is always encrypted.

**Deferred: NIP-46 remote signing.** The strongest answer is for the server to hold no key
at all and delegate signing to a bunker (Amber, nsec.app). It is out of scope for v1 because
the SDK has no NIP-46 support, but `KeySource`/signing-service seam above exists precisely so
adding `type: nip46` later touches one class.

### 7.2 Packaging

Ships as an executable Spring Boot jar, a `Dockerfile` (distroless JRE 21 base, non-root
user), and a `docker-compose.yml` that runs the server in HTTP transport mode alongside the
existing test relay container, with the keystore mounted read-only as a volume and the
passphrase supplied as a secret. Per repo convention the compose file is verified with
`docker-compose build` in CI. The stdio transport is documented as a bare `java -jar`
invocation, since an MCP host launches the process itself and containerising stdio adds
little.

## 8. Safety model

Publishing to Nostr is public and irreversible: an event, once accepted by a relay, cannot
be reliably deleted (NIP-09 is advisory). The module therefore treats every write as a
guarded action.

- `write-policy: deny` — read-only server; write tools are not registered at all.
- `write-policy: confirm` (default) — write tools are registered but return a preview of
  the signed event plus a `confirmationToken`; the agent must call again with the token.
  This turns a hallucinated post into a no-op.
- `write-policy: allow` — writes proceed directly, for trusted automation.
- Rate limits per identity and per relay, enforced in `WriteGuard`.
- Every write is logged with event id, kind, identity pubkey, and target relays.
- DM decryption is opt-in per identity, since it exposes private correspondence to the
  model. NIP-17's gift wrapping means the relay cannot see the correspondents, but the MCP
  host can, so this stays an explicit per-identity grant.

## 9. Error handling

Errors are returned as MCP tool errors with a stable machine-readable `code` and a message
the model can act on, never as stack traces. Categories mirror the SDK's exception
hierarchy: `RELAY_UNREACHABLE`, `RELAY_REJECTED`, `INVALID_ARGUMENT`, `IDENTITY_UNKNOWN`,
`WRITE_FORBIDDEN`, `TIMEOUT`, `SUBSCRIPTION_UNKNOWN`, `SUBSCRIPTION_LIMIT_REACHED`,
`KEYSTORE_LOCKED`. Partial success on multi-relay publish is a success with a per-relay
result list, not an error.

## 10. Testing strategy

- **Unit**: each tool adapter against fake services — argument validation, bech32 decoding,
  relative-time parsing, `WriteGuard` policy transitions, ring-buffer overflow and
  `droppedCount`, subscription TTL reaping.
- **Security**: a test that walks every registered tool and resource and asserts no response
  or error message can contain a private key; keystore permission and passphrase-failure
  paths.
- **Integration**: the full server over an in-process MCP client against a stub relay
  (reusing the existing Docker relay harness), asserting round trips for publish, query,
  a live subscription receiving an event published mid-test, and a NIP-17 DM round trip.
- **Contract**: every registered tool's JSON schema is validated, and a golden-file test
  pins the tool list so accidental surface changes are visible in review.
- **Packaging**: `docker-compose build` runs in CI.
- Run with `mvn -q verify` from the repository root as usual.

## 11. Delivery plan

0. **Prerequisite, in `nostr-java-event`/`nostr-java-identity`**: NIP-17 support — kind-14
   rumor, kind-13 seal, kind-1059 gift wrap over the existing NIP-44 primitives. Tracked
   separately; blocks phase 5 only, so the rest can proceed in parallel.
1. Module skeleton, POM, BOM entry, official MCP SDK on stdio, `IdentityVault` with the
   `encrypted-file` keystore and `keygen`, plus `nostr_list_identities` and
   `nostr_list_relays` — proves the wiring end to end.
2. Read path: `nostr_query_events`, `nostr_get_profile`, `nostr_relay_info`.
3. Write path behind `WriteGuard`: `nostr_publish_note`, `nostr_publish_event`,
   `nostr_update_profile`.
4. Subscriptions: `SubscriptionRegistry`, the four subscription tools, resource
   notifications, TTL reaping.
5. Social layer: threads, contacts, NIP-17 direct messages.
6. HTTP transport, `Dockerfile` and `docker-compose.yml`, prompts, and documentation (a
   how-to for wiring the server into an MCP host).

## 12. Open questions

- Which keystore backend is the default on a fresh install: prompt-for-passphrase
  (`encrypted-file`) is safest but blocks unattended startup. Is a passphrase-less
  `os-keychain` default better for desktop users?
- Should `write-policy: confirm` tokens expire, and after how long?
- Does the HTTP transport need authentication of its own (bearer token) in v1, or is it
  documented as bind-to-localhost only?
- Does NIP-17 support land as a contribution to this repo, or is it already planned
  upstream in the BOM's event module?

## Related documents

- [architecture.md](architecture.md) — existing module architecture and data flow
- [../howto/streaming-subscriptions.md](../howto/streaming-subscriptions.md) — the
  subscription mechanics the query tools build on
- [../operations/configuration.md](../operations/configuration.md) — configuration
  conventions this module follows
