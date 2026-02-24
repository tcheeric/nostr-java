# Streaming Subscriptions

Navigation: [Docs index](../README.md) · [Getting started](../GETTING_STARTED.md) · [API how-to](use-nostr-java-api.md) · [Custom events](custom-events.md) · [API reference](../reference/nostr-java-api.md)

This guide explains how to open and manage long-lived, non-blocking subscriptions to Nostr relays using `NostrRelayClient`.

## Overview

- Use `NostrRelayClient.subscribe()` to open a REQ subscription that streams relay messages to your callback.
- The method returns immediately with an `AutoCloseable`. Calling `close()` sends a `CLOSE` to the relay and frees the underlying WebSocket resource.
- Callbacks are dispatched on Virtual Threads, so expensive listener logic does not block inbound WebSocket I/O.

## Quick start

```java
import nostr.base.Kinds;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.message.ReqMessage;

import java.util.List;

// Build a filter
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .limit(100)
    .build();

Filters filters = new Filters(filter);
String subscriptionId = "my-sub-" + System.currentTimeMillis();
ReqMessage req = new ReqMessage(subscriptionId, filters);

// Open subscription
try (NostrRelayClient client = new NostrRelayClient("wss://relay.398ja.xyz")) {
    AutoCloseable subscription = client.subscribe(
        req,
        message -> {
            // Handle EVENT/EOSE/NOTICE payloads here
            System.out.println("Received: " + message);
        },
        error -> {
            System.err.println("Error: " + error.getMessage());
        },
        () -> {
            System.out.println("Connection closed");
        }
    );

    // Keep the subscription open while processing events
    Thread.sleep(30_000);

    subscription.close();  // sends CLOSE and releases resources
}
```

## Async subscription (Virtual Threads)

```java
NostrRelayClient.connectAsync("wss://relay.398ja.xyz")
    .thenCompose(client -> client.subscribeAsync(
        req.encode(),
        message -> System.out.println("Event: " + message),
        error -> System.err.println("Error: " + error),
        () -> System.out.println("Closed")
    ))
    .thenAccept(subscription -> {
        // subscription is AutoCloseable — close when done
    });
```

## Lifecycle and closing

- Each `subscribe()` call opens a dedicated WebSocket connection. Keep the returned handle while you need the stream and call `close()` when done.
- Always close subscriptions to ensure a `CLOSE` frame is sent to the relay and resources are freed.
- After `close()`, no further messages will be delivered to your listener.

## Concurrency and backpressure

- Message callbacks execute on Virtual Threads; processing can block safely, but bounded queues are still recommended when downstream systems are slower than relay throughput.
- For high-throughput feeds, consider batching or asynchronous processing to prevent socket stalls.
- The client limits accumulated events per blocking request to 10,000 by default (configurable) to prevent unbounded memory growth.

## Filter examples

```java
// Filter by multiple kinds and authors
EventFilter filter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE, Kinds.REACTION))
    .authors(List.of(pubKeyHex1, pubKeyHex2))
    .since(System.currentTimeMillis() / 1000 - 86400)  // last 24 hours
    .limit(200)
    .build();

// Filter by tag values
EventFilter tagFilter = EventFilter.builder()
    .kinds(List.of(Kinds.TEXT_NOTE))
    .addTagFilter("t", List.of("nostr", "bitcoin"))
    .build();
```

## Error handling

- Provide an `errorListener` to capture exceptions raised during subscription or message handling.
- If the relay times out on a blocking send, `NostrRelayClient` throws `RelayTimeoutException` (not an empty list).
- Consider transient vs. fatal errors. The client uses Spring Retry with exponential backoff for transient I/O failures.

## Related API

- Client: `nostr-java-client/src/main/java/nostr/client/springwebsocket/NostrRelayClient.java`
- Filters: `nostr-java-event/src/main/java/nostr/event/filter/EventFilter.java`

For method signatures and additional details, see the [API reference](../reference/nostr-java-api.md).
