# Configuration

Tune WebSocket behavior and retries for your environment.

## Purpose

- Adjust timeouts for send operations.
- Understand retry behavior for transient I/O failures.
- Configure WebSocket buffer sizes for large events.

## WebSocket client settings

`NostrRelayClient` reads the following properties (with defaults):

- `nostr.websocket.await-timeout-ms` (default: `60000`) — Max time to await a response after send.
- `nostr.websocket.max-idle-timeout-ms` (default: `3600000`) — Max idle timeout for WebSocket sessions. Set to `0` for no timeout. Prevents premature connection closures when relays have periods of inactivity.
- `nostr.websocket.max-text-message-buffer-size` (default: `1048576`) — WebSocket text message buffer size in bytes.
- `nostr.websocket.max-binary-message-buffer-size` (default: `1048576`) — WebSocket binary message buffer size in bytes.

Example (application.properties):

```
nostr.websocket.await-timeout-ms=30000
nostr.websocket.max-idle-timeout-ms=7200000
nostr.websocket.max-text-message-buffer-size=2097152
```

## Retry behavior

WebSocket send and subscribe operations are annotated with `@NostrRetryable`:

- Included exception: `IOException`
- Max attempts: `3`
- Backoff: initial `500ms`, multiplier `2.0`

To customize globally, create a custom annotation or replace `@NostrRetryable` with your configuration.

## Virtual Thread dispatch

Relay subscription callbacks and async operations (`connectAsync`, `sendAsync`, `subscribeAsync`) are dispatched on Virtual Threads using named thread factories:

- `nostr-relay-io-*` — relay I/O operations
- `nostr-listener-*` — listener/callback dispatch
- `nostr-http-*` — HTTP client operations (NIP-05 validation)

## Notes

- Timeouts apply per send; long-running subscriptions are managed separately.
- If a relay response is not received before the timeout elapses, `NostrRelayClient` throws `RelayTimeoutException`.
- The maximum number of events accumulated per blocking request defaults to 10,000 to prevent unbounded memory growth.
- Ensure your relay endpoints' SLAs align with chosen timeouts and backoff.
