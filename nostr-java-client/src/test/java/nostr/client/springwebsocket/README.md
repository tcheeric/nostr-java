# Client Module Tests (springwebsocket)

This package contains tests for the Spring-based WebSocket client.

## What’s covered

- `SpringWebSocketClientTest`
  - Retry behavior for `send(String)` with recoveries and final failure
  - Retry behavior for `subscribe(...)` (message overload and raw String overload)
- `StandardWebSocketClientTimeoutTest`
  - Timeout path returns an empty list and closes session

## Notes

- The tests wire a test WebSocketClientIF into `SpringWebSocketClient` using Spring’s `@Configuration` to simulate retries and failures deterministically.
- Keep callbacks (`messageListener`, `errorListener`, `closeListener`) short and non-blocking in production; tests use simple counters to assert behavior.
