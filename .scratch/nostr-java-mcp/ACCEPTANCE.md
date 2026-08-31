# End-to-end acceptance check

Drives the **shipped jar** as an MCP host does, over stdio, against a real relay, and asserts
one requirement per ticket checklist item. It exists because the module's own tests exercise
classes through their own seams: they can all pass while the artefact a user actually runs
behaves differently. That is not hypothetical, it is how the bound-server `identity` argument
defect was found.

## Running it

```bash
# A relay. Retry until it starts without the po2_denom panic, which leaves it accepting
# connections while silently answering nothing.
docker run -d --name nostr-acceptance-relay -p 127.0.0.1:18777:8080 scsibug/nostr-rs-relay:0.8.13
docker logs nostr-acceptance-relay 2>&1 | grep po2_denom   # if this matches, recreate it

mvn -o -pl nostr-java-mcp package -DskipTests
python3 .scratch/nostr-java-mcp/accept.py
```

Expected: `33/33 requirements verified against the shipped jar`.

## What it covers

Every substantive checklist item across the twelve tickets: the CLI creating keys without
printing them, the full 22-tool surface, key secrecy across the whole surface, refusing to guess
between identities, confirm-then-publish including single-use tokens, querying published events
back, identifier and timestamp handling, profile round trips, subscription open/drain/close,
contacts, the DM decryption opt-in, the absence of NIP-04, prompts and resources, and the three
policy modes (bound, read-only, environment-configured).

## Known flake, and why it is not the product

`nostr-rs-relay:0.8.13` sometimes panics during startup (`po2_denom was zero!`) and then accepts
connections while storing nothing. The harness probes by publishing and requiring acceptance,
rather than by connecting, because a connection succeeds against an inert relay. If the probe
gives up, recreate the container. The Maven ITs handle this with
`RelayStoresEventsWaitStrategy` and `withStartupAttempts`.
