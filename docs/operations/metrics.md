# Metrics

Capture simple client metrics (successes/failures) without bringing a full metrics stack.

## Purpose

- Track successful and failed relay sends.
- Provide hooks for plugging into your metrics/observability system.

## Minimal counters

```java
import java.util.concurrent.atomic.AtomicLong;

class Counters {
    final AtomicLong sendsOk = new AtomicLong();
    final AtomicLong sendsFailed = new AtomicLong();
}

Counters metrics = new Counters();

try (NostrRelayClient client = new NostrRelayClient("wss://relay.example.com")) {
    List<String> responses = client.send(new EventMessage(event));
    metrics.sendsOk.incrementAndGet();
} catch (Exception e) {
    metrics.sendsFailed.incrementAndGet();
}
```

## Integrating with your stack

- Micrometer: Wrap send calls with `Timer` and `Counter` instances.
- Prometheus: Expose counters using your HTTP endpoint.
- Logs: Periodically log counters as structured JSON.

## Notes

- Prefer batching external calls (e.g., ship metrics on a schedule) over per-event network calls.

## Micrometer example (with Prometheus)

Add Micrometer + Prometheus dependencies (Spring Boot example):

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-core</artifactId>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
  <scope>runtime</scope>
</dependency>
```

Register counters and a timer:

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.event.message.EventMessage;

public class NostrMetrics {
    private final Counter sendsOk;
    private final Counter sendsFailed;
    private final Timer sendTimer;

    public NostrMetrics(MeterRegistry registry) {
        this.sendsOk = Counter.builder("nostr.sends.ok")
            .description("Successful relay responses").register(registry);
        this.sendsFailed = Counter.builder("nostr.sends.failed")
            .description("Failed relay sends").register(registry);
        this.sendTimer = Timer.builder("nostr.send.timer")
            .description("Send latency per event")
            .publishPercentileHistogram().register(registry);
    }

    public List<String> timedSend(NostrRelayClient client, EventMessage message) {
        return sendTimer.record(() -> {
            try {
                List<String> responses = client.send(message);
                sendsOk.increment();
                return responses;
            } catch (Exception e) {
                sendsFailed.increment();
                throw new RuntimeException(e);
            }
        });
    }
}
```

## Spring Boot wiring example

```java
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import nostr.id.Identity;

@Configuration
public class NostrConfig {

    @Bean
    public Identity nostrIdentity() {
        return Identity.generateRandomIdentity();
    }

    @Bean
    public NostrMetrics nostrMetrics(MeterRegistry registry) {
        return new NostrMetrics(registry);
    }
}
```

Use in your service:

```java
import nostr.base.Kinds;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

@Service
@RequiredArgsConstructor
public class NostrService {
    private final Identity identity;
    private final NostrMetrics metrics;

    public List<String> publish(String content) throws Exception {
        GenericEvent event = GenericEvent.builder()
            .pubKey(identity.getPublicKey())
            .kind(Kinds.TEXT_NOTE)
            .content(content)
            .build();
        identity.sign(event);

        try (NostrRelayClient client = new NostrRelayClient("wss://relay.398ja.xyz")) {
            return metrics.timedSend(client, new EventMessage(event));
        }
    }
}
```

Application properties:

```properties
management.endpoints.web.exposure.include=prometheus
management.endpoint.prometheus.enabled=true
nostr.websocket.await-timeout-ms=30000
```
