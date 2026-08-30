# 02: Module skeleton with MCP stdio transport

**What to build:** An MCP host such as Claude Desktop can launch `nostr-java-mcp` over stdio,
see its tool list, and call a read-only tool that reports the configured relays and their
connection state.

This is the tracer bullet for the whole module: transport, tool registration, configuration and
the SDK underneath, proven end to end by one tool that needs no keystore and writes nothing.

The module depends on `nostr-java-api` and reaches relays through `NostrClient`. It must not
reach past that to `NostrRelayClient`, which would rebuild connection management, result
aggregation and de-duplication that the SDK already owns.

**Blocked by:** None (can start immediately).

**Status:** done

- [x] `nostr-java-mcp` is added to the build, depending on `nostr-java-api`, with nothing
      depending on it
- [x] The server starts over stdio using the official MCP SDK and responds to tool discovery
- [x] `NostrToolRegistry` declares tools one class per tool, so a new tool is a new class
      rather than an edit to a switch
- [x] `nostr_list_relays` returns the configured relays with their connection state
- [x] `RelayDirectory` resolves logical relay names to the URIs handed to `NostrClient`
- [x] Configuration binds under `nostr.mcp.*` and works with no hand-written config file
- [x] A golden-file test pins the tool list so surface changes are visible in review
- [x] `mvn -q verify` passes
