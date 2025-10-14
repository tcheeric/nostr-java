# Client/Handler Test Suite

This package contains tests for the API client and the internal WebSocket handler.

## Structure

- `NostrSpringWebSocketClient*` — Tests for high-level client behavior (logging, relays, integration).
- `WebSocketHandler*` — Tests for internal handler semantics:
  - `SendCloseFrame` — Ensures CLOSE frame is sent on handle close.
  - `CloseSequencing` — Verifies close ordering and exception handling.
  - `CloseIdempotent` — Double close does not throw.
  - `SendRequest` — Encodes correct subscription id; multi-sub tests.
  - `RequestError` — IOException wrapping as RuntimeException.
- `NostrRequestDispatcher*` — Tests REQ dispatch across handlers including de-duplication and ensureClient calls.
- `NostrSubscriptionManager*` — Tests subscribe lifecycle and close error aggregation.

## Notes

- `nostr.api.TestHandlerFactory` is used to instantiate a `WebSocketClientHandler` from outside the `nostr.api` package while preserving access to its package-private constructor.
- Logging assertions use `slf4j-test` to capture and inspect log events.
