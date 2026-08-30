# nostr-java-mcp: Module Specification (Draft)

This document specifies a new module, `nostr-java-mcp`, that exposes the nostr-java SDK
as a [Model Context Protocol](https://modelcontextprotocol.io) server so that LLM agents
can read from and write to Nostr relays using natural language. It is a design
explanation: it states the goals, the boundaries, the tool surface, and the open
decisions. It is not yet an implementation guide.

**Baseline: nostr-java 2.2.0.** The module builds on `nostr-java-api` and its `NostrClient`
entry point. An earlier draft targeted 2.0.x and planned to build relay pooling, subscription
merging, and NIP-17 itself; the SDK now provides all three, so this revision consumes them
instead. §5 lists what that removes.

## 1. Motivation

Today an application must speak Java to use nostr-java: name an identity and some relays,
then drive `NostrClient` from `nostr-java-api`. An LLM agent cannot do that directly. MCP is the emerging standard for giving an agent typed, discoverable
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
- Offer a **process-per-identity** deployment for users who want hard isolation between
  accounts, without forcing it on users who want one server across all of theirs.
- Support both stdio (local desktop agents) and streamable HTTP (remote/hosted) transports.
- Be strictly additive: no changes required in `core`, `event`, `identity`, `client`, or `api`.
- Make every destructive or public-facing action (anything that writes to a relay) opt-in
  and auditable.

### Non-goals

- No relay implementation. The module is a client only.
- No LLM inference. Natural-language understanding lives in the MCP host, not here.
- No persistent event store. Live subscriptions buffer in memory only (§5.1).
- No NIP implementation in this module. NIP-17, the one gap that used to block it, shipped in
  2.1.0. One gap remains: kind-3 contact lists have no SDK type (§12), and that belongs in
  `nostr-java-event` where every consumer benefits, not here.
- No remote signing (NIP-46) in v1. Keys are held locally; see §6.

## 3. Resolved decisions

| Decision | Choice |
| --- | --- |
| Baseline | Built on nostr-java **2.2.0**, depending on `nostr-java-api`. |
| MCP library | Official SDK, `io.modelcontextprotocol.sdk:mcp` (2.x), plus its stdio and HTTP transports. Not Spring AI's starter. |
| Signing | Local `nsec`/hex keys in an `IdentityVault`, unlocked at startup. Default backend is the OS keychain (§12). NIP-46 deferred but designed for. |
| Subscriptions | Long-lived streaming on `RelayPool.subscribe`, surfaced as MCP resources with change notifications. |
| Packaging | Executable jar plus `Dockerfile` and `docker-compose.yml`. |
| Direct messages | NIP-17 gift-wrapped DMs, delegated to `nostr-java-api`'s `DirectMessagePublisher`. No SDK work required. |
| SDK prerequisites | One: a `ContactList` type over kind-3 in `nostr-java-event`, blocking the social phase only (§12). |
| Identity isolation | Optional **single-identity mode** binding one server process to one identity, deployed as one process per identity (§6.3.1). Not one thread per identity. |

## 4. Position in the module graph

```
nostr-java-core ──▶ nostr-java-event ──▶ nostr-java-identity ──▶ nostr-java-client ──▶ nostr-java-api
                                                                                            │
                                                                                            ▼
                                                                                     nostr-java-mcp
```

`nostr-java-mcp` is a leaf: it depends on `api` (and transitively on the rest) and nothing
depends on it. This satisfies the Stable Dependencies Principle — the volatile,
protocol-adapting component depends on the stable ones, never the reverse.

**It depends on `api`, not on `client`.** `nostr-java-api` exists precisely to own the
orchestration every application would otherwise repeat: fan-out publishing with per-relay
outcomes, de-duplicated subscriptions across relays, and NIP-17 delivery to each recipient's
own relays. An MCP server that reached past it to `NostrRelayClient` would rebuild all of
that, and get it subtly wrong in the ways the SDK already learned about.

It is packaged as an executable Spring Boot application **and** a library jar, so it can
be run standalone (`java -jar nostr-java-mcp.jar`) or embedded in a host application.

## 5. Architecture

Four layers, each with one reason to change:

| Layer | Responsibility | Changes when |
| --- | --- | --- |
| **Transport** | Official MCP SDK server (`McpServer`) over `StdioServerTransportProvider` or `HttpServletStreamableServerTransportProvider`; JSON-RPC framing | The MCP spec changes |
| **Tool adapters** | Map MCP tool calls to domain commands; validate arguments; shape results | The tool surface changes |
| **MCP services** | The behaviour MCP needs and the SDK does not provide: buffering a live subscription for later reads, write confirmation, identity administration | MCP-specific semantics change |
| **SDK** | `NostrClient` from `nostr-java-api`, used directly | The SDK API changes |

Tool adapters depend on service *interfaces*, so the tool layer is testable with in-memory
fakes and no relay.

### What this module does not build

The bottom layer is deliberately thin, because 2.2.0 already owns most of what an earlier
draft of this spec planned to write here:

| Concern | Provided by | So MCP does not |
| --- | --- | --- |
| Connecting to many relays, health, reconnection | `RelayPool` (`nostr-java-client`) | manage connections |
| Publishing to all relays, per-relay outcomes | `PublishResult`, `RelayPublishOutcome` | aggregate results |
| Total-failure signalling | `NoRelayAcceptedException` | invent an error convention |
| One subscription across relays, de-duplicated | `RelaySubscription`, `SubscriptionListener` | merge or de-duplicate streams |
| Re-subscribing a relay that dropped | `RelayPool` background reconnection | replay REQs |
| NIP-17 seal and gift wrap | `Nip17DirectMessageService`, `Nip59GiftWrapper` | implement the envelope |
| Delivering a DM to the recipient's relays | `DirectMessagePublisher` (`nostr-java-api`) | resolve or connect to them |
| Finding a recipient's kind-10050 relay list | `RelayListLookup` (`nostr-java-api`) | query for it |

What remains genuinely this module's work is everything MCP-shaped: the tool surface, the
keystore and vault, the write guard, identifier parsing, and the buffering that turns a
push-based subscription into something an agent can poll.

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
- `RelayDirectory` — maps the logical relay names an agent uses (`"read"`, `"write"`, or a
  configured alias) to the URIs handed to `NostrClient`. Naming only; the connections
  themselves belong to the SDK's `RelayPool`, and reusing that name here would give the
  codebase two different `RelayPool` types.
- `IdentityVault` — resolves a logical identity name (`"default"`, `"alice"`) to an
  `Identity`. Private keys are loaded once at startup and never leave the vault (§7.1).
- `SubscriptionBuffers` — holds the bounded ring buffer behind each live subscription so an
  agent can poll for what arrived while it was not looking (§6.1). The subscription itself is
  a `RelaySubscription` owned by the SDK.
- `WriteGuard` — the policy object consulted before any relay write (see §8).
- `McpDirectMessageService` — the tool-facing seam for DM tools. Named to avoid colliding
  with `nostr.encryption.DirectMessageService`, the SDK interface it ultimately calls
  through `NostrClient`.
- `RelayConnectionBroker` — shares one websocket per relay across bound processes (§12).
  Optional: a single server does not need it, and a deployment that declines it simply opens
  its own connections. See §6.3.1 for why it shares transport only.

### Limitations inherited from the SDK

Three documented SDK behaviours shape decisions in this module rather than being incidental:

- **One request in flight per relay.** `NostrRelayClient` serves a single request at a time
  and `RelayPool` queues per relay, so throughput to any one relay is bounded by round-trip
  latency. This is why `limits.max-events-per-query` and the subscription cap exist: an agent
  can otherwise queue enough work behind one slow relay to stall every other tool call.
- **A relay borrowed for a direct message is briefly shared.** Sending a DM connects to the
  recipient's relays, and while connected they take part in other operations. Under
  single-identity mode this is contained by the process boundary; in the multi-identity
  server it is worth knowing that a DM can widen the relay set another tool then publishes to.
- **De-duplication is windowed.** An event whose copies arrive far apart can be delivered
  twice. A subscription buffer must therefore tolerate a repeat rather than assume the SDK
  guarantees exactly-once over an unbounded period.

## 6. Tool surface (v1)

Names are namespaced `nostr_*` so they read clearly in an agent's tool list. Every tool
returns structured JSON plus a short human-readable summary.

The surface is **not fixed**: `write-policy` (§8) and single-identity mode (§6.3.1) both
unregister tools rather than rejecting calls at runtime. A tool an agent cannot see is a
tool it cannot misuse, and the tool list itself tells the agent what this server is for.

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
| `nostr_list_identities` | Aliases and public keys the server can sign with | — |
| `nostr_create_identity` | Generate a new keypair in the keystore (§6.3) | `alias`, `relays?`, `publishProfile?` |
| `nostr_import_identity` | Adopt an existing key held outside the model (§6.3) | `alias`, `source` |
| `nostr_rename_identity` | Change an alias, keeping the key | `alias`, `newAlias` |
| `nostr_set_default_identity` | Choose the identity used when `identity` is omitted | `alias` |
| `nostr_export_identity_backup` | Write an encrypted backup file to disk (§6.3) | `alias`, `path`, `passphrase?` |
| `nostr_remove_identity` | Forget a key, irreversibly (§6.3) | `alias`, `confirmationToken` |
| `nostr_list_relays` | Configured relays and connection state | — |
| `nostr_relay_info` | NIP-11 relay metadata | `relay` |

### 6.1 Long-lived subscriptions

Query-until-EOSE is not enough: an agent asked to "watch my mentions" needs events that
arrive after the call returns. MCP has no server-push-into-a-tool-result mechanism, so
subscriptions are modelled as **stateful server resources**:

- `nostr_subscribe` calls `NostrClient.subscribe(filters, listener)`, which registers the
  filter with every relay in the pool and returns a `RelaySubscription`. The tool returns a
  `subscriptionId` naming it.
- The listener writes incoming events into a **bounded ring buffer** per subscription
  (default 500 events). When it overflows the oldest events are dropped and a monotonic
  `droppedCount` is incremented, so the agent is told it missed data rather than silently
  losing it. Nothing is persisted; a restart drops all subscriptions.

  This buffer is not de-duplication. The SDK already delivers each event once however many
  relays carry it; the buffer exists because MCP has no way to push into a tool result, so
  events arriving between polls must be held somewhere.
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
- Reconnection is the SDK's job. `RelayPool` retries downed relays on its own schedule and
  re-subscribes them from the stored filter, so a relay that drops mid-stream rejoins without
  the MCP layer replaying anything. `SubscriptionListener.onRelayFailure` reports the drop,
  and `nostr_list_subscriptions` surfaces it so an agent can see a stream degrade.
- `SubscriptionListener.onEndOfStoredEvents` fires once, after every relay has replayed its
  backlog or a timeout expires. It is **asynchronous**: `RelayPool.subscribe` returns
  immediately, before any stored event or the signal itself has arrived (verified against a
  live relay: both counts are zero the instant it returns).

  So `nostr_subscribe` must not pretend history is ready. It returns the `subscriptionId`
  together with a `backlogDrained: false`, and `nostr_read_subscription` reports the flag so
  an agent can tell "nothing matched yet" from "the backlog is still replaying". A tool that
  blocked until EOSE would stall for the backlog timeout on any relay that never answers,
  which is exactly the failure the SDK's timeout exists to prevent.

### 6.2 Direct messages and NIP-17

**NIP-17 shipped in the SDK in 2.1.0, so this is no longer a prerequisite.** An earlier
draft of this spec treated it as blocking work against `nostr-java-event`; that work is done
and the module consumes it.

The layering is worth stating, because it decides what a DM tool actually calls:

- `Nip59GiftWrapper` (`nostr-java-identity`) implements the three-layer envelope: an unsigned
  kind-14 `Rumor`, sealed in a kind-13 signed by the real author, gift-wrapped in a kind-1059
  signed by a single-use key with a randomised `created_at`.
- `Nip17DirectMessageService` composes a `ChatMessage` into one wrap per participant and
  reads incoming wraps back. It plans delivery but never sends, because that module holds no
  transport.
- `DirectMessagePublisher` (`nostr-java-api`) performs that plan, delivering each wrap to its
  own recipient's relays and reporting a `RecipientDeliveryOutcome` per participant.

So `nostr_send_direct_message` is a thin adapter over `NostrClient.sendDirectMessage`, and
its result maps directly onto the per-recipient outcomes. Two behaviours the tool must
surface rather than hide:

- A recipient who has published no kind-10050 relay list comes back `UNREACHABLE`. NIP-17
  forbids sending to them, so the message genuinely did not go, and the agent must be able to
  tell the user which recipient missed out.
- Every conversation includes the sender, since NIP-17 requires a copy addressed to them.
  A one-recipient send therefore reports **two** outcomes, and the tool must not present that
  as a partial failure.

  This matters more than it first appears. A sender who has published no kind-10050 list of
  their own comes back `UNREACHABLE` for their *own* copy while the actual recipient is
  `DELIVERED` (observed against a live relay). Reported naively, "1 of 2 delivered" would tell
  the user their message failed when it arrived perfectly well. So the tool reports the
  recipients separately from the sender's archival copy, and surfaces a sender-side
  `UNREACHABLE` as advice — publish a relay list to keep your own sent messages — rather than
  as a delivery error.

NIP-04 leaks metadata (both pubkeys and the conversation are visible to every relay) and is
unsuitable as the DM story for a tool an agent drives on a user's behalf. It is deprecated in
the SDK and not exposed here at all.

### 6.3 Identity management

An agent that can only use pre-configured keys is half a tool. "Make me a throwaway account
for this project" and "stop using that key" are natural requests, so the keystore is
managed through tools rather than by hand-editing YAML. But an identity **is** the user's
Nostr account: creating one is cheap, losing one is unrecoverable, and exposing one is
irreversible. The lifecycle is therefore designed around what can and cannot be undone.

#### The safety asymmetry

| Operation | Reversible? | Treatment |
| --- | --- | --- |
| Create | Yes, discard the new key | Allowed freely |
| Rename, set default | Yes | Allowed freely |
| Import | Yes, remove it again | Allowed, but never through the model (see below) |
| Export backup | **No** — a key, once copied, cannot be uncopied | Guarded, file only |
| Remove | **No** — the key is gone and every event ever signed with it is orphaned | Two-step, guarded, backup-first |

`WriteGuard` already exists for relay writes (§8). Identity mutations reuse the same
two-step confirmation mechanism for the irreversible half of this table, so there is one
confirmation concept in the module, not two.

#### Creating

`nostr_create_identity` generates a keypair with `PrivateKey.generateRandomPrivKey()`,
stores it in the keystore under the alias, and returns **only** the alias, public key, and
npub. The private key never leaves `IdentityVault`, so the agent can create an account it
can use but cannot leak.

Optional `publishProfile` publishes a kind-0 for the new identity in the same call, since a
key with no metadata is invisible to every Nostr client. That publish still goes through
`WriteGuard` like any other write.

A fresh identity starts with the server's default relay set unless `relays` is given.

#### Importing

Importing means supplying an existing `nsec` or hex key, and this is where the design says
no to the obvious thing. **`nostr_import_identity` never accepts key material as an
argument.** If it did, the key would pass through the model's context window, be written to
the host's conversation log, and very likely be sent to a third-party inference API. That is
the single worst thing this module could do.

Instead `source` names *where the server should read the key from itself*:

- `source: "file:/path/to/key"` — the server reads and then offers to shred the file.
- `source: "env:NOSTR_IMPORT_KEY"` — read once from its own environment.
- `source: "prompt"` — on stdio, the server reads from its controlling terminal, invisible
  to the agent. On HTTP this returns `INPUT_REQUIRED` with a one-time local URL the human
  opens to paste the key.

The key is validated, its public key derived and reported back, and the source cleared. The
model orchestrates the import without ever seeing the secret. This is the same principle as
§7.1's rule that `Identity` objects never reach the tool layer, applied to the way in.

#### Removing

`nostr_remove_identity` is the only genuinely dangerous tool in the module: an npub with no
nsec is a dead account, and no relay, backup, or protocol can restore it.

- It is two-step. The first call returns the alias, public key, whether a backup exists,
  and a `confirmationToken`; nothing is deleted. The second call with the token deletes.
- It **refuses** if no backup has ever been exported for that alias, unless
  `acknowledgeNoBackup` is explicitly set. An agent that has not been told a key is
  disposable should not be able to destroy it on a hunch.
- Deletion zeroes the in-memory key, removes the keystore entry, and closes any
  subscription or pending write bound to that alias.
- The removal is logged with the public key, so the audit trail outlives the key.
- Under `identity-policy: deny` the tool is not registered at all, and `write-policy: deny`
  implies that (§12): a read-only server cannot mutate the keystore either.

#### Exporting

`nostr_export_identity_backup` writes a passphrase-encrypted file to a path on the server's
filesystem and returns **the path only, never the contents**. This is what makes removal
safe without ever putting key material in the agent's context. A passphrase is required; if
omitted the server prompts for one by the same `source: "prompt"` mechanism as import.

#### Interface

One interface, mirroring `KeySource` from §7.1 so a future NIP-46 backend can decline
mutations cleanly rather than pretending to support them:

```java
public interface IdentityStore {
  List<IdentitySummary> list();                 // aliases and public keys only
  IdentitySummary create(String alias);
  IdentitySummary importFrom(KeyLocation source, String alias);
  void rename(String alias, String newAlias);
  Path exportBackup(String alias, Path destination, char[] passphrase);
  void remove(String alias);
  boolean supportsMutation();                   // false for a remote signer
}
```

`IdentitySummary` is a value type carrying alias, public key, and npub. It has no field that
could hold a private key, so "no tool can return a secret" is a property of the type rather
than a rule someone has to remember.

#### Multi-identity behaviour

- Every signing tool takes an optional `identity` alias; omitting it uses the configured
  default. An **ambiguity guard** applies: when more than one identity exists and no default
  is set, signing tools fail with `IDENTITY_AMBIGUOUS` and list the aliases rather than
  guessing. Posting from the wrong account is a public, irreversible mistake.
- Aliases are human-meaningful (`personal`, `project-bot`) and validated against
  `[a-z0-9-]{1,32}`, since they appear in resource URIs.
- Per-identity relay sets are supported, because a throwaway identity often belongs on
  different relays than a main one.

#### 6.3.1 Single-identity mode and process isolation

The multi-identity server above is the general case, but it has a real weakness: an agent
holding several aliases can post as the wrong one. The `IDENTITY_AMBIGUOUS` guard reduces
that risk without removing it, because once a default exists the agent can still name any
alias it likes.

**Single-identity mode** removes the risk instead of guarding it. Setting
`nostr.mcp.identity: <alias>` binds the whole process to exactly one identity:

- The `identity` argument disappears from every signing tool's schema. There is nothing to
  name, so nothing to name wrongly, and `IDENTITY_AMBIGUOUS` cannot occur.
- `nostr_list_identities` returns the single bound identity.
- The lifecycle tools (create, import, rename, export, remove) are **not registered**. A
  bound server operates a key; it does not administer the keystore.
- Only that alias's entry is unlocked. The other keystore entries are never decrypted, so
  they are absent from the process's heap entirely.

##### Why a process, not a thread

The isolation people usually want here is "identity A's key cannot reach identity B's
operation", and it is worth being precise about what does and does not deliver that.

**Threads do not.** Threads in a JVM share one heap, so every thread can read every other
thread's `Identity` object. A thread-per-identity design would give the *appearance* of
separation with none of the substance. It is also the wrong concurrency shape: relay work is
I/O-bound and the SDK already fans out across relays on virtual threads inside `RelayPool`,
so a dedicated platform thread per identity would idle almost always while adding a second,
coarser queue on top of the pool's own per-relay serialisation. And it would not remove the
lifecycle question, since a thread neither generates a keypair nor forgets one.

**Processes do.** A separate process has its own address space, its own file handles, and
its own OS-level permissions. A compromise or a bug in the process signing for `project-bot`
cannot reach the `personal` key, because that key was never decrypted in that process.

**Sessions are the middle ground.** The streamable HTTP transport has a session concept, so
a hosted deployment may bind each session to one identity and filter the tool surface
accordingly. This is a logical boundary within one heap: it guards against agent confusion,
not against a compromised process. It is offered for the hosted case, where one process per
identity per user does not scale, and its weaker guarantee is stated plainly rather than
implied.

##### The deployment pattern

Single-identity mode fits how MCP hosts already work: a host config lists one entry per
server, so identities become entries.

```json
{
  "mcpServers": {
    "nostr-personal":    { "command": "java", "args": ["-jar", "nostr-java-mcp.jar", "--nostr.mcp.identity=personal"] },
    "nostr-project-bot": { "command": "java", "args": ["-jar", "nostr-java-mcp.jar", "--nostr.mcp.identity=project-bot"] }
  }
}
```

Each process reads only its own key. The agent sees two clearly-named tool groups and cannot
confuse them, because the tools themselves are distinct.

The costs are real and worth stating: N processes, N relay connection pools, and **no
cross-identity operation** — you cannot ask "which of my accounts was mentioned this week"
from a bound server. That query needs the multi-identity server, which is exactly why both
modes exist rather than one replacing the other.

The websocket cost is mitigated rather than accepted: bound processes share relay connections
through a `RelayConnectionBroker` (§12), so N identities do not mean N connections to the same
relay. The broker shares transport only. Sharing a `RelayPool` would share subscriptions and
per-relay state too, reintroducing exactly the cross-identity reach that running separate
processes exists to prevent.

There is a tension here worth naming rather than glossing. Bound processes are separate
address spaces, which is the whole point, so a shared broker cannot be an object they all
reference; it is a separate process they all talk to. That makes it a component with its own
lifecycle, its own failure mode (every identity loses relay access when it dies), and its own
trust question (it sees every bound identity's traffic, though never their keys). It is
therefore **opt-in and not the default**: a handful of identities should just open a handful of
connections, and the broker earns its complexity only where relay-imposed connection limits
actually bite.

`RelayConnection` (`nostr-java-client`) is the seam it implements, so adopting it changes
configuration rather than code. **Verified by building it**: two independent `RelayPool`
instances, each publishing successfully to a real relay, shared a single websocket, with the
only change being the `RelayConnectionFactory` they were handed.

##### Bootstrapping

Binding a process to an alias presupposes the alias exists, so single-identity mode does not
remove the need to create and remove keys, it relocates it. Two paths, neither of which
requires a bound server to administer anything:

- **A multi-identity server** run deliberately for administration, with the lifecycle tools
  from §6.3 available.
- **A CLI** on the same jar: `java -jar nostr-java-mcp.jar keygen <alias>`,
  `import <alias>`, `list`, `remove <alias>`. This is the better default for the
  process-per-identity deployment, since it keeps key administration out of every agent's
  reach entirely and puts it in the hands of the human who set the servers up.

Both drive the same `IdentityStore` (§6.3), so there is one implementation of the lifecycle
and two front doors to it.

### 6.4 Resources and prompts

- **Resources**: `nostr://identity/{alias}` (public key, npub, configured relays),
  `nostr://relay/{name}` (NIP-11 document), and `nostr://subscription/{id}` (buffered
  events, updated by notification), so an agent can read context without a tool call.
- **Prompts**: a small set of guided templates, e.g. `compose-note`, `catch-up-feed`, and
  `watch-mentions`, that teach the host how to sequence the tools.

### 6.5 Argument conventions

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
    bind-address: 127.0.0.1     # http only; the transport has no auth of its own (§12)
    identity: personal          # optional: bind to one identity (§6.3.1)
    keystore:
      type: os-keychain         # env | encrypted-file | os-keychain
      path: ${HOME}/.nostr-java/keys.jceks   # used by encrypted-file only
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
    identity-policy: deny        # deny | confirm | allow; keystore mutation, separate from writes
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

**B. `encrypted-file` — a password-protected keystore (portable fallback).**
A JCEKS/PKCS#12 file at `~/.nostr-java/keys.jceks`, one entry per identity alias, the file
encrypted with a passphrase supplied at startup (prompt for stdio, `NOSTR_MCP_KEYSTORE_PASSPHRASE`
for headless). Decrypted keys live only inside `IdentityVault`, held as `byte[]`/`char[]`
that are zeroed on shutdown rather than as `String`, since a `String` cannot be wiped and
may be interned. File permissions are checked at startup and the server refuses to start on
a world-readable keystore. This is a familiar, portable, dependency-free mechanism and it
survives a container restart via a mounted volume.

**C. `os-keychain` — delegate to the platform (default).**
macOS Keychain, Windows DPAPI, or Secret Service / `libsecret` on Linux, reached through a
small adapter. Best available protection on a developer desktop, and no passphrase to manage,
so it does not block unattended startup the way a prompt does. It is platform-specific and
awkward in containers, which is why `encrypted-file` remains the documented choice there and
is selected explicitly in the compose file rather than inherited.

Regardless of backend:

- A `nostr_list_identities` result exposes aliases and public keys only. No tool, resource,
  or error message can ever return a private key; a unit test asserts this over the whole
  tool surface.
- `Identity` objects are never handed to the tool layer. Tools pass an alias to a signing
  service, which returns a signed event. Signing is the only capability that crosses the
  vault boundary.
- A **generate** path (`nostr_create_identity`, §6.3) creates a key inside the keystore so a
  user never has to paste an `nsec` into a shell or into a chat window.
- Import reads the key from a location the server resolves itself; at rest it is always
  encrypted (§6.3).

**Deferred: NIP-46 remote signing.** The strongest answer is for the server to hold no key
at all and delegate signing to a bunker (Amber, nsec.app). It is out of scope for v1 because
the SDK has no NIP-46 support, but `KeySource`/signing-service seam above exists precisely so
adding `type: nip46` later touches one class. A remote signer reports
`supportsMutation() == false`, so the identity lifecycle tools degrade to read-only rather
than failing confusingly.

### 7.2 Packaging

Ships as an executable Spring Boot jar that is **both** the MCP server and the key-admin
CLI (§6.3.1), a `Dockerfile` (distroless JRE 21 base, non-root user), and a
`docker-compose.yml` that runs the server in HTTP transport mode alongside the existing test
relay container. Its port mapping is bound to the host loopback (`127.0.0.1:PORT:PORT`, not
`PORT:PORT`), since a container publishing a port reaches every interface by default and the
transport carries no credentials of its own (§8, §12). The keystore is mounted read-only as a
volume and the passphrase supplied as a secret. The compose file sets `keystore.type: encrypted-file` explicitly, because the
`os-keychain` default (§12) has nothing to talk to inside a container. The compose file also demonstrates the bound-container pattern: one service per
identity, each with `--nostr.mcp.identity` set and only its own key readable. Per repo
convention the compose file is verified with `docker-compose build` in CI. The stdio
transport is documented as a bare `java -jar` invocation, since an MCP host launches the
process itself and containerising stdio adds little.

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
- Identity removal and backup export are guarded by the same two-step confirmation, and
  neither is registered under `identity-policy: deny` or in single-identity mode (§6.3,
  §6.3.1).
- `identity-policy` governs keystore mutation separately from `write-policy` (§12), so a
  deployment can let an agent post without letting it create or destroy keys. It defaults to
  the more restrictive of the two, and `write-policy: deny` implies no mutation, so the safe
  combination needs no configuration.
- Key isolation between identities is a **process** boundary, not a thread or a check
  (§6.3.1). Deployments that need it run one bound server per identity.
- No tool accepts private key material as an argument, and no tool returns it (§6.3, §7.1).
- Every write is logged with event id, kind, identity pubkey, and target relays. Every
  keystore mutation is logged with the alias and public key.
- DM decryption is opt-in per identity, since it exposes private correspondence to the
  model. NIP-17's gift wrapping means the relay cannot see the correspondents, but the MCP
  host can, so this stays an explicit per-identity grant.
- **The HTTP transport has no authentication of its own** (§12) and binds to `127.0.0.1` by
  default. Anything that can reach it can publish as every identity the server holds, so
  exposing it beyond the loopback interface requires a reverse proxy with real credentials in
  front. The server logs a warning at startup when `bind-address` is not a loopback address,
  for the same reason the `env` keystore backend warns: a weaker choice should be noisy rather
  than silent.

## 9. Error handling

Errors are returned as MCP tool errors with a stable machine-readable `code` and a message
the model can act on, never as stack traces. Categories mirror the SDK's exception
hierarchy: `RELAY_UNREACHABLE`, `RELAY_REJECTED`, `INVALID_ARGUMENT`, `IDENTITY_UNKNOWN`,
`WRITE_FORBIDDEN`, `TIMEOUT`, `SUBSCRIPTION_UNKNOWN`, `SUBSCRIPTION_LIMIT_REACHED`,
`KEYSTORE_LOCKED`, `IDENTITY_AMBIGUOUS`, `ALIAS_IN_USE`, `NO_BACKUP_EXISTS`,
`MUTATION_UNSUPPORTED`, `INPUT_REQUIRED`.

Publishing maps onto the SDK's own distinction rather than inventing one:

| SDK outcome | MCP result |
| --- | --- |
| `PublishResult` with at least one `ACCEPTED` | Success, carrying the per-relay list |
| `NoRelayAcceptedException` | Error `RELAY_REJECTED`, with the attached `PublishResult` rendered so the agent sees each relay's reason |

Partial success is therefore a success with a per-relay result list, and the only publish
failure is the one where the event reached nobody. Reporting a partial success as an error
would push agents to retry writes that already landed, which on a public and irreversible
medium is worse than the original problem.

## 10. Testing strategy

- **Unit**: each tool adapter against fake services — argument validation, bech32 decoding,
  relative-time parsing, `WriteGuard` policy transitions, ring-buffer overflow and
  `droppedCount`, subscription TTL reaping.
- **Security**: a test that walks every registered tool and resource and asserts no response
  or error message can contain a private key, and that no tool's input schema accepts one;
  keystore permission and passphrase-failure paths; identity removal refused without a
  backup; `IDENTITY_AMBIGUOUS` raised rather than a key guessed.
- **Integration**: the full server over an in-process MCP client against a real relay,
  reusing the harness from `NostrClientRoundTripIT` (Testcontainers, held until the relay has
  proved it can store an event), asserting round trips for publish, query, a live
  subscription receiving an event published mid-test, and a NIP-17 DM round trip. Follow that
  test's readiness discipline: a relay that accepts connections is not necessarily one that
  answers, and mistaking the two produces failures that look like client bugs.
- **Contract**: every registered tool's JSON schema is validated, and a golden-file test
  pins the tool list so accidental surface changes are visible in review. Separate golden
  files per mode (multi-identity, single-identity, `write-policy: deny`) so tool
  unregistration is asserted rather than assumed.
- **Isolation**: a single-identity server started with `identity: personal` exposes no
  lifecycle tools, accepts no `identity` argument, and never decrypts another alias's
  keystore entry.
- **Spec conformance**: the SDK behaviours this document depends on are asserted against a
  live relay in `McpSpecAssumptionsIT` (`nostr-java-api`), so a change in the SDK that
  invalidates a design decision here fails a build rather than being discovered during
  implementation. It covers: publish returning per-relay outcomes; total failure throwing with
  the result attached; `subscribe` returning before the backlog drains; an event published
  mid-subscription reaching the listener; `EOSE` firing exactly once; a kind-10050 lookup
  resolving; a one-recipient DM reporting two outcomes; a recipient without a relay list
  reported `UNREACHABLE`; and `publishAs` signing as the named identity.
- **Packaging**: `docker-compose build` runs in CI.
- Run with `mvn -q verify` from the repository root as usual.

## 11. Delivery plan

1. Module skeleton, POM, BOM entry, official MCP SDK on stdio, `IdentityVault` and
   `IdentityStore` with the `encrypted-file` keystore, the **CLI** lifecycle commands
   (§6.3.1), single-identity mode, and `nostr_list_relays` — proves the wiring end to end
   and makes the server usable from a cold start with no hand-written config.
2. Read path: `nostr_query_events`, `nostr_get_profile`, `nostr_relay_info`.
3. Write path behind `WriteGuard`: `nostr_publish_note`, `nostr_publish_event`,
   `nostr_update_profile`. Identity lifecycle **tools** (§6.3) land here too, for the
   multi-identity administration case.
4. Subscriptions: `SubscriptionBuffers`, the four subscription tools, resource
   notifications, TTL reaping.
5. Social layer: threads, contacts, and NIP-17 direct messages over
   `NostrClient.sendDirectMessage` / `readDirectMessage`. Direct messages need no SDK work,
   since NIP-17 landed in 2.1.0 and delivery in 2.2.0. Contacts do: a `ContactList` value type
   over kind-3 belongs in `nostr-java-event` (§12) and is this module's only SDK prerequisite.
6. HTTP transport with per-session identity binding, `Dockerfile` and `docker-compose.yml`
   (including a profile showing one bound container per identity), prompts, and
   documentation (a how-to for wiring the server into an MCP host, covering both modes).

## 12. Resolved decisions (round two)

The questions this section previously listed have been answered. They are kept as decisions
with their reasoning, because the reasoning is what a reader needs when the trade-off resurfaces.

### Keystore default: `os-keychain`

The default on a fresh install is the platform keychain, not the encrypted file. It needs no
passphrase, so it does not block unattended startup, and on a desktop it is the strongest
protection available. `encrypted-file` remains the portable fallback and the sensible choice in
a container, where there is no keychain to talk to; §7.2's compose file therefore selects it
explicitly rather than inheriting the default.

### Identity lifecycle tools: keep them

The tools stay, alongside the CLI. An agent asked to "make me a throwaway account for this
project" should be able to do it, and the design already removes the reason to withhold the
capability: no tool accepts or returns key material, creation is reversible by discarding the
key, and the two genuinely irreversible operations (export, remove) are guarded by two-step
confirmation. Withholding the tools would not add safety, only friction, since the CLI can do
the same things with less oversight from the model's own audit trail.

Single-identity mode still unregisters them (§6.3.1): a process bound to one key operates that
key and does not administer a keystore.

### Documented default: single-identity mode

The how-to leads with one bound process per identity, and presents the multi-identity server as
the advanced case. It is the safer default, it matches how MCP hosts are configured anyway, and
the cross-identity query that justifies the multi-identity server is a genuinely advanced need.

### Bound processes share relay connections

Bound processes can share a `RelayConnectionBroker` rather than each opening its own websocket
to the same relay. Without it, N identities means N connections per relay, which relays
penalise and which scales badly exactly where this deployment is recommended.

The broker is a connection-level concern only. It must not become a shared `RelayPool`: the
pool holds subscriptions and per-relay state, and sharing that across bound processes would
reintroduce the cross-identity leakage that process isolation exists to prevent (§6.3.1). What
is shared is the transport beneath it, implementing `RelayConnection`.

It is **opt-in, not the default**, because bound processes are separate address spaces: a
shared broker is another process, not an object, and it brings its own lifecycle, a single
point of failure for every identity's relay access, and a component that sees all their traffic.
A handful of identities should open a handful of connections. The broker earns that complexity
only where a relay's connection limits actually bite.

### Confirmation tokens do not expire

A `write-policy: confirm` token stays valid until it is used or the server restarts. Expiry
would add a failure mode without adding safety: the token's purpose is to make a hallucinated
write a no-op by requiring a second, deliberate call, and that property does not decay with
time. An agent that comes back to a token an hour later is still confirming the same event it
was shown.

### Identity mutation gets its own policy switch

`identity-policy` is separate from `write-policy`, so an agent can be allowed to post while
being unable to touch the keystore. The two capabilities have genuinely different risk profiles:
publishing is public and irreversible but bounded to one event, while removing a key destroys
every future use of an account. Collapsing them would force a deployment to choose between a
useful agent and a protected keystore.

`identity-policy` defaults to the more restrictive of the two, and `write-policy: deny` still
implies no keystore mutation, so the safe combination remains the default without configuration.

### No auto-created identity on first run

A server started with an empty keystore refuses to start and says how to create an identity,
rather than generating a `default` silently. A key created without the user knowing is a key
they have no backup of, and the first they would learn of it is when something published under
it. The CLI (§6.3.1) makes the explicit path a single command.

### HTTP transport authentication: not in v1

The HTTP transport is documented as bind-to-localhost only, with no bearer token of its own in
v1. Adding half an auth story is worse than deferring it deliberately: a token in a config file
protects little, and the deployments that genuinely need remote access need a reverse proxy
with real credentials in front of them regardless.

The constraint is stated where it can be acted on rather than only here: `bind-address`
defaults to `127.0.0.1` in the configuration (§7), the safety model lists an exposed transport
as a threat and warns at startup on a non-loopback bind (§8), and the compose file publishes
its port to the host loopback only (§7.2), since a container reaches every interface by
default. A deferral that lives in one section of a design document is not a deferral, it is a
trap.

### kind-3 contact lists need a new SDK type

**Verified against 2.2.0:** no `ContactList` type exists. `Kinds.CONTACT_LIST = 3` is defined,
but nothing models the event, so `nostr_get_contacts` has nothing to call.

The type belongs in `nostr-java-event`, not here, for the same reason NIP-17 did: every
consumer benefits, and an MCP module parsing `p` tags inline would be the only place in the
codebase that knows how a contact list is shaped. `DirectMessageRelayList` is the pattern to
follow, being a value type over a tag-carrying replaceable event with `from(GenericEvent)` and
`toEvent()`.

**Verified by prototyping it**: a `ContactList` written to that pattern, reading `p` tags and
rendering them back, round-tripped through a real relay via `NostrClient` — published, matched
by an author-and-kind filter, and recovered with both contacts intact — and its kind guard
rejected a kind-1 event. The prerequisite is genuinely small: one value type, no new
infrastructure.

This makes a small `ContactList` addition to `nostr-java-event` a **prerequisite of the social
phase** (§11 phase 5), and the only SDK work this module now requires.

## Tickets

The delivery plan above is broken into twelve tracer-bullet tickets under
`.scratch/nostr-java-mcp/issues/`, each declaring what blocks it. Two can start immediately:
the `ContactList` prerequisite in `nostr-java-event`, and the module skeleton with its stdio
transport.

## Related documents

- [architecture.md](architecture.md) — existing module architecture and data flow
- [../howto/multi-relay-publishing.md](../howto/multi-relay-publishing.md) — the
  `NostrClient` surface this module adapts, including per-relay publish outcomes and
  de-duplicated subscriptions
- [../reference/nostr-java-api.md](../reference/nostr-java-api.md) — signatures for
  `NostrClient`, `RelayPool`, `PublishResult`, and the known limitations they carry
- [../howto/streaming-subscriptions.md](../howto/streaming-subscriptions.md) — single-relay
  subscription mechanics
- [../operations/configuration.md](../operations/configuration.md) — configuration
  conventions this module follows
