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
  publish, query, subscribe-and-collect, profile lookup, direct messages, and relay/key
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
- No persistent event store beyond an optional in-memory/embedded cache for subscriptions.
- No new NIP support. The module only surfaces what the SDK already implements; missing
  NIPs are implemented in `nostr-java-event`, not here.

## 3. Position in the module graph

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

## 4. Architecture

Four layers, each with one reason to change:

| Layer | Responsibility | Changes when |
| --- | --- | --- |
| **Transport** | MCP stdio / streamable HTTP wiring, JSON-RPC framing | The MCP spec changes |
| **Tool adapters** | Map MCP tool calls to domain commands; validate arguments; shape results | The tool surface changes |
| **Nostr services** | `PublishEventService`, `QueryEventService`, `ProfileService`, `DirectMessageService` — the domain operations | Nostr semantics change |
| **SDK facade** | Thin wrappers over `NostrRelayClient`, `Identity`, event factories | The SDK API changes |

Tool adapters depend on service *interfaces*, not on `NostrRelayClient`, so the tool layer
is testable with in-memory fakes and no relay.

### Key components

- `NostrMcpServer` — bootstraps the transport and registers tools.
- `NostrToolRegistry` — the single place where tools are declared, one class per tool
  implementing a common `NostrTool` interface (name, JSON schema, `execute`). New tools are
  added by adding a class, not by editing a switch (Open/Closed).
- `RelayPool` — resolves logical relay names to URLs, owns connection lifecycle and reuse.
- `IdentityVault` — resolves a logical identity name (`"default"`, `"alice"`) to an
  `Identity`. Private keys are loaded once at startup from configuration or an external
  signer and never leave the vault.
- `WriteGuard` — the policy object consulted before any relay write (see §7).

## 5. Tool surface (v1)

Names are namespaced `nostr_*` so they read clearly in an agent's tool list. Every tool
returns structured JSON plus a short human-readable summary.

| Tool | Purpose | Key arguments |
| --- | --- | --- |
| `nostr_publish_note` | Publish a kind-1 text note | `content`, `identity?`, `relays?`, `replyTo?`, `mentions?` |
| `nostr_publish_event` | Publish an arbitrary event (escape hatch) | `kind`, `content`, `tags?`, `identity?`, `relays?` |
| `nostr_query_events` | One-shot REQ, collect until EOSE | `authors?`, `kinds?`, `tags?`, `since?`, `until?`, `limit`, `relays?` |
| `nostr_fetch_thread` | Resolve a note and its replies (NIP-10) | `eventId`, `depth?` |
| `nostr_get_profile` | Fetch and decode kind-0 metadata | `pubkey` or `nip05`, `relays?` |
| `nostr_update_profile` | Publish kind-0 metadata | `fields`, `identity?` |
| `nostr_get_contacts` | Read a kind-3 contact list (NIP-02) | `pubkey?` |
| `nostr_send_direct_message` | Encrypted DM (NIP-17 preferred, NIP-04 legacy) | `recipient`, `content`, `identity?` |
| `nostr_read_direct_messages` | Decrypt DMs addressed to an identity | `identity?`, `since?`, `limit` |
| `nostr_list_identities` | Public keys the server can sign with | — |
| `nostr_list_relays` | Configured relays and connection state | — |
| `nostr_relay_info` | NIP-11 relay metadata | `relay` |

### Resources and prompts

- **Resources**: `nostr://identity/{name}` (public key, npub, configured relays) and
  `nostr://relay/{name}` (NIP-11 document), so an agent can read context without a tool
  call.
- **Prompts**: a small set of guided templates, e.g. `compose-note` and `catch-up-feed`,
  that teach the host how to sequence the tools.

### Argument conventions

- Public keys accept hex or `npub`; event ids accept hex or `note`/`nevent`. Decoding is
  centralised in one `NostrIdentifier` value type — the tools never parse bech32 inline.
- Timestamps accept ISO-8601 or relative expressions (`"24h"`, `"7d"`) and are normalised
  to Unix seconds at the boundary.
- `relays` defaults to the configured write/read set; explicit relays override it.

## 6. Configuration

Spring Boot properties under `nostr.mcp.*`, overridable by environment variables:

```yaml
nostr:
  mcp:
    transport: stdio            # stdio | http
    identities:
      default:
        private-key: ${NOSTR_PRIVATE_KEY}   # nsec or hex; or signer: nip46
    relays:
      read:  [wss://relay.damus.io, wss://nos.lol]
      write: [wss://relay.damus.io]
    limits:
      max-events-per-query: 500
      query-timeout: 15s
    write-policy: confirm        # deny | confirm | allow
```

Keys must never be logged. The startup banner prints public keys only.

## 7. Safety model

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
  model.

## 8. Error handling

Errors are returned as MCP tool errors with a stable machine-readable `code` and a message
the model can act on, never as stack traces. Categories mirror the SDK's exception
hierarchy: `RELAY_UNREACHABLE`, `RELAY_REJECTED`, `INVALID_ARGUMENT`, `IDENTITY_UNKNOWN`,
`WRITE_FORBIDDEN`, `TIMEOUT`. Partial success on multi-relay publish is a success with a
per-relay result list, not an error.

## 9. Testing strategy

- **Unit**: each tool adapter against fake services — argument validation, bech32 decoding,
  relative-time parsing, `WriteGuard` policy transitions.
- **Integration**: the full server over an in-process MCP client against a stub relay
  (reusing the existing Docker relay harness), asserting round trips for publish, query,
  and DM.
- **Contract**: every registered tool's JSON schema is validated, and a golden-file test
  pins the tool list so accidental surface changes are visible in review.
- Run with `mvn -q verify` from the repository root as usual.

## 10. Delivery plan

1. Module skeleton, POM, BOM entries, stdio transport, `nostr_list_identities` and
   `nostr_list_relays` — proves the wiring end to end.
2. Read path: `nostr_query_events`, `nostr_get_profile`, `nostr_relay_info`.
3. Write path behind `WriteGuard`: `nostr_publish_note`, `nostr_publish_event`,
   `nostr_update_profile`.
4. Social layer: threads, contacts, direct messages.
5. HTTP transport, resources, prompts, and documentation (a how-to for wiring the server
   into an MCP host).

## 11. Open questions

- Which Java MCP SDK: the official `io.modelcontextprotocol` SDK, or Spring AI's MCP server
  starter? The latter fits the existing Spring dependency, the former has fewer transitive
  dependencies.
- Should remote signing (NIP-46) be in v1, so the server never holds a private key at all?
- Does long-lived streaming (`subscribe` with push notifications) belong in v1, or is
  query-until-EOSE enough?
- Should the module ship a Dockerfile and `docker-compose.yml`, per the repo's convention
  for runnable components?
- Minimum viable NIP set for "natural language Nostr": is NIP-17 gift-wrapped DM support
  present in the SDK today, or must NIP-04 be the v1 fallback?

## Related documents

- [architecture.md](architecture.md) — existing module architecture and data flow
- [../howto/streaming-subscriptions.md](../howto/streaming-subscriptions.md) — the
  subscription mechanics the query tools build on
- [../operations/configuration.md](../operations/configuration.md) — configuration
  conventions this module follows
