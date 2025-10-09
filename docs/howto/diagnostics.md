# Diagnostics: Relay Failures and Troubleshooting

This how‑to shows how to inspect, capture, and react to relay send failures when broadcasting events via the API client.

## Overview

- `DefaultNoteService` attempts to send an event to all configured relays.
- Failures on individual relays are tolerated; other relays are still attempted.
- After the send completes, you can inspect failures and structured details.
- You can also register a listener to receive failures in real time.

## Inspect last failures

```java
NostrSpringWebSocketClient client = new NostrSpringWebSocketClient(sender);
client.setRelays(Map.of(
  "relayA", "wss://relayA.example.com",
  "relayB", "wss://relayB.example.com"
));

List<String> responses = client.sendEvent(event);

// Map<String, Throwable>: relay name to exception
Map<String, Throwable> failures = client.getLastSendFailures();
failures.forEach((relay, error) -> System.err.printf(
  "Relay %s failed: %s%n", relay, error.getMessage()
));

// Structured details (timestamp, relay URI, cause chain summary)
Map<String, DefaultNoteService.FailureInfo> details = client.getLastSendFailureDetails();
details.forEach((relay, info) -> System.err.printf(
  "[%d] %s (%s) failed: %s | root: %s - %s%n",
  info.timestampEpochMillis,
  info.relayName,
  info.relayUri,
  info.message,
  info.rootCauseClass,
  info.rootCauseMessage
));
```

Note: If you use a custom `NoteService`, these accessors return empty maps unless the implementation exposes diagnostics.

## Receive failures with a listener

Register a callback to receive the failures map immediately after each send attempt:

```java
client.onSendFailures(failureMap -> {
  failureMap.forEach((relay, t) -> System.err.printf(
    "Failure on %s: %s: %s%n",
    relay, t.getClass().getSimpleName(), t.getMessage()
  ));
});
```

## Tips

- Partial success is common on public relays; prefer aggregating successful responses.
- Use `getLastSendFailureDetails()` when you need to correlate failures with relay URIs or log timestamps.
- Combine diagnostics with your retry/backoff strategy at the application level if needed.

## MDC snippet (correlate logs per send)

Use SLF4J MDC to attach a correlation id for a send. Remember to clear the MDC in `finally`.

```java
import org.slf4j.MDC;
import java.util.UUID;

String correlationId = UUID.randomUUID().toString();
MDC.put("corrId", correlationId);
try {
  var responses = client.sendEvent(event);
  // Your logging here; include %X{corrId} in your log pattern
  log.info("Sent event id={} corrId={} responses={}", event.getId(), correlationId, responses.size());
} finally {
  MDC.remove("corrId");
}
```

Logback pattern example:

```properties
logging.pattern.console=%d{HH:mm:ss.SSS} %-5level [%X{corrId}] %logger{36} - %msg%n
```
