# Logging

Configure logging for nostr-java using your preferred SLF4J backend (e.g., Logback).

## Purpose

- Control verbosity for `nostr.*` packages.
- Separate client transport logs from application logs.
- Capture failures for troubleshooting without overwhelming output.

## Quick Start (Logback)

Add `logback.xml` to your classpath (e.g., `src/main/resources/logback.xml`):

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- Reduce noise by default -->
  <logger name="nostr" level="INFO"/>

  <!-- Raise transport/client logs when troubleshooting -->
  <logger name="nostr.client.springwebsocket" level="DEBUG"/>
  <logger name="nostr.api" level="DEBUG"/>

  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

## Useful Categories

- `nostr.api` — High-level API flows and event dispatching
- `nostr.api.client` — Dispatcher, relay registry, subscription manager
- `nostr.client.springwebsocket` — Low-level send/subscribe, retry recoveries
- `nostr.event` — Serialization, validation, decoding

## Tips

- Use `DEBUG` on `nostr.client.springwebsocket` to see REQ/close frames and retry recoveries.
- Use `WARN` or `ERROR` globally in production; temporarily bump `nostr.*` to `DEBUG` for investigations.

## Spring Boot logging tips

You can control logging without a custom Logback file using `application.properties`:

```properties
# Reduce global noise, selectively raise nostr categories
logging.level.root=INFO
logging.level.nostr=INFO
logging.level.nostr.api=DEBUG
logging.level.nostr.client.springwebsocket=DEBUG

# Optional: color and pattern tweaks (console)
spring.output.ansi.enabled=ALWAYS
logging.pattern.console=%d{HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n

# Write to a rolling file (Boot-managed)
logging.file.name=logs/nostr-java.log
logging.logback.rollingpolicy.max-history=7
logging.logback.rollingpolicy.max-file-size=10MB
```

### JSON logging (Logback)

For structured logs you can use Logstash Logback Encoder.

Add the dependency (version managed by your BOM/build):

```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  
</dependency>
```

Example `logback.xml` (console JSON):

```xml
<configuration>
  <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LoggingEventJsonEncoder"/>
  </appender>

  <logger name="nostr" level="INFO"/>
  <logger name="nostr.client.springwebsocket" level="DEBUG"/>

  <root level="INFO">
    <appender-ref ref="JSON"/>
  </root>
</configuration>
```

Tip: Use MDC to correlate sends/subscriptions across logs. In pattern layouts include `%X{key}`; with JSON, add an MDC provider or use the default providers (MDC entries are emitted automatically by Logstash encoder).
