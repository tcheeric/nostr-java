# Streaming Subscriptions

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how‑to](use-nostr-java-api.md) · [Custom events](custom-events.md) · [API reference](../reference/nostr-java-api.md)

This guide explains how to open and manage long‑lived, non‑blocking subscriptions to Nostr relays
using the `nostr-java` API. It covers lifecycle, concurrency/backpressure, multiple relays, and
error handling.

## Overview

- Use `NostrSpringWebSocketClient.subscribe` to open a REQ subscription that streams relay messages
  to your callback.
- The method returns immediately with an `AutoCloseable`. Calling `close()` sends a `CLOSE` to the
  relay(s) and frees the underlying WebSocket resource(s).
- Callbacks run on the WebSocket thread; offload heavy work to your own executor/queue to keep the
  socket responsive.

## Quick start

```java
import java.util.Map;
import nostr.api.NostrSpringWebSocketClient;
import nostr.base.Kind;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;

Map<String, String> relays = Map.of("damus", "wss://relay.damus.io");

NostrSpringWebSocketClient client = new NostrSpringWebSocketClient().setRelays(relays);

Filters filters = new Filters(new KindFilter<>(Kind.TEXT_NOTE));

AutoCloseable subscription = client.subscribe(
    filters,
    "example-subscription",
    message -> {
      // Handle EVENT/EOSE/NOTICE payloads here. Offload if heavy.
    },
    error -> {
      // Log/report errors. Consider retry or metrics.
    }
);

// ... keep the subscription open while processing events ...

subscription.close(); // sends CLOSE and releases resources
client.close();       // closes any remaining relay connections
```

See a runnable example in [../../nostr-java-examples/src/main/java/nostr/examples/SpringSubscriptionExample.java](../../nostr-java-examples/src/main/java/nostr/examples/SpringSubscriptionExample.java).

## Lifecycle and closing

- Each `subscribe` call opens a dedicated WebSocket per relay. Keep the handle while you need the
  stream and call `close()` when done.
- Always close subscriptions to ensure a `CLOSE` frame is sent to the relay and resources are freed.
- After `close()`, no further messages will be delivered to your listener.

## Concurrency and backpressure

- Message callbacks execute on the WebSocket thread; avoid blocking. If processing may block, hand
  off to a separate executor or queue.
- For high‑throughput feeds, consider batching or asynchronous processing to prevent socket stalls.

## Multiple relays

- When multiple relays are configured via `setRelays`, the client opens one WebSocket per relay and
  fans out the same REQ. Your listener receives messages from all configured relays.
- Include an identifier (e.g., relay name/URL) in logs/metrics if you need per‑relay visibility.

## Error handling

- Provide an `errorListener` to capture exceptions raised during subscription or message handling.
- Consider transient vs. fatal errors. You can implement retry logic at the application level if
  desired.

## Related API

- Client: `nostr-java-api/src/main/java/nostr/api/NostrSpringWebSocketClient.java`
- WebSocket wrapper: `nostr-java-client/src/main/java/nostr/client/springwebsocket/SpringWebSocketClient.java`
- Interface: `nostr-java-client/src/main/java/nostr/client/springwebsocket/WebSocketClientIF.java`

For method signatures and additional details, see the API reference: [../reference/nostr-java-api.md](../reference/nostr-java-api.md).
