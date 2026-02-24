# Diagnostics: Relay Failures and Troubleshooting

This how-to shows how to inspect and react to relay send failures when broadcasting events.

## Overview

- `NostrRelayClient` sends events to a single relay per connection.
- Failures throw exceptions (`IOException`, `RelayTimeoutException`) that can be caught and handled.
- Spring Retry (`@NostrRetryable`) automatically retries transient I/O failures with exponential backoff.

## Catching failures

```java
try (NostrRelayClient client = new NostrRelayClient("wss://relay.example.com")) {
    List<String> responses = client.send(new EventMessage(event));
    System.out.println("Responses: " + responses);
} catch (RelayTimeoutException e) {
    System.err.printf("Timeout after %dms on relay %s%n", e.getTimeoutMs(), e.getRelayUri());
} catch (IOException e) {
    System.err.println("Send failed: " + e.getMessage());
}
```

## Sending to multiple relays

Send to multiple relays and collect results:

```java
List<String> relays = List.of("wss://relay1.example.com", "wss://relay2.example.com");
Map<String, List<String>> results = new HashMap<>();
Map<String, Throwable> failures = new HashMap<>();

for (String relay : relays) {
    try (NostrRelayClient client = new NostrRelayClient(relay)) {
        results.put(relay, client.send(new EventMessage(event)));
    } catch (Exception e) {
        failures.put(relay, e);
    }
}

failures.forEach((relay, error) ->
    System.err.printf("Relay %s failed: %s%n", relay, error.getMessage())
);
```

## Async multi-relay send

```java
List<String> relays = List.of("wss://relay1.example.com", "wss://relay2.example.com");

List<CompletableFuture<List<String>>> futures = relays.stream()
    .map(relay -> NostrRelayClient.connectAsync(relay)
        .thenCompose(client -> client.sendAsync(new EventMessage(event))))
    .toList();

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
    .thenRun(() -> System.out.println("All relays attempted"))
    .join();
```

## MDC snippet (correlate logs per send)

Use SLF4J MDC to attach a correlation id for a send:

```java
import org.slf4j.MDC;
import java.util.UUID;

String correlationId = UUID.randomUUID().toString();
MDC.put("corrId", correlationId);
try {
    var responses = client.send(new EventMessage(event));
    log.info("Sent event id={} corrId={} responses={}", event.getId(), correlationId, responses.size());
} finally {
    MDC.remove("corrId");
}
```

Logback pattern example:

```properties
logging.pattern.console=%d{HH:mm:ss.SSS} %-5level [%X{corrId}] %logger{36} - %msg%n
```

## Tips

- Partial success is common on public relays; send to multiple relays for redundancy.
- `RelayTimeoutException` provides `getRelayUri()` and `getTimeoutMs()` for structured diagnostics.
- Spring Retry handles transient failures automatically (3 attempts, exponential backoff from 500ms).
