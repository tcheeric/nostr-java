# Run the Nostr MCP server

This guide shows how to run `nostr-java-mcp` so an LLM agent can use Nostr, how to give it a
signing key, and how to choose how much freedom it has. It assumes you have a built
`nostr-java-mcp` jar and an MCP host such as Claude Desktop or an IDE agent.

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

## Run it over stdio

An MCP host launches the server itself and speaks to it over standard input and output. Add an
entry to your host's configuration:

```json
{
  "mcpServers": {
    "nostr": {
      "command": "java",
      "args": ["-jar", "/path/to/nostr-java-mcp.jar"]
    }
  }
}
```

The server starts with `write-policy: confirm`, so the agent must confirm before anything is
published. See [Choosing how much freedom the agent has](#choosing-how-much-freedom-the-agent-has).

## Bind one server to one identity

If you hold several keys, bind a server to one so the agent cannot post as the wrong account.
The `identity` argument then disappears from every signing tool, and only that key is decrypted:

```json
{
  "mcpServers": {
    "nostr-personal":    { "command": "java", "args": ["-jar", "nostr-java-mcp.jar", "--nostr.mcp.identity=personal"] },
    "nostr-project-bot": { "command": "java", "args": ["-jar", "nostr-java-mcp.jar", "--nostr.mcp.identity=project-bot"] }
  }
}
```

A bound server refuses to start if its identity does not exist, and registers no
keystore-mutating tools.

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

## Related

- [Send and read NIP-17 private direct messages](private-direct-messages.md)
- [Publish and subscribe across many relays](multi-relay-publishing.md)
- [The MCP server's design](../explanation/nostr-java-mcp-spec.md)
