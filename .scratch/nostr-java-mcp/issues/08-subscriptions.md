# 08: Long-lived subscriptions with buffering and resources

**What to build:** An agent asked to "watch my mentions" receives events that arrive after the
call returns, and is told when it missed some.

MCP cannot push into a tool result, so a subscription becomes a stateful server resource: the
tool opens it and returns an id, events land in a bounded buffer, and the agent drains them.
The buffer is not de-duplication, which the SDK already does across relays; it exists because
events arriving between polls must be held somewhere.

`RelayPool.subscribe` is **asynchronous**: it returns before any stored event or the
end-of-backlog signal arrives, verified against a live relay in `McpSpecAssumptionsIT`. So the
tool must not pretend history is ready. It reports `backlogDrained` so an agent can tell
"nothing matched yet" from "still replaying", and a tool that blocked until the backlog drained
would stall on any unresponsive relay.

**Blocked by:** 05 (Read tools).

**Status:** ready-for-agent

- [x] `nostr_subscribe` opens a subscription across every relay and returns an id plus
      `backlogDrained: false`
- [x] Events land in a bounded ring buffer; overflow drops the oldest and increments a
      monotonic `droppedCount` so the agent knows it missed data
- [x] `nostr_read_subscription` drains what it returns, so repeated calls yield only new events
- [x] `nostr_list_subscriptions` reports filters, buffer depth, drop count and relay health
- [x] `nostr_unsubscribe` closes a subscription and frees its buffer
- [x] Each subscription is exposed as `nostr://subscription/{id}` with update notifications
- [x] An idle TTL reaps abandoned subscriptions, and the live total is capped
- [x] A relay dropping mid-stream is surfaced rather than silently reducing coverage
- [x] Buffers tolerate a duplicate, since SDK de-duplication is windowed
- [x] `mvn -q verify` passes
