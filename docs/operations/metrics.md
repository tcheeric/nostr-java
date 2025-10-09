# Metrics

Capture simple client metrics (successes/failures) without bringing a full metrics stack.

## Purpose

- Track successful and failed relay sends.
- Provide hooks for plugging into your metrics/observability system.

## Minimal counters via listener

```java
class Counters {
  final java.util.concurrent.atomic.AtomicLong sendsOk = new java.util.concurrent.atomic.AtomicLong();
  final java.util.concurrent.atomic.AtomicLong sendsFailed = new java.util.concurrent.atomic.AtomicLong();
}

Counters metrics = new Counters();
NostrSpringWebSocketClient client = new NostrSpringWebSocketClient(sender);

client.onSendFailures(failureMap -> {
  // Any failure increments failed; actual successes counted after sendEvent
  metrics.sendsFailed.addAndGet(failureMap.size());
});

var responses = client.sendEvent(event);
metrics.sendsOk.addAndGet(responses.size());
```

## Integrating with your stack

- Micrometer: Wrap the listener to increment `Counter` instances and register with your registry.
- Prometheus: Expose counters using your HTTP endpoint and update from the listener.
- Logs: Periodically log counters as structured JSON for ingestion by your log pipeline.

## Notes

- Listener runs on the calling thread; keep callbacks fast and non-blocking.
- Prefer batching external calls (e.g., ship metrics on a schedule) over per-event network calls.

## Micrometer example (with Prometheus)

Add Micrometer + Prometheus dependencies (Spring Boot example):

```xml
<!-- pom.xml -->
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-core</artifactId>
  <scope>runtime</scope>
  <!-- version managed by Spring Boot BOM or your own BOM -->
  
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
  <scope>runtime</scope>
  
</dependency>
```

Register counters and a timer, then wire the failure listener:

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import nostr.api.NostrSpringWebSocketClient;
import nostr.base.IEvent;

public class NostrMetrics {
  private final Counter sendsOk;
  private final Counter sendsFailed;
  private final Timer sendTimer;

  public NostrMetrics(MeterRegistry registry) {
    this.sendsOk = Counter.builder("nostr.sends.ok").description("Successful relay responses").register(registry);
    this.sendsFailed = Counter.builder("nostr.sends.failed").description("Failed relay sends").register(registry);
    this.sendTimer = Timer.builder("nostr.send.timer").description("Send latency per event").publishPercentileHistogram().register(registry);
  }

  public void instrument(NostrSpringWebSocketClient client) {
    // Count failures per send call (sum of relays that failed)
    client.onSendFailures((Map<String, Throwable> failures) -> sendsFailed.increment(failures.size()));
  }

  public List<String> timedSend(NostrSpringWebSocketClient client, IEvent event) {
    return sendTimer.record(() -> client.sendEvent(event));
  }
}
```

Labeling failures by relay (beware high cardinality):

```java
client.onSendFailures(failures -> failures.forEach((relay, t) ->
  Counter.builder("nostr.sends.failed")
      .tag("relay", relay) // cardinality grows with number of relays
      .tag("exception", t.getClass().getSimpleName())
      .register(registry)
      .increment()
));
```

Expose Prometheus metrics (Spring Boot):

```properties
# application.properties
management.endpoints.web.exposure.include=prometheus
management.endpoint.prometheus.enabled=true
```

Navigate to `/actuator/prometheus` to scrape metrics.

## Spring Boot wiring example

Create a configuration that wires the client, metrics, and listener:

```java
// src/main/java/com/example/nostr/NostrConfig.java
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import nostr.api.NostrSpringWebSocketClient;
import nostr.id.Identity;

@Configuration
public class NostrConfig {

  @Bean
  public Identity nostrIdentity() {
    // Replace with a real private key or a managed Identity
    return Identity.generateRandomIdentity();
  }

  @Bean
  public NostrSpringWebSocketClient nostrClient(Identity identity) {
    return new NostrSpringWebSocketClient(identity);
  }

  @Bean
  public NostrMetrics nostrMetrics(MeterRegistry registry, NostrSpringWebSocketClient client) {
    NostrMetrics metrics = new NostrMetrics(registry);
    metrics.instrument(client);
    return metrics;
  }
}
```

Use the instrumented client and timer in your service:

```java
// src/main/java/com/example/nostr/NostrService.java
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import nostr.api.NostrSpringWebSocketClient;
import nostr.event.impl.GenericEvent;
import nostr.base.Kind;

@Service
@RequiredArgsConstructor
public class NostrService {
  private final NostrSpringWebSocketClient client;
  private final NostrMetrics metrics;

  public List<String> publish(String content) {
    GenericEvent event = GenericEvent.builder()
        .pubKey(client.getSender().getPublicKey())
        .kind(Kind.TEXT_NOTE)
        .content(content)
        .build();
    event.update();
    client.sign(client.getSender(), event);
    return metrics.timedSend(client, event);
  }
}
```

Application properties (example):

```properties
# Expose Prometheus endpoint
management.endpoints.web.exposure.include=prometheus
management.endpoint.prometheus.enabled=true

# Optional: tune WebSocket timeouts
nostr.websocket.await-timeout-ms=30000
nostr.websocket.poll-interval-ms=250
```
