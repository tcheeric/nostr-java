# Configuration

Tune WebSocket behavior and retries for your environment.

## Purpose

- Adjust timeouts and poll intervals for send operations.
- Understand retry behavior for transient I/O failures.

## WebSocket client settings

The Spring WebSocket client reads the following properties (with defaults):

- `nostr.websocket.await-timeout-ms` (default: `60000`) — Max time to await a response after send.
- `nostr.websocket.poll-interval-ms` (default: `500`) — Poll interval used during await.
- `nostr.websocket.max-idle-timeout-ms` (default: `3600000`) — Max idle timeout for WebSocket sessions. Set to `0` for no timeout. This prevents premature connection closures when relays have periods of inactivity.

Example (application.properties):

```
nostr.websocket.await-timeout-ms=30000
nostr.websocket.poll-interval-ms=250
nostr.websocket.max-idle-timeout-ms=7200000  # 2 hours
```

## Retry behavior

WebSocket send and subscribe operations are annotated with a common retry policy:

- Included exception: `IOException`
- Max attempts: `3`
- Backoff: initial `500ms`, multiplier `2.0`

These values are defined in the `@NostrRetryable` annotation. To customize globally, consider:

- Creating a custom annotation or replacing `@NostrRetryable` with your configuration.
- Providing your own `NoteService` or client wrapper that applies your retry strategy.

## Notes

- Timeouts apply per send; long-running subscriptions are managed separately.
- Ensure your relay endpoints’ SLAs align with chosen timeouts and backoff.
