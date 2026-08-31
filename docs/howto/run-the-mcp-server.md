# Run the Nostr MCP server

This guide shows how to run `nostr-java-mcp` so an LLM agent can use Nostr, how to give it a
signing key, and how to choose how much freedom it has. You need Java 21 and an MCP host such
as Claude Desktop or an IDE agent.

## Build the jar

The server ships as one self-contained jar, which is both the MCP server and the command-line
tool for managing its keys:

```bash
git clone https://github.com/tcheeric/nostr-java.git
cd nostr-java
mvn -pl nostr-java-mcp -am package -DskipTests
```

The jar lands at `nostr-java-mcp/target/nostr-java-mcp-<version>-runnable.jar`. The
`-runnable` suffix matters: the plain `nostr-java-mcp-<version>.jar` beside it holds only this
module's classes and will not start on its own. The examples below shorten the path to
`nostr-java-mcp.jar`; substitute your real one, or copy it somewhere convenient:

```bash
cp nostr-java-mcp/target/nostr-java-mcp-*-runnable.jar ~/nostr-java-mcp.jar
```

## Create a key first

The server signs as identities held in its own keystore. Create one from the command line
rather than through the agent, so key administration stays with you:

```bash
java -jar nostr-java-mcp.jar keygen personal
```

This prints the new public key and stores the private key in your OS keychain. The private key
is never printed and never reaches the agent.

Other commands: `list`, `import <alias>` (reads the key from standard input), and
`remove <alias>`.

## Wire it into your MCP host

An MCP host launches the server itself and speaks to it over standard input and output. Bind
each server to one identity, which is both the safer arrangement and the one that fits how
hosts are configured anyway:

```json
{
  "mcpServers": {
    "nostr-personal": {
      "command": "java",
      "args": ["-jar", "/path/to/nostr-java-mcp.jar", "-Dnostr.mcp.identity=personal"]
    }
  }
}
```

Binding does more than express a preference. Only that one key is decrypted, so another
identity's key is absent from the process rather than merely out of policy, and the `identity`
argument disappears from every signing tool: there is nothing to name, so nothing to name
wrongly. A bound server also registers no keystore-mutating tools, and refuses to start if its
identity does not exist.

Add one entry per identity, and the agent sees two clearly-named tool groups it cannot confuse:

```json
{
  "mcpServers": {
    "nostr-personal":    { "command": "java", "args": ["-jar", "nostr-java-mcp.jar", "-Dnostr.mcp.identity=personal"] },
    "nostr-project-bot": { "command": "java", "args": ["-jar", "nostr-java-mcp.jar", "-Dnostr.mcp.identity=project-bot"] }
  }
}
```

### The unbound server

Omitting `identity` gives one server holding every key. You need this to administer the keystore
through tools, and for questions no bound server can answer, such as "which of my accounts was
mentioned this week". The cost is that the agent chooses which identity signs, so the
wrong-account risk is guarded rather than removed: where several identities exist and none is
the default, signing fails rather than guessing.

The server starts with `write-policy: confirm` either way, so the agent must confirm before
anything is published.

## What the agent is taught

The server ships three guided prompts, which hosts surface as slash-commands or similar:

| Prompt | What it teaches |
| --- | --- |
| `compose-note` | Draft, show the user, then publish with the confirmation token |
| `catch-up-feed` | Read the follow list first, then query those authors |
| `watch-mentions` | Subscribe, and distinguish "still replaying" from "nothing matched" |

They exist because a tool surface with no guidance makes a model explore by trial and error,
and on a public, permanent medium the mistakes are visible to everyone.

It can also read `nostr://identity/{alias}` and `nostr://relay/{name}` as resources, so a host
can put the server's own configuration into context without spending a tool call on it.

## Choosing how much freedom the agent has

Publishing to Nostr is public and cannot be reliably undone, so writes are guarded:

| Setting | Effect |
| --- | --- |
| `-Dnostr.mcp.write-policy=deny` | No write tool is registered. A read-only server. |
| `-Dnostr.mcp.write-policy=confirm` | Default. The agent previews, then publishes with a token. |
| `-Dnostr.mcp.write-policy=allow` | Writes proceed directly. For trusted automation. |

`-Dnostr.mcp.identity-policy` governs the keystore separately and never grants more than
`write-policy` does, so a read-only server also cannot create or destroy keys.

## Reading private messages

Reading direct messages brings private correspondence into the model's context, and therefore
into your host's conversation log and probably a third-party inference API. It is off by
default and enabled per identity:

```bash
java -jar nostr-java-mcp.jar -Dnostr.mcp.dm.decrypt-for=personal
```

Sending messages needs no such flag, because sending discloses nothing you did not write.

## Run it over HTTP

For a hosted deployment where the host does not launch the process:

```bash
java -jar nostr-java-mcp.jar -Dnostr.mcp.transport=http -Dnostr.mcp.port=8080
```

> **The HTTP transport has no authentication of its own.** Anything that can reach it can
> publish as every identity the server holds and read every message it is allowed to decrypt.
> It binds `127.0.0.1` by default for that reason, and logs a warning when configured
> otherwise. If you need to reach it from another machine, put a reverse proxy with real
> credentials in front of it. Do not expose the port directly.

## Run it in a container

The image is built from `nostr-java-mcp/Dockerfile` and runs the HTTP transport as a non-root
user on a distroless base:

```bash
docker build -f nostr-java-mcp/Dockerfile -t nostr-java-mcp .
```

`nostr-java-mcp/docker-compose.yml` runs it alongside a local relay. Create a keystore first
and put it in `nostr-java-mcp/keys/`, then:

```bash
export NOSTR_MCP_KEYSTORE_PASSPHRASE='your passphrase'
docker compose -f nostr-java-mcp/docker-compose.yml up mcp
```

The keystore is mounted read-only, and `keystore.type` is `encrypted-file` because there is no
OS keychain inside a container.

> Note the `127.0.0.1:` prefix on every published port. Without it Docker publishes to **every**
> interface, which bypasses the server's own loopback default and exposes an unauthenticated
> MCP endpoint to your network. Keep it.

For one container per identity, each unlocking only its own key:

```bash
docker compose -f nostr-java-mcp/docker-compose.yml --profile bound up
```

## Configuring from the environment

Every setting can be given as an environment variable instead of a system property, which is
how the container is configured. Uppercase the name and replace dots and hyphens with
underscores:

| Setting | Environment variable |
| --- | --- |
| `write-policy` | `NOSTR_MCP_WRITE_POLICY` |
| `bind-address` | `NOSTR_MCP_BIND_ADDRESS` |
| `relays.read` | `NOSTR_MCP_RELAYS_READ` |
| `limits.max-events-per-query` | `NOSTR_MCP_LIMITS_MAX_EVENTS_PER_QUERY` |

The encrypted-file keystore reads its passphrase from `NOSTR_MCP_KEYSTORE_PASSPHRASE`.

## Configuration reference

All settings are `nostr.mcp.*` system properties, or the same name in the environment.

| Setting | Default | Purpose |
| --- | --- | --- |
| `transport` | `stdio` | `stdio` or `http` |
| `bind-address` | `127.0.0.1` | HTTP only; where to listen |
| `port` | `8080` | HTTP only |
| `relays.read` | public relays | Comma-separated relay URIs to read from |
| `relays.write` | the read set | Relay URIs to publish to |
| `identity` | none | Bind this server to one alias |
| `identity.default` | the only identity | Which identity signs when none is named |
| `keystore.type` | `os-keychain` | `os-keychain`, `encrypted-file` or `env` |
| `keystore.path` | `~/.nostr-java/keys.p12` | Encrypted-file keystore location |
| `identities` | discovered | Aliases to look for, for backends that cannot list themselves |
| `write-policy` | `confirm` | `deny`, `confirm` or `allow` |
| `identity-policy` | `confirm` | Keystore mutation, capped by `write-policy` |
| `dm.decrypt-for` | none | Aliases whose messages may be decrypted |
| `limits.max-events-per-query` | `500` | Most events one query returns |
| `limits.query-timeout` | `15s` | How long a query waits |
| `limits.writes-per-minute` | `10` | Per-identity publishing rate limit |
| `limits.max-subscriptions` | `20` | Open subscriptions allowed |
| `limits.subscription-buffer` | `500` | Events held per subscription between reads |
| `limits.subscription-idle-timeout` | `1h` | When an unread subscription is closed |

## Limits worth knowing about

These are properties of the underlying SDK and the protocol, not settings you can tune away.

- **Throughput is per relay.** Each relay connection serves one request at a time, so many
  concurrent queries against the same relay queue behind each other. This is why queries are
  bounded by `limits.query-timeout`: an unbounded one would stall every other tool call.
- **De-duplication is windowed.** The SDK delivers each event once however many relays carry
  it, but over a bounded window. A relay replaying an old event long afterwards can arrive
  again; subscription buffers drop the repeat, and a query may show it.
- **Subscriptions do not survive a restart.** Nothing is persisted. If the server restarts, open
  subscriptions are gone and the agent must open them again.
- **A full subscription buffer drops the oldest events.** The read reports a `droppedCount` so
  the agent knows it missed some; read more often or narrow the filter.
- **The HTTP transport has no authentication.** See [Run it over HTTP](#run-it-over-http).
- **Deletion is advisory.** NIP-09 asks relays to forget an event; it cannot compel them. Treat
  anything published as permanent, which is why `write-policy: confirm` is the default.

## Checking that a model can still use the tools

Most tests here ask whether the tools work. One asks whether a model can *use* them, which is a
different question: a tool can be correct and still unusable because its name misleads or its
description omits what the model needs to decide.

`OllamaAgentIT` runs a real local model against the live tool surface and checks that it picks
the right tool unprompted, tells querying from subscribing, and reads a publish preview as "not
yet published" rather than as success.

```bash
# Needs Ollama models cached at ~/.ollama/models. Skips cleanly if they are absent.
mvn -pl nostr-java-mcp verify -Dexcluded.it.groups= -Dgroups=model-driven
```

It is excluded from the ordinary build because it takes several minutes. Run it when you change
a tool's name, description or schema: those are exactly the changes nothing else can catch.

## Related

- [Send and read NIP-17 private direct messages](private-direct-messages.md)
- [Publish and subscribe across many relays](multi-relay-publishing.md)
- [The MCP server's design](../explanation/nostr-java-mcp-spec.md)
